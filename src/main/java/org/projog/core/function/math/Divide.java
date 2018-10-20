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
package org.projog.core.function.math;

import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.TermType;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>/</code> - performs division.
 */
public final class Divide extends AbstractCalculatable {
   @Override
   public Numeric calculate(Numeric n1, Numeric n2) {
      if (containsFraction(n1, n2)) {
         return divideFractions(n1, n2);
      } else {
         long dividend = n1.getLong();
         long divisor = n2.getLong();
         if (dividend % divisor == 0) {
            // e.g. 6 / 2 = 3
            return new IntegerNumber(dividend / divisor);
         } else {
            // e.g. 7 / 2 = 3.5
            return divideFractions(n1, n2);
         }
      }
   }

   private static boolean containsFraction(Numeric n1, Numeric n2) {
      return n1.getType() == TermType.FRACTION || n2.getType() == TermType.FRACTION;
   }

   private DecimalFraction divideFractions(Numeric n1, Numeric n2) {
      return new DecimalFraction(n1.getDouble() / n2.getDouble());
   }
}
