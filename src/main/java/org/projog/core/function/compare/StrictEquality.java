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
package org.projog.core.function.compare;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;

/* TEST
 %FALSE X == Y

 %QUERY X == X
 %ANSWER X=UNINSTANTIATED VARIABLE

 %QUERY X = Y, X == Y, Y = 1
 %ANSWER
 % X=1
 % Y=1
 %ANSWER

 %FALSE X == Y, Y = 1, X = Y

 %FALSE append([A|B],C) == append(X,Y)

 %QUERY append([A|B],C) == append([A|B],C)
 %ANSWER
 % A=UNINSTANTIATED VARIABLE
 % B=UNINSTANTIATED VARIABLE
 % C=UNINSTANTIATED VARIABLE
 %ANSWER
 */
/**
 * <code>X==Y</code> - a strict equality test.
 * <p>
 * If <code>X</code> can be matched with <code>Y</code> the goal succeeds else the goal fails. A <code>X==Y</code> goal
 * will only consider an uninstantiated variable to be equal to another uninstantiated variable that is already sharing
 * with it.
 * </p>
 */
public final class StrictEquality extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term arg1, Term arg2) {
      return arg1.strictEquality(arg2);
   }
}
