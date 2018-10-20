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
package org.projog.core.term;

import static org.projog.core.KnowledgeBaseUtils.getOperands;

import org.projog.core.KnowledgeBase;
import org.projog.core.Operands;

/**
 * Produces {@code String} representations of {@link Term} instances.
 * <p>
 * Does take account of operator precedence.
 * 
 * @see #toString(Term)
 */
public class TermFormatter {
   private final Operands operands;

   public TermFormatter(KnowledgeBase kb) {
      this(getOperands(kb));
   }

   public TermFormatter(Operands operands) {
      this.operands = operands;
   }

   /**
    * Returns a string representation of the specified {@code Term}.
    * <p>
    * This method does take account of current operator declarations - thus an infix operator will be printed out
    * between its arguments. This method represents lists as a comma separated sequence of elements enclosed in square
    * brackets.
    * <p>
    * For example:
    * 
    * <pre>
    * Term structure = Structure.createStructure("+", new IntegerNumber(1), new IntegerNumber(2));
    * Term list = ListFactory.create(new Term[]{new Atom("a"), Atom("b"), Atom("c")});
    * System.out.println("Structure.toString():      "+structure.toString());
    * System.out.println("Write.toString(structure): "+write.toString(structure));
    * System.out.println("List.toString():           "+list.toString());
    * System.out.println("Write.toString(list):      "+write.toString(list));
    * </pre>
    * would print out:
    * 
    * <pre>
    * Structure.toString():      +(1, 2)
    * Write.toString(structure): 1 + 2
    * List.toString():           .(a, .(b, .(c, [])))
    * Write.toString(list):      [a,b,c]
    * </pre>
    * 
    * @param t the {@code Term} to represent as a string
    * @return a string representation of the specified {@code Term}
    */
   public String toString(Term t) {
      StringBuilder sb = new StringBuilder();
      write(t, sb);
      return sb.toString();
   }

   private void write(Term t, StringBuilder sb) {
      switch (t.getType()) {
         case STRUCTURE:
            writePredicate(t, sb);
            break;
         case LIST:
            writeList(t, sb);
            break;
         case EMPTY_LIST:
            sb.append("[]");
            break;
         case NAMED_VARIABLE:
            sb.append(((Variable) t).getId());
            break;
         default:
            sb.append(t.toString());
      }
   }

   private void writeList(Term p, StringBuilder sb) {
      sb.append('[');
      Term head = p.getArgument(0);
      Term tail = p.getArgument(1);
      write(head, sb);
      Term list;
      while ((list = getList(tail)) != null) {
         sb.append(',');
         write(list.getArgument(0), sb);
         tail = list.getArgument(1);
      }

      if (tail.getType() != TermType.EMPTY_LIST) {
         sb.append('|');
         write(tail, sb);
      }
      sb.append(']');
   }

   private static Term getList(Term t) {
      if (t.getType() == TermType.LIST) {
         return t;
      } else {
         return null;
      }
   }

   private void writePredicate(Term p, StringBuilder sb) {
      if (isInfixOperator(p)) {
         writeInfixOperator(p, sb);
      } else if (isPrefixOperator(p)) {
         writePrefixOperator(p, sb);
      } else if (isPostfixOperator(p)) {
         writePostfixOperator(p, sb);
      } else {
         writeNonOperatorPredicate(p, sb);
      }
   }

   private boolean isInfixOperator(Term t) {
      return t.getType() == TermType.STRUCTURE && t.getArgs().length == 2 && operands.infix(t.getName());
   }

   private void writeInfixOperator(Term p, StringBuilder sb) {
      Term[] args = p.getArgs();
      write(args[0], sb);
      sb.append(' ').append(p.getName()).append(' ');
      // if second argument is an infix operand then add brackets around it so:
      //  ?-(,(fail, ;(fail, true)))
      // appears as:
      //  ?- fail , (fail ; true)
      // not:
      //  ?- fail , fail ; true
      if (isInfixOperator(args[1]) && isEqualOrLowerPriority(p, args[1])) {
         sb.append('(');
         writeInfixOperator(args[1], sb);
         sb.append(')');
      } else {
         write(args[1], sb);
      }
   }

   private boolean isEqualOrLowerPriority(Term p1, Term p2) {
      return operands.getInfixPriority(p1.getName()) <= operands.getInfixPriority(p2.getName());
   }

   private boolean isPrefixOperator(Term t) {
      return t.getType() == TermType.STRUCTURE && t.getArgs().length == 1 && operands.prefix(t.getName());
   }

   private void writePrefixOperator(Term p, StringBuilder sb) {
      sb.append(p.getName()).append(' ');
      write(p.getArgs()[0], sb);
   }

   private boolean isPostfixOperator(Term t) {
      return t.getType() == TermType.STRUCTURE && t.getArgs().length == 1 && operands.postfix(t.getName());
   }

   private void writePostfixOperator(Term p, StringBuilder sb) {
      write(p.getArgs()[0], sb);
      sb.append(' ').append(p.getName());
   }

   private void writeNonOperatorPredicate(Term p, StringBuilder sb) {
      String name = p.getName();
      Term[] args = p.getArgs();
      sb.append(name);
      sb.append("(");
      for (int i = 0; i < args.length; i++) {
         if (i != 0) {
            sb.append(", ");
         }
         write(args[i], sb);
      }
      sb.append(")");
   }
}
