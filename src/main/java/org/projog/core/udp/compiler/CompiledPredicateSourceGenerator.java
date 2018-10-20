/*
 * Copyright 2013 S. Webber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projog.core.udp.compiler;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getKeyGeneration;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getNewListSyntax;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getNewVariableSyntax;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getUnifyStatement;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.isNoMoreThanTwoElementList;
import static org.projog.core.udp.compiler.CompiledPredicateVariables.ARGUMENT_PREFIX;
import static org.projog.core.udp.compiler.CompiledPredicateVariables.INLINED_CTR_PREFIX;
import static org.projog.core.udp.compiler.CompiledPredicateVariables.PLACEHOLDER_PREFIX;
import static org.projog.core.udp.compiler.CompiledPredicateWriter.MAIN_LOOP_LABEL;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;
import org.projog.core.udp.MultipleRulesWithMultipleImmutableArgumentsPredicate;
import org.projog.core.udp.MultipleRulesWithSingleImmutableArgumentPredicate;

/**
 * Manages the construction of source code for new {@link CompiledPredicate} subclasses.
 * <p>
 * Delegates to {@link PredicateInvocationGenerator} instances.
 */
final class CompiledPredicateSourceGenerator {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and its intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring.

   private static final String PACKAGE_NAME = "org.projog.content_generated_at_runtime";
   private static final String DEBUG_ENABLED = "d";
   private static final String SPYPOINT = "s";

   private final CompiledPredicateWriter w;

   public CompiledPredicateSourceGenerator(CompiledPredicateWriter writer) {
      this.w = writer;
   }

   public void generateSource() {
      outputPackageImportAndClassStatements(className());
      outputMethodsForAllClauses();
      outputConstructors();
      outputBacktrackMethodIfRequired();
      if (factMetaData().isTailRecursive()) {
         outputRecursiveMethods();
      } else {
         outputEvaluateMethod(factMetaData().getClauses());
      }
      outputIsRetryableMethod();
      outputcouldReevaluationSucceedMethod(factMetaData().getClauses());
      outputSetKnowledgeBaseMethod();
      outputGetPredicateMethod();
      outputMemberVariables();
      w.endBlock();
   }

   private void outputPackageImportAndClassStatements(String className) {
      w.writePackage(PACKAGE_NAME);
      w.writeImport("org.projog.core.udp.compiler.*");
      w.writeImport("org.projog.core.udp.interpreter.*");
      w.writeImport("org.projog.core.udp.*");
      w.writeImport("org.projog.core.term.*");
      w.writeImport("org.projog.core.*");
      w.writeImport("static org.projog.core.term.NumericTermComparator.NUMERIC_TERM_COMPARATOR");
      w.writeImport("static org.projog.core.function.AbstractSingletonPredicate.toPredicate");

      String s;
      if (factMetaData().isTailRecursive()) {
         s = "extends CompiledTailRecursivePredicate";
      } else {
         s = "implements CompiledPredicate";
      }
      w.comment(factMetaData().getPredicateKey());
      w.beginClass(className, s);
   }

   private void outputMemberVariables() {
      outputStaticMemberVariables();
      if (isSpyPointsEnabled()) {
         outputDebugMemberVariables();
      }
      if (factMetaData().isTailRecursive()) {
         outputTailRecursiveMemberVariables();
      }
      if (factMetaData().isSingleResultPredicate() == false) {
         outputMultiResultMemberVariables();
      }
   }

   private void outputStaticMemberVariables() {
      for (Map.Entry<String, Term> e : classVariables().getStaticMemberVariables()) {
         w.writeStatement("private static final Term " + e.getKey() + " = " + w.outputCreateTermStatement(e.getValue(), false));
      }

      if (w.isNeedsKnowledgeBaseStaticVariable()) {
         w.writeStatement("private static KnowledgeBase kb");
      }

      if (w.isNeedsCalculatablesStaticVariable()) {
         w.writeStatement("private static Calculatables c");
      }

      for (CompiledPredicateVariables.PredicateFactoryStaticVariable v : classVariables().getRequiredPredicateFactories()) {
         String variableName = v.variableName;
         PredicateFactory ef = v.PredicateFactory;
         w.writeStatement("private static " + getClassName(ef) + " " + variableName);
      }
   }

