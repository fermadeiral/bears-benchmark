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
package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.decimalFraction;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.Structure;
import org.projog.core.term.Term;
import org.projog.core.term.TermUtils;

public class CalculatablesTest {
   private final KnowledgeBase kb = TestUtils.createKnowledgeBase();
   private final String dummyCalculatableName = "dummy_calculatable";
   private final PredicateKey dummyCalculatableKey = new PredicateKey(dummyCalculatableName, 1);
   private final int dummyTermArgument = 7;
   private final Structure dummyTerm = structure(dummyCalculatableName, integerNumber(dummyTermArgument));

   @Test
   public void testGetNumericIntegerNumber() {
      Calculatables c = createCalculatables();
      IntegerNumber i = integerNumber(1);
      assertSame(i, c.getNumeric(i));
   }

   @Test
   public void testGetNumericDecimalFraction() {
      Calculatables c = createCalculatables();
      DecimalFraction d = decimalFraction(17.6);
      assertSame(d, c.getNumeric(d));
   }

   @Test
   public void testGetNumericException() {
      Calculatables c = createCalculatables();
      try {
         c.getNumeric(variable("X"));
         fail();
      } catch (ProjogException e) {
         assertEquals("Cannot get Numeric for term: X of type: NAMED_VARIABLE", e.getMessage());
      }
   }

   @Test
   public void testGetNumericPredicate() {
      Calculatables c = createCalculatables();

      // try to use calculatable by a name that there is no match for (expect exception)
      try {
         c.getNumeric(dummyTerm);
         fail();
      } catch (ProjogException e) {
         assertEquals("Cannot find calculatable: dummy_calculatable/1", e.getMessage());
      }

      // add new calculatable
      c.addCalculatable(dummyCalculatableKey, DummyCalculatableDefaultConstructor.class.getName());

      // assert that the factory is now using the newly added calculatable
      Numeric n = c.getNumeric(dummyTerm);
      assertSame(IntegerNumber.class, n.getClass());
      assertEquals(dummyTermArgument + 1, n.getLong());
   }

   @Test
   public void testAddExistingCalculatableName() {
      Calculatables c = createCalculatables();

      // add new calculatable class name
      c.addCalculatable(dummyCalculatableKey, DummyCalculatableDefaultConstructor.class.getName());

      // attempt to add calculatable again 
      // (should fail now a calculatable with the same name already exists in the factoty)
      try {
         c.addCalculatable(dummyCalculatableKey, DummyCalculatableDefaultConstructor.class.getName());
         fail("could re-add calculatable named: " + dummyCalculatableName);
      } catch (ProjogException e) {
         // expected;
      }
      try {
         c.addCalculatable(dummyCalculatableKey, new DummyCalculatableDefaultConstructor());
         fail("could re-add calculatable named: " + dummyCalculatableName);
      } catch (ProjogException e) {
         // expected;
      }
   }

   @Test
   public void testAddExistingCalculatableInstance() {
      Calculatables c = createCalculatables();

      // add new calculatable instance
      c.addCalculatable(dummyCalculatableKey, new DummyCalculatableDefaultConstructor());

      // attempt to add calculatable again 
      // (should fail now a calculatable with the same name already exists in the factoty)
      try {
         c.addCalculatable(dummyCalculatableKey, DummyCalculatableDefaultConstructor.class.getName());
         fail("could re-add calculatable named: " + dummyCalculatableName);
      } catch (ProjogException e) {
         // expected;
      }
      try {
         c.addCalculatable(dummyCalculatableKey, new DummyCalculatableDefaultConstructor());
         fail("could re-add calculatable named: " + dummyCalculatableName);
      } catch (ProjogException e) {
         // expected;
      }
   }

   @Test
   public void testAddCalculatableError() {
      Calculatables c = createCalculatables();

      // add new calculatable with invalid name
      c.addCalculatable(dummyCalculatableKey, "an invalid class name");
      try {
         c.getNumeric(dummyTerm);
         fail();
      } catch (RuntimeException e) {
         // expected as specified class name is invalid
         assertEquals("Could not create new Calculatable using: an invalid class name", e.getMessage());
      }
   }

   /** Test using a static method to add a calculatable that does not have a public no arg constructor. */
   @Test
   public void testAddCalculatableUsingStaticMethod() {
      final Calculatables c = createCalculatables();
      final String className = DummyCalculatableNoPublicConstructor.class.getName();
      c.addCalculatable(dummyCalculatableKey, className + "/getInstance");
      Numeric n = c.getNumeric(dummyTerm);
      assertSame(IntegerNumber.class, n.getClass());
      assertEquals(dummyTermArgument * 3, n.getLong());
   }

   private Calculatables createCalculatables() {
      return new Calculatables(kb);
   }

   /** Calculatable used to test that new calculatables can be added to the factory. */
   public static class DummyCalculatableDefaultConstructor implements Calculatable {
      KnowledgeBase kb;

      /**
       * @return an IntegerNumber with a value of the first input argument + 1
       */
      @Override
      public Numeric calculate(Term... args) {
         if (kb == null) {
            // setKnowledgeBase should be called by Calculatables when it creates an instance of this class
            throw new RuntimeException("KnowledgeBase not set on " + this);
         }
         long input = TermUtils.castToNumeric(args[0]).getLong();
         long output = input + 1;
         return new IntegerNumber(output);
      }

      @Override
      public void setKnowledgeBase(KnowledgeBase kb) {
         this.kb = kb;
      }
   }

   /** Calculatable used to test that new calculatables can be created using a static method. */
   public static class DummyCalculatableNoPublicConstructor implements Calculatable {
      KnowledgeBase kb;

      public static DummyCalculatableNoPublicConstructor getInstance() {
         return new DummyCalculatableNoPublicConstructor();
      }

      private DummyCalculatableNoPublicConstructor() {
         // private as want to test creation using getInstance static method
      }

      /**
       * @return an IntegerNumber with a value of the first input argument + 1
       */
      @Override
      public Numeric calculate(Term... args) {
         if (kb == null) {
            // setKnowledgeBase should be called by Calculatables when it creates an instance of this class
            throw new RuntimeException("KnowledgeBase not set on " + this);
         }
         long input = TermUtils.castToNumeric(args[0]).getLong();
         long output = input * 3;
         return new IntegerNumber(output);
      }

      @Override
      public void setKnowledgeBase(KnowledgeBase kb) {
         this.kb = kb;
      }
   }
}
