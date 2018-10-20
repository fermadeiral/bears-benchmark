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

/**
 * Defines the type of terms supported by Projog.
 * 
 * @see Term#getType()
 */
public enum TermType {
   /** @see Variable */
   NAMED_VARIABLE(false, false, true, 1),
   /** @see DecimalFraction */
   FRACTION(false, true, false, 2),
   /** @see IntegerNumber */
   INTEGER(false, true, false, 3),
   /** @see EmptyList */
   EMPTY_LIST(false, false, false, 4),
   /** @see Atom */
   ATOM(false, false, false, 5),
   /** @see Structure */
   STRUCTURE(true, false, false, 6),
   /** @see List */
   LIST(true, false, false, 6);

   private final boolean isStructure;
   private final boolean isNumeric;
   private final boolean isVariable;
   private final int precedence;

   private TermType(boolean isStructure, boolean isNumeric, boolean isVariable, int precedence) {
      this.isStructure = isStructure;
      this.isNumeric = isNumeric;
      this.isVariable = isVariable;
      this.precedence = precedence;
   }

   /**
    * @return {@code true} if this type represents "compound structure"
    */
   public boolean isStructure() {
      return isStructure;
   }

   /**
    * @return {@code true} if this type represents instances of {@link Numeric}
    */
   public boolean isNumeric() {
      return isNumeric;
   }

   /**
    * @return {@code true} if this type represents a variable
    */
   public boolean isVariable() {
      return isVariable;
   }

   /**
    * Used to consistently order {@link Term}s of different types.
    * 
    * @return precedence of this type
    * @see TermComparator#compare(Term, Term)
    */
   public int getPrecedence() {
      return precedence;
   }
}