   private void outputDebugMemberVariables() {
      w.writeStatement("private static SpyPoints.SpyPoint " + SPYPOINT);
      if (factMetaData().isSingleResultPredicate() == false) {
         w.writeStatement("private final boolean " + DEBUG_ENABLED);
      }
   }

   private void outputTailRecursiveMemberVariables() {
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         if (factMetaData().isTailRecursiveArgument(i)) {
            w.writeStatement("private List " + PLACEHOLDER_PREFIX + i);
         }
      }

      w.beginMethod("public final boolean[] isSingleResultIfArgumentImmutable()");
      StringBuilder s = new StringBuilder();
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         if (i != 0) {
            s.append(", ");
         }
         s.append(factMetaData().isSingleResultIfArgumentImmutable(i));
      }
      w.writeStatement("return new boolean[]{" + s + "}");
      w.endBlock();

      if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
         w.writeStatement("private final boolean isRetryable");
      }
   }

   private void outputMultiResultMemberVariables() {
      if (factMetaData().isCutVariableRequired()) {
         w.writeStatement("private boolean isCut");
      }
      if (factMetaData().isMultipleAnswersClause()) {
         w.writeStatement("private boolean isRetrying");
      }
      w.writeStatement("private int clauseCtr");
      w.writeStatement("private int conjunctionCtr");

      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         w.writeStatement("private Term " + ARGUMENT_PREFIX + i);
      }

      for (CompiledPredicateVariables.MemberVariable v : classVariables().getVariablesToDeclare()) {
         w.writeStatement("private " + v.type + " " + v.name);
      }
   }

   private void outputConstructors() {
      outputPublicConstructor();
      if (factMetaData().isSingleResultPredicate() == false) {
         outputPackageConstructor();
      }
   }

   /**
    * Constructs a no-arg public constructor.
    * <p>
    * The no-arg construcor is used by {@link CompiledPredicateClassGenerator} to create the first instance of a new
    * implementation of {@link CompiledPredicate}.
    */
   private void outputPublicConstructor() {
      w.beginMethod("public " + className() + "(final KnowledgeBase _kb)");
      if (w.isNeedsKnowledgeBaseStaticVariable()) {
         w.assign("kb", "_kb");
      }
      if (w.isNeedsCalculatablesStaticVariable()) {
         w.assign("c", "KnowledgeBaseUtils.getCalculatables(_kb)");
      }

      if (isSpyPointsEnabled()) {
         w.assign(SPYPOINT, "KnowledgeBaseUtils.getSpyPoints(_kb).getSpyPoint(" + getKeyGeneration(factMetaData().getPredicateKey()) + ")");
         if (factMetaData().isSingleResultPredicate() == false) {
            w.assign(DEBUG_ENABLED, "false"); // value will never be used - just set to something as is "final"
         }
      }
      for (CompiledPredicateVariables.PredicateFactoryStaticVariable v : classVariables().getRequiredPredicateFactories()) {
         PredicateKey key = v.key;
         PredicateFactory ef = v.PredicateFactory;
         String getSyntax = "_kb.getPredicateFactory(" + getKeyGeneration(key) + ")";
         if (v.isCompiledPredicate) {
            getSyntax = "((StaticUserDefinedPredicateFactory)" + getSyntax + ").getActualPredicateFactory()";
         }
         String castType = getClassName(ef);
         w.assign(v.variableName, "(" + castType + ")" + getSyntax);
      }
      if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
         w.writeStatement("isRetryable = true");
      }
      w.endBlock();
   }

   private void outputPackageConstructor() {
      StringBuilder args = new StringBuilder();
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         if (i != 0) {
            args.append(", ");
         }
         args.append("final Term in" + i);
      }

      w.beginMethod(className() + "(" + args + ")");
      if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
         StringBuilder args2 = new StringBuilder();
         for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
            if (i != 0) {
               args2.append(", ");
            }
            args2.append("in" + i);
         }
         w.writeStatement("this(" + args2 + ", true)");
         w.endBlock();

         w.beginMethod(className() + "(" + args + ", final boolean isRetryable)");
         w.writeStatement("this.isRetryable = isRetryable");
      }

      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         w.assign(ARGUMENT_PREFIX + i, "in" + i + ".getTerm()");
      }

      if (isSpyPointsEnabled() && factMetaData().isSingleResultPredicate() == false) {
         w.assign(DEBUG_ENABLED, SPYPOINT + ".isEnabled()");
      }

      w.endBlock();
   }

   private void outputBacktrackMethodIfRequired() {
      if (isBacktrackMethodRequired()) {
         outputBacktrackMethod();
      }
   }

   private boolean isBacktrackMethodRequired() {
      return factMetaData().isSingleResultPredicate() == false;// && (factMetaData().isTailRecursive()==true || factMetaData().getNumberArguments()>0);
   }

   /**
    * Constructs a {@code backtrack()} method.
    * <p>
    * Has to be {@code protected} if a tail recursive function so it can be used by its
    * {@link org.projog.core.udp.TailRecursivePredicate} superclass.
    */
   private void outputBacktrackMethod() {
      String access = factMetaData().isTailRecursive() ? "protected" : "private";
      w.beginMethod(access + " final void backtrack()");
      outputBacktrackMethodBody();
      w.endBlock();
   }

   private void outputBacktrackMethodBody() {
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         String argumentVariableId = ARGUMENT_PREFIX + i;
         if (factMetaData().isTailRecursiveArgument(i)) {
            w.beginIf(argumentVariableId + "!=" + PLACEHOLDER_PREFIX + i);
            w.outputBacktrack(argumentVariableId);
            w.endBlock();
         } else {
            w.outputBacktrack(argumentVariableId);
         }
      }
   }

   /**
    * Constructs an implementation of
    * {@link org.projog.core.PredicateFactory#setKnowledgeBase(org.projog.core.KnowledgeBase)}.
    */
   private void outputSetKnowledgeBaseMethod() {
      w.beginMethod("public final void setKnowledgeBase(KnowledgeBase kb)");
      w.writeStatement("throw new RuntimeException()");
      w.endBlock();
   }

   /** Constructs an implementation of {@link org.projog.core.Predicate#isRetryable()}. */
   private void outputIsRetryableMethod() {
      w.addLine("public final boolean isRetryable() {");
      if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
         w.writeStatement("return isRetryable");
      } else if (factMetaData().isSingleResultPredicate()) {
         w.returnFalse();
      } else {
         w.returnTrue();
      }
      w.endBlock();
   }

   /** Constructs an implementation of {@link org.projog.core.Predicate#couldReevaluationSucceed()}. */
   private void outputcouldReevaluationSucceedMethod(ClauseMetaData[] clauses) {
      w.addLine("public final boolean couldReevaluationSucceed() {");
      if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
         w.writeStatement("return isRetryable");
      } else if (factMetaData().isSingleResultPredicate()) {
         w.returnFalse();
      } else {
         int clausesLength = clauses.length;
         ClauseMetaData lastClause = clauses[clausesLength - 1];
         if (clausesLength == 1) {
            // do nothing
         } else if (lastClause.isSingleResult()) {
            w.ifTrueReturnTrue("clauseCtr<" + (clausesLength));
         } else {
            w.ifTrueReturnTrue("clauseCtr<" + (clausesLength - 1));
         }
         if (lastClause.isSingleResult() == false) {
            int retryableClausesCtr = 0;
            int retryableCompiledPredicateClausesCtr = 0;
            int inlineClausesCtr = 0;
            for (int i = 0; i < lastClause.getConjunctionCount(); i++) {
               if (lastClause.isConjunctionMulipleResult(i)) {
                  PredicateFactory pf = lastClause.getPredicateFactory(i);
                  if (pf instanceof MultipleRulesWithMultipleImmutableArgumentsPredicate) {
                     w.beginIf(INLINED_CTR_PREFIX + inlineClausesCtr + "<" + ((MultipleRulesWithMultipleImmutableArgumentsPredicate) pf).data.length);
                     w.returnTrue();
                     w.endBlock();
                     inlineClausesCtr++;
                  } else if (pf instanceof MultipleRulesWithSingleImmutableArgumentPredicate) {
                     w.beginIf(INLINED_CTR_PREFIX + inlineClausesCtr + "<" + ((MultipleRulesWithSingleImmutableArgumentPredicate) pf).data.length);
                     w.returnTrue();
                     w.endBlock();
                     inlineClausesCtr++;
                  } else if (pf instanceof CompiledPredicate) {
                     String varId = "c" + (clausesLength - 1) + "_" + retryableCompiledPredicateClausesCtr;
                     w.beginIf(varId + "==null || " + varId + ".couldReevaluationSucceed()");
                     w.returnTrue();
                     w.endBlock();
                     retryableCompiledPredicateClausesCtr++;
                  } else if (pf == null) {
                     w.ifTrueReturnTrue("true");
                  } else {
                     w.beginIf("e" + retryableClausesCtr + "==null || e" + retryableClausesCtr + ".couldReevaluationSucceed()");
                     w.returnTrue();
                     w.endBlock();
                     retryableClausesCtr++;
                  }
               }
            }
         }
         w.returnFalse();
      }
      w.endBlock();
   }

   /**
    * Constructs implementations of {@link org.projog.core.udp.TailRecursivePredicate#matchFirstRule()} and
    * {@link org.projog.core.udp.TailRecursivePredicate#matchSecondRule()}.
    */
   private void outputRecursiveMethods() {
      w.beginMethod("protected final boolean matchFirstRule()");
      w.writeStatement("return initRule0()");
      w.endBlock();

      w.beginMethod("protected final boolean matchSecondRule()");
      w.writeStatement("return initRule1()");
      w.endBlock();

      w.beginMethod("protected final void logCall()");
      logCall();
      w.endBlock();

      w.beginMethod("protected final void logRedo()");
      logRedo();
      w.endBlock();

      w.beginMethod("protected final void logExit()");
      logExit(1);
      w.endBlock();

      w.beginMethod("protected final void logFail()");
      logFail();
      w.endBlock();
   }

   /** Constructs an implementation of {@link org.projog.core.PredicateFactory#getPredicate(Term...)}. */
   private void outputGetPredicateMethod() {
      w.beginMethod("public final Predicate getPredicate(final Term... termArgs)");
      if (factMetaData().isSingleResultPredicate()) {
         StringBuilder args = new StringBuilder(); // TODO extract method
         for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
            if (i != 0) {
               args.append(',');
            }
            args.append("termArgs[").append(i).append(']');
         }
         w.writeStatement("return toPredicate(staticEvaluate(" + args + "))");
      } else {
         StringBuilder args = new StringBuilder();
         for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
            if (i != 0) {
               args.append(", ");
            }
            args.append("termArgs[" + i + "]");
         }

         if (factMetaData().isPossibleSingleResultRecursiveFunction()) {
            boolean matched = false;
            args.append(", !(");
            for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
               if (factMetaData().isSingleResultIfArgumentImmutable(i)) {
                  if (matched) {
                     args.append(" || ");
                  } else {
                     matched = true;
                  }
                  args.append("termArgs[" + i + "].isImmutable()");
               }
            }
            args.append(")");
         }

         w.writeStatement("return new " + className() + "(" + args + ")");
      }
      w.endBlock();
   }

   private void outputEvaluateMethod(ClauseMetaData[] clauses) {
      if (factMetaData().isSingleResultPredicate()) {
         outputSingleResultEvaluateMethod();
      } else {
         outputMultiResultEvaluateMethod(clauses);
      }
   }

   /**
    * Constructs an implementation of {@link org.projog.core.Predicate#evaluate(Term...)} for a
    * {@link CompiledPredicate} that only ever returns at most a single result per unique query.
    */
   private void outputSingleResultEvaluateMethod() {
      w.beginMethod("public final boolean evaluate()");
      w.writeStatement("throw new UnsupportedOperationException()"); // TODO just don't implement Predicate, so won't need to implement this method
      w.endBlock();

      w.beginMethod("static final boolean staticEvaluate(" + getArgsDeclaration() + ")");
      declareDebugEnabledLocalVariable();
      logCall();
      w.writeStatement("return " + getRuleInitMethodNameCall());
      w.endBlock();
   }

   private void declareDebugEnabledLocalVariable() {
      if (isSpyPointsEnabled()) {
         w.assign("final boolean " + DEBUG_ENABLED, SPYPOINT + ".isEnabled()");
      }
   }

   /**
    * Constructs an implementation of {@link org.projog.core.Predicate#evaluate(Term...)} for a
    * {@link CompiledPredicate} that may return multiple results per unique query.
    */
   private void outputMultiResultEvaluateMethod(ClauseMetaData[] clauses) {
      w.beginMethod("public final boolean evaluate()");

      if (isSpyPointsEnabled()) {
         w.beginIf(DEBUG_ENABLED);
         w.beginIf("clauseCtr==0" + (clauses[0].isSingleResult() ? "" : " && !isRetrying"));
         logCall();
         w.addLine("} else {");
         logRedo();
         w.endBlock();
         w.endBlock();
      }

      if (factMetaData().isCutVariableRequired()) {
         w.ifTrueReturnFalse("isCut");
      }
      if (clauses.length == 1) {
         outputSingleClauseEvaluateBlock();
      } else {
         outputMultipleClausesEvaluateBlock(clauses);
      }
      logFail();
      w.returnFalse();
      w.endBlock();
   }

   private void outputSingleClauseEvaluateBlock() {
      if (currentClause().isSingleResult()) {
         w.ifTrueReturnTrue(getRuleInitMethodNameCall());
      } else {
         outputMultiAnswerClauseEvaluateBlock(0);
      }
   }

   private void outputMultipleClausesEvaluateBlock(ClauseMetaData[] clauses) {
      int clauseCtr = 0;

      w.beginSwitch("clauseCtr");
      for (ClauseMetaData clause : clauses) {
         setCurrentClause(clause);
         w.beginCase(clauseCtr);
         if (clause.isSingleResult()) {
            if (clauseCtr != 0) {
               w.writeStatement("backtrack()");
            }
            w.beginIf(getRuleInitMethodNameCall());
            w.assign("clauseCtr", clauseCtr + 1);
            logExit(clauseCtr + 1);
            w.returnTrue();
            w.endBlock();
         } else {
            outputMultiAnswerClauseEvaluateBlock(clauseCtr);
         }
         if (clause.containsCut()) {
            w.ifTrueReturnFalse("isCut");
         }
         clauseCtr++;
      }
      w.endBlock();
      w.assign("clauseCtr", clauseCtr); // do this in case evaluate called again after returning false
      w.writeStatement("backtrack()");
   }

   private void outputMultiAnswerClauseEvaluateBlock(int clauseCtr) {
      w.beginIf("isRetrying");
      w.beginIf(getRuleRetryMethodName());
      logExit(clauseCtr + 1);
      w.returnTrue();
      w.endBlock();
      w.assignFalse("isRetrying");
      w.elseStatement();
      if (clauseCtr != 0) {
         w.writeStatement("backtrack()");
      }
      w.beginIf(getRuleInitMethodNameCall() + " && " + getRuleRetryMethodName());
      w.assignTrue("isRetrying");
      if (clauseCtr != 0) {
         w.assign("clauseCtr", clauseCtr);
      }
      logExit(clauseCtr + 1);
      w.returnTrue();
      w.endBlock();
      w.endBlock();
   }

   private void outputMethodsForAllClauses() {
      for (ClauseMetaData clause : factMetaData().getClauses()) {
         outputMethodsForClause(clause);
      }
   }

   private void outputMethodsForClause(ClauseMetaData clauseMetaData) {
      setCurrentClause(clauseMetaData);
      classVariables().addMemberVariables(currentClause());

      outputInitMethod();

      if (currentClause().isSingleResult() == false) {
         outputRetryMethodForRule();
      }
   }

   private void outputInitMethod() {
      outputInitMethod(getRuleInitMethodNameDeclaration());
   }

   private void outputInitMethod(String methodNameDeclarartion) {
      w.beginMethod(methodNameDeclarartion);

      outputInitMethodBody();

      if (factMetaData().isSingleResultPredicate()) {
         declareDebugEnabledLocalVariable();
         logExit(1);
      }
      w.returnTrue();
      w.endBlock();
   }

   private void outputInitMethodBody() {
      outputMatchConsequent();
      outputAntecedantConjunctions();
   }

   private void outputMatchConsequent() {
      Term consequent = currentClause().getConsequent();
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         boolean tailRecursiveArgument = factMetaData().isTailRecursiveArgument(i);
         outputMatchConsequentArgument(consequent.getArgument(i), ARGUMENT_PREFIX + i, tailRecursiveArgument ? i : -1);
      }
   }

   /** @param tailRecursiveArgumentIdx index of the tail recursive argument, or -1 if not tail recursive */
   private void outputMatchConsequentArgument(Term argument, String variableNameToCompareTo, int tailRecursiveArgumentIdx) {
      final Set<Variable> newlyDeclaredVariables = declareArgumentVariabledNotAlreadyDeclared(argument);

      final boolean isTailRecursivePredicate = tailRecursiveArgumentIdx != -1;
      final boolean isFirstClauseOfTailRecursivePredicate = isTailRecursivePredicate && currentClause().getClauseIndex() == 0;
      if (isFirstClauseOfTailRecursivePredicate) {
         String outputCreateTermStatement = null;
         w.beginIf(variableNameToCompareTo + "==" + PLACEHOLDER_PREFIX + tailRecursiveArgumentIdx);

         Set<String> tmp = classVariables().getAssignedVariables();
         outputCreateTermStatement = w.outputCreateTermStatement(argument, true);
         classVariables().setAssignedVariables(tmp);
         w.writeStatement(PLACEHOLDER_PREFIX + tailRecursiveArgumentIdx + ".setTail(" + outputCreateTermStatement + ")");
         w.endBlock();

         if (currentClause().isIgnorableVariable(argument)) {
            // ignoring "argument" as only mentioned once in consequent and never in antecedant
            return;
         }

         w.addLine("else {");
      }

      if (argument.getType() == TermType.NAMED_VARIABLE) {
         outputMatchVariableConsequentArgument(argument, variableNameToCompareTo);
      } else if (isNoMoreThanTwoElementList(argument)) {
         if (isTailRecursivePredicate) {
            String placeholderVariableId = PLACEHOLDER_PREFIX + tailRecursiveArgumentIdx;
            String newListHead;
            if (argument.getArgument(0).getType() == TermType.NAMED_VARIABLE) {
               newListHead = w.getVariableId(argument.getArgument(0));
            } else {
               newListHead = w.outputCreateTermStatement(argument.getArgument(0), true);
            }
            String placeholderList = getNewListSyntax(newListHead, CompiledPredicateWriter.EMPTY_LIST_SYNTAX);

            if (currentClause().getClauseIndex() == 1) {
               w.beginIf(variableNameToCompareTo + "==" + placeholderVariableId);
               if (newlyDeclaredVariables.contains(argument.getArgument(0))) {
                  String variableId = w.getVariableId(argument.getArgument(0));
                  if (!w.isAssigned(variableId)) {
                     w.assign(variableId, getNewVariableSyntax(argument.getArgument(0)));
                  }
               }
               String tmpId = getNewTempoaryVariableName();
               w.assign("final List " + tmpId, placeholderList);
               w.writeStatement(placeholderVariableId + ".setTail(" + tmpId + ")");
               w.assign(placeholderVariableId, tmpId);
               assignNullToVariableIfRequired(argument.getArgument(1), newlyDeclaredVariables);

               w.endBlock();
               w.addLine("else");
            }

            w.beginIf(variableNameToCompareTo + ".getType()==TermType.NAMED_VARIABLE");
            if (newlyDeclaredVariables.contains(argument.getArgument(0))) {
               w.assign(w.getVariableId(argument.getArgument(0)), getNewVariableSyntax(argument.getArgument(0)));
            }
            w.assign(placeholderVariableId, placeholderList);
            writeIfConsequentArgumentUnificationFailsReturnFalse(placeholderVariableId, variableNameToCompareTo);
            assignNullToVariableIfRequired(argument.getArgument(1), newlyDeclaredVariables);

            w.elseIf(variableNameToCompareTo + ".getType()==TermType.LIST");
         } else {
            w.beginIf(variableNameToCompareTo + ".getType()==TermType.LIST");
         }
         for (Variable v : newlyDeclaredVariables) {
            if (argument.getArgument(0) != v && argument.getArgument(1) != v) {
               String variableId = w.getVariableId(v);
               if (!w.isAssigned(variableId)) {
                  classVariables().addAssignedVariable(variableId);
                  w.assign(variableId, getNewVariableSyntax(v));
               }
            }
         }

         // check head
         assignArgument(argument, variableNameToCompareTo, newlyDeclaredVariables, 0);
         // check tail
         assignArgument(argument, variableNameToCompareTo, newlyDeclaredVariables, 1);

         if (!isTailRecursivePredicate) {
            w.elseIf(variableNameToCompareTo + ".getType()==TermType.NAMED_VARIABLE");
            // variable will of been declared above in if (out of scope of this else)
            for (Variable v : newlyDeclaredVariables) {
               classVariables().addAssignedVariable(w.getVariableId(v));
               w.assign(w.getVariableId(v), getNewVariableSyntax(v));
            }
            String value = w.outputCreateTermStatement(argument, true);
            w.writeStatement(getUnifyStatement(value, variableNameToCompareTo));
         }
         w.elseStatement();
         w.exitCodeBlock();
         w.endBlock();
      } else {
         String value = w.outputCreateTermStatement(argument, true);
         writeIfConsequentArgumentUnificationFailsReturnFalse(value, variableNameToCompareTo);
      }

      if (isFirstClauseOfTailRecursivePredicate) {
         w.endBlock();
      }
   }

   private void assignNullToVariableIfRequired(Term argument, Set<Variable> newlyDeclaredVariables) {
      if (argument.getType() == TermType.NAMED_VARIABLE && !currentClause().isIgnorableVariable(argument) && !classVariables().isAssignedVariable(w.getVariableId(argument))) {
         w.assign(w.getVariableId(argument), null);
      }
   }

   private void assignArgument(Term argument, String variableNameToCompareTo, Set<Variable> newlyDeclaredVariables, int argumentIdx) {
      final String getArgumentMethod = ".getArgument(" + argumentIdx + ")";
      if (newlyDeclaredVariables.contains(argument.getArgument(argumentIdx))) {
         classVariables().addAssignedVariable(w.getVariableId(argument.getArgument(argumentIdx)));
         w.assign(w.getVariableId(argument.getArgument(argumentIdx)), variableNameToCompareTo + getArgumentMethod);
      } else {
         String tmpId = getNewTempoaryVariableName();
         w.classVariables().addAssignedVariable(tmpId);
         w.assign("final Term " + tmpId, variableNameToCompareTo + getArgumentMethod);
         outputMatchConsequentArgument(argument.getArgument(argumentIdx), tmpId, -1);
      }
   }

   private String getNewTempoaryVariableName() {
      int ctr = 0;
      String tmpId;
      do {
         tmpId = "t" + (ctr++);
      } while (w.classVariables().isDeclaredVariable(tmpId));
      return tmpId;
   }

   private Set<Variable> declareArgumentVariabledNotAlreadyDeclared(Term argument) {
      Set<Variable> variablesInTerm = TermUtils.getAllVariablesInTerm(argument);
      // LinkedSet so order predictable (makes unit tests easier)
      Set<Variable> newlyDeclaredVariables = new LinkedHashSet<>();
      for (Variable v : variablesInTerm) {
         if (w.declareVariableIfNotAlready(v, false)) {
            newlyDeclaredVariables.add(v);
         }
      }
      return newlyDeclaredVariables;
   }

   private void outputMatchVariableConsequentArgument(Term argument, String variableNameToCompareTo) {
      Variable v = (Variable) argument;

      if (currentClause().isIgnorableVariable(v)) {
         // ignoring "argument" as only mentioned once in consequent and never in antecedant
      } else {
         String variableId = w.getVariableId(argument);
         boolean firstUse = classVariables().isAssignedVariable(variableId) == false;
         if (firstUse) {
            w.declareVariableIfNotAlready(argument, false);
            classVariables().addAssignedVariable(variableId);
            w.assign(variableId, variableNameToCompareTo);
         } else {
            writeIfConsequentArgumentUnificationFailsReturnFalse(variableId, variableNameToCompareTo);
         }
      }
   }

   private void outputAntecedantConjunctions() {
      int finish = currentClause().isSingleResult() ? currentClause().getConjunctionCount() : currentClause().getIndexOfFirstMulipleResultConjuction();
      for (int i = 0; i < finish; i++) {
         currentClause().setConjunctionIndex(i);
         outputFunctionInAntecedant();
      }
   }

   private void outputRetryMethodForRule() {
      currentClause().clearVariablesToBackTrack();
      w.beginMethod("private final boolean " + getRuleRetryMethodName());
      w.addLine("do {");
      w.addLine(MAIN_LOOP_LABEL + ":");
      w.beginSwitch("conjunctionCtr");

      int conjunctionCtr = 0;
      for (int i = currentClause().getIndexOfFirstMulipleResultConjuction(); i < currentClause().getConjunctionCount(); i++) {
         int lastBacktrackPoint = currentClause().getLastBacktrackPoint();
         if (currentClause().isConjunctionMulipleResult(i)) {
            w.beginCase(conjunctionCtr);
            lastBacktrackPoint = conjunctionCtr;
            conjunctionCtr++;
         }
         currentClause().setConjunctionIndex(i);
         outputFunctionInAntecedant();
         if (currentClause().isConjunctionMulipleResult(i) && lastBacktrackPoint != 0) {
            w.assign("conjunctionCtr", lastBacktrackPoint);
         }
         currentClause().setLastBacktrackPoint(lastBacktrackPoint);
      }

      // if we get this far without breaking out of the switch then all
      // conjunctions have been met
      if (currentClause().isNothingToBackTrack()) {
         w.returnTrue();
      } else {
         w.assign("conjunctionCtr", conjunctionCtr);
         w.returnTrue();

         w.beginCase(conjunctionCtr);
         w.assign("conjunctionCtr", conjunctionCtr - 1);
         w.outputBacktrack();
      }

      w.endBlock();
      w.addLine("} while (true);");
      w.endBlock();
   }

   private void outputFunctionInAntecedant() {
      PredicateFactory ef = currentClause().getCurrentPredicateFactory();
      PredicateInvocationGenerator efjsg = PredicateInvocationGeneratorFactory.getPredicateInvocationGenerator(ef);
      efjsg.generate(w);
   }

   private void writeIfConsequentArgumentUnificationFailsReturnFalse(String variable1, String variable2) {
      w.beginIf("!" + getUnifyStatement(variable1, variable2));
      // not backtracking as comparing consequent arguments
      w.exitCodeBlock();
      w.endBlock();
   }

   private String getRuleInitMethodNameDeclaration() {
      if (factMetaData().isSingleResultPredicate()) {
         return "private static final boolean initRule" + currentClause().getClauseIndex() + "(" + getArgsDeclaration() + ")";
      } else {
         return "private final boolean initRule" + currentClause().getClauseIndex() + "()";
      }
   }

   private String getRuleInitMethodNameCall() {
      if (factMetaData().isSingleResultPredicate()) {
         return "initRule" + currentClause().getClauseIndex() + "(" + getArgsCall() + ")";
      } else {
         return "initRule" + currentClause().getClauseIndex() + "()";
      }
   }

   private String getRuleRetryMethodName() {
      return "retryRule" + currentClause().getClauseIndex() + "()";
   }

   private void logCall() {
      log("Call", -1);
   }

   private void logRedo() {
      log("Redo", -1);
   }

   private void logExit(int clauseIdx) {
      log("Exit", clauseIdx);
   }

   private void logFail() {
      log("Fail", -1);
   }

   private void log(String level, int clauseIdx) {
      if (isSpyPointsEnabled()) {
         w.beginIf(DEBUG_ENABLED);
         String source = factMetaData().isSingleResultPredicate() ? className() + ".class" : "this";
         if (factMetaData().getNumberArguments() > 0) {
            source += ", new Term[]{" + getArgsCall() + "}";
         } else {
            source += ", TermUtils.EMPTY_ARRAY";
         }

         if (clauseIdx > 0) {
            source += ", " + clauseIdx;
         } else if ("Exit".equals(level)) {
            throw new IllegalArgumentException(level + " " + clauseIdx);
         }

         w.writeStatement(SPYPOINT + ".log" + level + "(" + source + ")");
         w.endBlock();
      }
   }

   private StringBuilder getArgsDeclaration() {
      return getArgsCsv("final Term " + ARGUMENT_PREFIX, null);
   }

   private StringBuilder getArgsDeclarationNotFinal() {
      return getArgsCsv("Term " + ARGUMENT_PREFIX, null);
   }

   private StringBuilder getArgsCall() {
      return getArgsCsv(ARGUMENT_PREFIX, null);
   }

   private StringBuilder getArgsCsv(String prefix, String suffix) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < factMetaData().getNumberArguments(); i++) {
         if (i != 0) {
            sb.append(", ");
         }
         sb.append(prefix);
         sb.append(i);
         if (suffix != null) {
            sb.append(suffix);
         }
      }
      return sb;
   }

   private String getClassName(PredicateFactory ef) {
      // replace $ with . so inner classes work
      return ef.getClass().getName().replace('$', '.');
   }

   private String className() {
      return w.className();
   }

   private PredicateMetaData factMetaData() {
      return w.factMetaData();
   }

   private final CompiledPredicateVariables classVariables() {
      return w.classVariables();
   }

   private ClauseMetaData currentClause() {
      return w.currentClause();
   }

   private void setCurrentClause(ClauseMetaData clauseMetaData) {
      w.setCurrentClause(clauseMetaData);
   }

   private boolean isSpyPointsEnabled() {
      return w.isSpyPointsEnabled();
   }
}
