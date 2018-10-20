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
package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.decimalFraction;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.list;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import org.junit.Test;
import org.projog.core.term.EmptyList;
import org.projog.core.term.Term;

public class PredicateKeyTest {
   @Test
   public void testCanCreate() {
      String name = "abc";
      testCanCreate(atom(name), name, 0);
      testCanCreate(structure(name, atom()), name, 1);
      testCanCreate(structure(name, atom(), integerNumber(), decimalFraction()), name, 3);
   }

   private void testCanCreate(Term t, String name, int numArgs) {
      // test both static factory methods
      PredicateKey k1 = PredicateKey.createForTerm(t);
      Term arity = createArity(name, numArgs);
      PredicateKey k2 = PredicateKey.createFromNameAndArity(arity);

      testCreatedKey(k1, name, numArgs);
      testCreatedKey(k2, name, numArgs);

      // test that two keys instances with same name and number of arguments are considered equal
      testEquals(k1, k2);
   }

   private void testCreatedKey(PredicateKey k, String name, int numArgs) {
      assertEquals(name, k.getName());
      assertEquals(numArgs, k.getNumArgs());
      if (numArgs == 0) {
         assertEquals(name, k.toString());
      } else {
         assertEquals(name + "/" + numArgs, k.toString());
      }
   }

   @Test
   public void testNotEquals() {
      // different named atoms
      testNotEquals(atom("abc"), atom("abC"));
      testNotEquals(atom("abc"), atom("abcd"));

      // atom versus structure
      testNotEquals(atom("abc"), structure("abc", atom("abc")));

      // structures with different names and/or number of arguments
      testNotEquals(structure("abc", atom("a")), structure("xyz", atom("a")));
      testNotEquals(structure("abc", atom("a")), structure("abc", atom("a"), atom("b")));
   }

   private void testNotEquals(Term t1, Term t2) {
      PredicateKey k1 = PredicateKey.createForTerm(t1);
      PredicateKey k2 = PredicateKey.createForTerm(t2);
      assertFalse(k1.equals(k2));
   }

   @Test
   public void testEquals() {
      // structures with same name and number of arguments
      Term t1 = structure("abc", atom("a"), atom("b"), atom("c"));
      Term t2 = structure("abc", integerNumber(), decimalFraction(), variable());
      PredicateKey k1 = PredicateKey.createForTerm(t1);
      PredicateKey k2 = PredicateKey.createForTerm(t2);
      testEquals(k1, k2);
   }

   private void testEquals(PredicateKey k1, PredicateKey k2) {
      assertTrue(k1.equals(k2));
      assertTrue(k2.equals(k1));
      assertTrue(k1.equals(k1));
      assertTrue(k2.equals(k2));
      assertEquals(k1.hashCode(), k2.hashCode());
   }

   @Test
   public void testNotEqualsNonPredicateKey() {
      PredicateKey k1 = PredicateKey.createForTerm(structure());
      String s = "Not an instanceof PredicateKey";
      assertFalse(k1.equals(s));
   }

   @Test
   public void testCannotCreateForTerm() {
      testCannotCreateForTerm(integerNumber());
      testCannotCreateForTerm(decimalFraction());
      testCannotCreateForTerm(variable());
      testCannotCreateForTerm(EmptyList.EMPTY_LIST);
      testCannotCreateForTerm(list(atom(), atom()));
   }

   private void testCannotCreateForTerm(Term t) {
      try {
         PredicateKey k = PredicateKey.createForTerm(t);
         fail("Managed to create: " + k + " for: " + t);
      } catch (ProjogException e) {
         // expected
      }
   }

   @Test
   public void testCannotCreateFromNameAndArity() {
      testCannotCreateFromNameAndArity(integerNumber());
      testCannotCreateFromNameAndArity(atom());
      testCannotCreateFromNameAndArity(variable());
      testCannotCreateFromNameAndArity(structure("\\", atom(), atom()));
      testCannotCreateFromNameAndArity(structure("/", atom(), atom(), atom()));
   }

   private void testCannotCreateFromNameAndArity(Term t) {
      try {
         PredicateKey k = PredicateKey.createFromNameAndArity(t);
         fail("Managed to create: " + k + " for: " + t);
      } catch (ProjogException e) {
         // expected
      }
   }

   @Test
   public void testCompareTo() {
      PredicateKey k = createKey("bcde", 2);

      // equal to self
      assertTrue(k.compareTo(k) == 0);

      // test, if exact same name, ordered on number of arguments
      assertTrue(k.compareTo(createKey("bcde", 1)) > 0);
      assertTrue(k.compareTo(createKey("bcde", 3)) < 0);

      // test greater based on name
      assertTrue(k.compareTo(createKey("bcazzz", 1)) > 0);
      assertTrue(k.compareTo(createKey("a", 1)) > 0);
      assertTrue(k.compareTo(createKey("a", 2)) > 0);
      assertTrue(k.compareTo(createKey("a", 3)) > 0);

      // test lower based on name
      assertTrue(k.compareTo(createKey("bczaaa", 1)) < 0);
      assertTrue(k.compareTo(createKey("z", 1)) < 0);
      assertTrue(k.compareTo(createKey("z", 2)) < 0);
      assertTrue(k.compareTo(createKey("z", 3)) < 0);
   }

   @Test
   public void testIllegalArgumentException() {
      try {
         createKey("x", -1);
         fail();
      } catch (IllegalArgumentException e) {
         // expected
      }
      try {
         createKey("x", Integer.MIN_VALUE);
         fail();
      } catch (IllegalArgumentException e) {
         // expected
      }
   }

   private PredicateKey createKey(String name, int numArgs) {
      return PredicateKey.createFromNameAndArity(createArity(name, numArgs));
   }

   private Term createArity(String name, int numArgs) {
      return structure("/", atom(name), integerNumber(numArgs));
   }
}
