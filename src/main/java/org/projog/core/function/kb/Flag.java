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
package org.projog.core.function.kb;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;

import java.util.HashMap;
import java.util.Map;

import org.projog.core.Calculatables;
import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.Term;

/* TEST
 %QUERY flag(p(a), X, 2)
 %ANSWER X=0
 %QUERY flag(p(a), X, X*10)
 %ANSWER X=2
 %FALSE flag(p(a), 2, 7)
 %QUERY flag(p(a), X, 5)
 %ANSWER X=20
 %TRUE flag(p(b), 5, 7)
 
 %FALSE flag(p, 1, 1)
 %TRUE flag(p, 0, 1)
 
 %QUERY flag(a(a), X, 25)
 %ANSWER X=0
 %QUERY flag(a(b), X, X+1)
 %ANSWER X=25
 %QUERY flag(a(c), X, X+1)
 %ANSWER X=26
 %FALSE flag(a(d), 26, 33)
 %TRUE flag(a(d), 27, 33)
 */
/**
 * <code>flag(X,Y,Z)</code> - associates a key with a value.
 * <p>
 * The first argument must be an atom or structure. The name and arity of the first argument is used to construct the
 * key. The second argument is the value currently associated with the key. If there is not currently a value associated
 * with the key then it will default to 0. The third argument is the new value to associate with the key. The third
 * argument must be a numeric value.
 */
public final class Flag extends AbstractSingletonPredicate {
   private final Map<PredicateKey, Numeric> flags = new HashMap<>();
   private Calculatables calculatables;

   @Override
   public void init() {
      calculatables = getCalculatables(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(Term key, Term oldValue, Term newValue) {
      PredicateKey pk = PredicateKey.createForTerm(key);
      synchronized (flags) {
         Numeric n = getOrCreate(pk);

         if (oldValue.unify(n)) {
            put(pk, newValue);
            return true;
         } else {
            return false;
         }
      }
   }

   private Numeric getOrCreate(PredicateKey pk) {
      Numeric n = flags.get(pk);
      if (n == null) {
         n = new IntegerNumber(0);
         flags.put(pk, n);
      }
      return n;
   }

   private void put(PredicateKey pk, Term value) {
      flags.put(pk, calculatables.getNumeric(value));
   }
}
