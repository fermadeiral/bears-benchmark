/*
 * Copyright 2013-2014 S. Webber
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
package org.projog;

import static org.junit.Assert.assertTrue;
import static org.projog.core.KnowledgeBaseUtils.getOperands;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.projog.core.KnowledgeBase;
import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Operands;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogDefaultProperties;
import org.projog.core.ProjogProperties;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.Atom;
import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.List;
import org.projog.core.term.ListFactory;
import org.projog.core.term.Structure;
import org.projog.core.term.Term;
import org.projog.core.term.TermFormatter;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;

/**
 * Helper methods for performing unit tests.
 */
public class TestUtils {
   public static final PredicateKey ADD_PREDICATE_KEY = new PredicateKey("pj_add_predicate", 2);
   public static final PredicateKey ADD_CALCULATABLE_KEY = new PredicateKey("pj_add_calculatable", 2);
   public static final File BOOTSTRAP_FILE = new File("src/main/resources/projog-bootstrap.pl");
   public static final ProjogProperties COMPILATION_DISABLED_PROPERTIES = new ProjogDefaultProperties() {
      @Override
      public boolean isRuntimeCompilationEnabled() {
         return false;
      }
   };
   public static final ProjogProperties COMPILATION_ENABLED_PROPERTIES = new ProjogDefaultProperties() {
      @Override
      public boolean isRuntimeCompilationEnabled() {
         return true;
      }
   };

   private static final File TEMP_DIR = new File("target");

   private static final Operands OPERANDS = getOperands(createKnowledgeBase());

   /**
    * Private constructor as all methods are static.
    */
   private TestUtils() {
      // do nothing
   }

   public static File writeToTempFile(Class<?> c, String contents) {
      try {
         File tempFile = createTempFile(c.getClass());
         try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(contents);
         }
         return tempFile;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static File createTempFile(Class<?> c) throws IOException {
      TEMP_DIR.mkdir();
      File tempFile = File.createTempFile(c.getName(), ".tmp", TEMP_DIR);
      tempFile.deleteOnExit();
      return tempFile;
   }

   public static KnowledgeBase createKnowledgeBase() {
      try {
         KnowledgeBase kb = KnowledgeBaseUtils.createKnowledgeBase();
         KnowledgeBaseUtils.bootstrap(kb);
         return kb;
      } catch (Throwable t) {
         t.printStackTrace();
         throw new RuntimeException(t);
      }
   }

   public static KnowledgeBase createKnowledgeBase(ProjogProperties projogProperties) {
      try {
         KnowledgeBase kb = KnowledgeBaseUtils.createKnowledgeBase(projogProperties);
         KnowledgeBaseUtils.bootstrap(kb);
         return kb;
      } catch (Throwable t) {
         t.printStackTrace();
         throw new RuntimeException(t);
      }
   }

   public static Atom atom() {
      return atom("test");
   }

   public static Atom atom(String name) {
      return new Atom(name);
   }

   public static Structure structure() {
      return structure("test", new Term[] {atom()});
   }

   public static Structure structure(String name, Term... args) {
      return (Structure) Structure.createStructure(name, args);
   }

   public static List list(Term... args) {
      return (List) ListFactory.createList(args);
   }

   public static IntegerNumber integerNumber() {
      return integerNumber(1);
   }

   public static IntegerNumber integerNumber(long i) {
      return new IntegerNumber(i);
   }

   public static DecimalFraction decimalFraction() {
      return decimalFraction(1.0);
   }

   public static DecimalFraction decimalFraction(double d) {
      return new DecimalFraction(d);
   }

   public static Variable variable() {
      return variable("X");
   }

   public static Variable variable(String name) {
      return new Variable(name);
   }

   public static Term[] createArgs(int numberOfArguments) {
      return createArgs(numberOfArguments, atom());
   }

   public static Term[] createArgs(int numberOfArguments, Term term) {
      Term[] args = new Term[numberOfArguments];
      Arrays.fill(args, term);
      return args;
   }

   public static SentenceParser createSentenceParser(String prologSyntax) {
      return SentenceParser.getInstance(prologSyntax, OPERANDS);
   }

   public static Term parseSentence(String prologSyntax) {
      SentenceParser sp = createSentenceParser(prologSyntax);
      return sp.parseSentence();
   }

   public static Term parseTerm(String source) {
      SentenceParser sp = createSentenceParser(source);
      return sp.parseTerm();
   }

   public static ClauseModel createClauseModel(String prologSentenceSytax) {
      Term t = parseSentence(prologSentenceSytax);
      return ClauseModel.createClauseModel(t);
   }

   public static String write(Term t) {
      return new TermFormatter(OPERANDS).toString(t);
   }

   public static Term[] parseTermsFromFile(File f) {
      try (FileReader fr = new FileReader(f)) {
         SentenceParser sp = SentenceParser.getInstance(fr, OPERANDS);

         ArrayList<Term> result = new ArrayList<>();
         Term next;
         while ((next = sp.parseSentence()) != null) {
            result.add(next);
         }
         return result.toArray(new Term[result.size()]);
      } catch (IOException e) {
         throw new RuntimeException("Could not parse: " + f, e);
      }
   }

   public static void assertStrictEquality(Term t1, Term t2, boolean expectedResult) {
      assertTrue(t1.strictEquality(t2) == expectedResult);
      assertTrue(t2.strictEquality(t1) == expectedResult);
   }
}
