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
package org.projog.core.function.list;

import static java.util.Collections.sort;
import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toJavaUtilList;
import static org.projog.core.term.TermComparator.TERM_COMPARATOR;

import java.util.Comparator;
import java.util.List;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;

/* TEST
 %QUERY keysort([a - 1,b - 3,c - 2], X)
 %ANSWER X=[a - 1,b - 3,c - 2]
 
 %QUERY keysort([c - 2,a - 1,b - 3], X)
 %ANSWER X=[a - 1,b - 3,c - 2]

 %TRUE keysort([c - 2,a - 1,b - 3], [a - 1,b - 3,c - 2])
 %FALSE keysort([c - 2,a - 1,b - 3], [c - 2,a - 1,b - 3])

 % Duplicates are <i>not</i> removed.
 %QUERY keysort([a - 1,a - 9,a - 1,z - 1, q - 3, z - 1], X)
 %ANSWER X=[a - 1,a - 9,a - 1,q - 3,z - 1,z - 1]

 % Keys are sorted using the standard ordering of terms.
 %QUERY keysort([Variable - v,1.0 - v,1 - v,atom - v, [] - v,structure(a) - v,[list] - v], X)
 %ANSWER
 % X=[Variable - v,1.0 - v,1 - v,[] - v,atom - v,structure(a) - v,[list] - v]
 % Variable=UNINSTANTIATED VARIABLE 
 %ANSWER
 
 %QUERY keysort([[list] - v,structure(a) - v,[] - v,atom - v,1 - v,1.0 - v,Variable - v], X)
 %ANSWER
 % X=[Variable - v,1.0 - v,1 - v,[] - v,atom - v,structure(a) - v,[list] - v]
 % Variable=UNINSTANTIATED VARIABLE 
 %ANSWER
 
 % Both the first and second arguments can contain variables.
 %QUERY keysort([c - Q,a - W,b - E],[R - 1,T - 2,Y - 3])
 %ANSWER
 % Q=3
 % W=1
 % E=2
 % R=a
 % T=b
 % Y=c
 %ANSWER
 */
/**
 * <code>keysort(X,Y)</code> - sorts a list of key/value pairs.
 * <p>
 * Sorts the list <code>X</code>, containing <i>key/value pairs</i>, and attempts to unify the result with
 * <code>Y</code>. Key/value pairs are compound terms with a functor of <code>-</code> and two arguments. The first
 * argument is the <i>key</i> and the second argument is the <i>value</i>. It is the key of the key/value pairs that is
 * used to sort the elements contained in <code>X</code>. (Note: duplicates are <i>not</i> removed.)
 */
public final class KeySort extends AbstractSingletonPredicate {
   private static final String KEY_VALUE_PAIR_FUNCTOR = "-";
   private static final Comparator<Term> KEY_VALUE_PAIR_COMPARATOR = new Comparator<Term>() {
      @Override
      public int compare(Term kvp1, Term kvp2) {
         return TERM_COMPARATOR.compare(kvp1.getArgument(0), kvp2.getArgument(0));
      }
   };

   @Override
   public boolean evaluate(final Term original, final Term result) {
      final List<Term> elements = toJavaUtilList(original);
      if (elements == null) {
         throw new ProjogException("Expected first argument to be a fully instantied list but got: " + original);
      }
      assertKeyValuePairs(elements);
      sort(elements, KEY_VALUE_PAIR_COMPARATOR);
      return result.unify(createList(elements));
   }

   private boolean assertKeyValuePairs(List<Term> elements) {
      for (Term t : elements) {
         if (!assertKeyValuePair(t)) {
            throw new ProjogException("Expected every element of list to be a compound term with a functor of - and two arguments but got: " + t);
         }
      }
      return true;
   }

   private boolean assertKeyValuePair(Term t) {
      return t.getType() == TermType.STRUCTURE && KEY_VALUE_PAIR_FUNCTOR.equals(t.getName()) && t.getNumberOfArguments() == 2;
   }
}
