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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.assertStrictEquality;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.projog.core.ProjogException;

/**
 * @see TermTest
 */
public class VariableTest {
   @Test
   public void testUnassignedVariableMethods() {
      Variable v = new Variable("X");

      assertEquals("X", v.getId());
      assertEquals("X", v.toString());
      assertSame(v, v.getTerm());
      assertTrue(v.strictEquality(v));

      try {
         v.getName();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.getArgs();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.getNumberOfArguments();
         fail();
      } catch (NullPointerException e) {
      }
      try {
         v.getArgument(0);
         fail();
      } catch (NullPointerException e) {
      }
      try {
         TermUtils.castToNumeric(v);
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: NAMED_VARIABLE with value: X", e.getMessage());
      }

      assertTrue(v.unify(v));

      // just check backtrack doesn't throw an exception
      v.backtrack();
   }

   @Test
   public void testUnifyVariables_1() {
      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertStrictEquality(x, y, false);
      assertTrue(x.unify(y));
      assertStrictEquality(x, y, true);
      x.backtrack();
      assertStrictEquality(x, y, false);
   }

   @Test
   public void testUnifyVariables_2() {
      Atom a = atom();
      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertTrue(y.unify(a));
      assertTrue(x.unify(y));
      assertSame(a, x.getTerm());
      x.backtrack();
      assertSame(x, x.getTerm());
      assertSame(a, y.getTerm());
   }

   @Test
   public void testUnifyVariables_3() {
      Atom a = atom();
      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertTrue(x.unify(y));
      assertTrue(y.unify(a));
      assertSame(a, x.getTerm());
   }

   @Test
   public void testVariablesUnifiedToTheSameTerm() {
      Atom a = atom();
      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertStrictEquality(x, y, false);
      assertTrue(x.unify(a));
      assertTrue(y.unify(a));
      assertStrictEquality(x, y, true);
      x.backtrack();
      assertStrictEquality(x, y, false);
      assertSame(x, x.getTerm());
      assertSame(a, y.getTerm());
   }

   @Test
   public void testCopy() {
      Variable v = variable();
      Map<Variable, Variable> sharedVariables = new HashMap<>();
      Term copy = v.copy(sharedVariables);
      assertEquals(1, sharedVariables.size());
      assertSame(copy, sharedVariables.get(v));
      assertFalse(v.strictEquality(copy));
      assertTrue(v.unify(copy));
      assertTrue(v.strictEquality(copy));
   }

   /**
    * Tests that, when {@link Variable#copy(Map)} is called on a variable whose "copy" (contained in the specified Map)
    * is already instantiated, the term the "copy" is instantiated with gets returned rather than the "copy" itself.
    * <p>
    * This behaviour is required for things like
    * {@link org.projog.core.udp.interpreter.InterpretedTailRecursivePredicate} to work.
    */
   @Test
   public void testCopy_2() {
      Variable v = variable();
      Atom a = atom();
      Structure s1 = structure("name", v);
      Structure s2 = structure("name", v);

      Map<Variable, Variable> sharedVariables = new HashMap<>();

      Structure c1 = s1.copy(sharedVariables);
      assertTrue(c1.unify(structure("name", a)));

      Structure c2 = s2.copy(sharedVariables);
      // check that the single argument of the newly copied structure is the atom itself
      // rather than a variable assigned to the atom
      assertSame(a, c2.getArgument(0));
      // check that, while backtracking does affect the first copied structure,
      // it does not alter the second copied structure
      c1.backtrack();
      c2.backtrack();
      assertSame(Variable.class, c1.getArgument(0).getClass());
      assertSame(a, c2.getArgument(0));
   }

   @Test
   public void testIsImmutable() {
      Variable v = new Variable("X");
      assertFalse(v.isImmutable());
      Atom a = atom();
      assertTrue(v.unify(a));
      assertFalse(v.isImmutable());
   }

   @Test
   public void testUnifyAnonymousVariable() {
      Variable v = variable();
      Variable anon = TermUtils.createAnonymousVariable();
      assertTrue(v.unify(anon));
      assertSame(anon, v.getTerm());
   }

   @Test
   public void testVariableChain() {
      final Variable v1 = variable();
      Variable v2 = v1;
      for (int i = 0; i < 10000; i++) {
         Variable tmpVar = variable("V" + i);
         v2.unify(tmpVar);
         v2 = tmpVar;
      }
      Structure t = structure("name", atom("a"), atom("b"), atom("c"));
      assertTrue(v2.unify(t));

      assertSame(t, v1.getTerm());
      assertSame(t, v1.copy(null));
      assertEquals(t.toString(), v1.toString());
      assertSame(t.getName(), v1.getName());
      assertSame(t.getType(), v1.getType());
      assertSame(t.getNumberOfArguments(), v1.getNumberOfArguments());
      assertSame(t.getArgs(), v1.getArgs());
      assertSame(t.getArgument(0), v1.getArgument(0));
      assertTrue(t.strictEquality(v1));
      assertTrue(v1.strictEquality(t));
      assertTrue(v1.strictEquality(v1));
      assertTrue(t.unify(v1));
      assertTrue(v1.unify(t));
      assertFalse(v1.unify(atom()));
      assertFalse(atom().unify(v1));

      v2.backtrack();
      assertSame(v2, v1.getTerm());

      v1.backtrack();
      assertSame(v1, v1.getTerm());
   }

   @Test
   public void testInfiniteTerm() {
      Variable v = variable("X");
      Structure t = structure("name", v);
      assertTrue(v.unify(t));

      try {
         v.getTerm();
         fail();
      } catch (StackOverflowError e) {
      }
      try {
         t.getTerm();
         fail();
      } catch (StackOverflowError e) {
      }
      try {
         v.toString();
         fail();
      } catch (StackOverflowError e) {
      }
      try {
         t.toString();
         fail();
      } catch (StackOverflowError e) {
      }
   }
}
