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
package org.projog.core.parser;

import static org.projog.core.parser.TokenType.SYMBOL;

class Delimiters {
   private static final char ARGUMENT_SEPARATOR = ',';
   private static final char PREDICATE_OPENING_BRACKET = '(';
   private static final char PREDICATE_CLOSING_BRACKET = ')';
   private static final char LIST_OPENING_BRACKET = '[';
   private static final char LIST_CLOSING_BRACKET = ']';
   private static final char LIST_TAIL = '|';
   private static final char PERIOD = '.';

   static boolean isDelimiter(String s) {
      return s != null && s.length() == 1 && isDelimiter(s.charAt(0));
   }

   static boolean isDelimiter(int c) {
      switch (c) {
         case ARGUMENT_SEPARATOR:
         case PREDICATE_OPENING_BRACKET:
         case PREDICATE_CLOSING_BRACKET:
         case LIST_OPENING_BRACKET:
         case LIST_CLOSING_BRACKET:
         case LIST_TAIL:
         case PERIOD:
            return true;
         default:
            return false;
      }
   }

   static boolean isListOpenBracket(int c) {
      return c == LIST_OPENING_BRACKET;
   }

   static boolean isPredicateOpenBracket(Token token) {
      return isMatch(token, PREDICATE_OPENING_BRACKET);
   }

   static boolean isPredicateCloseBracket(Token token) {
      return isMatch(token, PREDICATE_CLOSING_BRACKET);
   }

   static boolean isListOpenBracket(Token token) {
      return isMatch(token, LIST_OPENING_BRACKET);
   }

   static boolean isListCloseBracket(Token token) {
      return isMatch(token, LIST_CLOSING_BRACKET);
   }

   static boolean isListTail(Token token) {
      return isMatch(token, LIST_TAIL);
   }

   static boolean isArgumentSeperator(Token token) {
      return isMatch(token, ARGUMENT_SEPARATOR);
   }

   static boolean isSentenceTerminator(Token token) {
      return isMatch(token, PERIOD);
   }

   private static boolean isMatch(Token token, char expected) {
      return token != null && token.type == SYMBOL && token.value != null && token.value.length() == 1 && token.value.charAt(0) == expected;
   }
}
