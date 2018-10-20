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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.createKnowledgeBase;
import static org.projog.TestUtils.writeToTempFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.Test;
import org.projog.core.parser.ParserException;
import org.projog.core.udp.DynamicUserDefinedPredicateFactory;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;
import org.projog.core.udp.UserDefinedPredicateFactory;

public class ProjogSourceReaderTest {
   @Test
   public void testParseFileNotFound() {
      File f = new File("does_not_exist");
      try {
         ProjogSourceReader.parseFile(createKnowledgeBase(), f);
         fail();
      } catch (ProjogException e) {
         assertMessageContainsText(e, "Could not read prolog source from file: does_not_exist due to: java.io.FileNotFoundException");
      }
   }

   @Test
   public void testParserException() {
      String message = "While parsing arguments of test_dynamic expected ) or , but got: d";
      String lineWithSyntaxError = "test_dynamic(a,b,c d). % Line 3";
      try {
         File f = writeToFile("test_dynamic(a,b).\n" + "test_dynamic(a,b,c).\n" + lineWithSyntaxError + "\n" + "test_dynamic(a,b,c,d,e).");
         ProjogSourceReader.parseFile(createKnowledgeBase(), f);
         fail();
      } catch (ProjogException e) {
         ParserException p = (ParserException) e.getCause();
         assertEquals(message + " Line: " + lineWithSyntaxError, p.getMessage());
         assertEquals(lineWithSyntaxError, p.getLine());
         assertEquals(3, p.getLineNumber());
         assertEquals(20, p.getColumnNumber());
         assertParserExceptionDescription(p, message, lineWithSyntaxError);
      }
   }

   private void assertParserExceptionDescription(ParserException p, String message, String line) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(os);
      p.getDescription(out);
      out.close();
      String[] lines = os.toString().split("\n");
      assertEquals(message, lines[0].trim());
      assertEquals(line, lines[1].trim());
      assertEquals("^", lines[2].trim());
   }

   @Test
   public void testDynamicKeywordForAlreadyDefinedFunction() {
      try {
         File f = writeToFile("test_dynamic(a,b).\n" + "test_dynamic(a,b,c).\n" + "test_dynamic(a,b,c,d).\n" + "?- dynamic(test_dynamic/3).");
         ProjogSourceReader.parseFile(createKnowledgeBase(), f);
         fail();
      } catch (ProjogException e) {
         assertMessageContainsText(e, "Cannot declare: test_dynamic/3 as a dynamic predicate when it has already been used.");
      }
   }

   @Test
   public void testDynamicKeyword() {
      KnowledgeBase kb = createKnowledgeBase();
      File f = writeToFile("?- dynamic(test_dynamic/3).\n"
                           + "test_dynamic(a,b).\n"
                           + "test_dynamic(a,b,c).\n"
                           + "test_dynamic(x,y,z).\n"
                           + "test_dynamic(q,w,e).\n"
                           + "test_dynamic(a,b,c,d).\n"
                           + "test_dynamic2(1,2,3).\n"
                           + "test_dynamic2(4,5,6).\n"
                           + "test_dynamic2(7,8,9).");
      ProjogSourceReader.parseFile(kb, f);

      assertDynamicUserDefinedPredicate(kb, new PredicateKey("test_dynamic", 3));
      assertClauseModels(kb, new PredicateKey("test_dynamic", 3), "a, b, c", "x, y, z", "q, w, e");

      assertStaticUserDefinedPredicate(kb, new PredicateKey("test_dynamic", 2));
      assertStaticUserDefinedPredicate(kb, new PredicateKey("test_dynamic", 4));
      assertStaticUserDefinedPredicate(kb, new PredicateKey("test_dynamic2", 3));
      assertClauseModels(kb, new PredicateKey("test_dynamic2", 3), "1, 2, 3", "4, 5, 6", "7, 8, 9");
   }

   private void assertDynamicUserDefinedPredicate(KnowledgeBase kb, PredicateKey key) {
      UserDefinedPredicateFactory udp = getUserDefinedPredicate(kb, key);
      assertSame(DynamicUserDefinedPredicateFactory.class, udp.getClass());
   }

   private void assertStaticUserDefinedPredicate(KnowledgeBase kb, PredicateKey key) {
      UserDefinedPredicateFactory udp = getUserDefinedPredicate(kb, key);
      assertSame(StaticUserDefinedPredicateFactory.class, udp.getClass());
   }

   private void assertClauseModels(KnowledgeBase kb, PredicateKey key, String... expectedArgs) {
      UserDefinedPredicateFactory udp = getUserDefinedPredicate(kb, key);
      for (int i = 0; i < expectedArgs.length; i++) {
         String actual = udp.getClauseModel(i).getOriginal().toString();
         String expected = key.getName() + "(" + expectedArgs[i] + ")";
         assertEquals(expected, actual);
      }
      assertNull(udp.getClauseModel(expectedArgs.length));
   }

   private UserDefinedPredicateFactory getUserDefinedPredicate(KnowledgeBase kb, PredicateKey key) {
      PredicateFactory ef = kb.getPredicateFactory(key);
      assertNotNull(ef);
      UserDefinedPredicateFactory udp = kb.createOrReturnUserDefinedPredicate(key);
      assertSame(ef, udp);
      return udp;
   }

   private void assertMessageContainsText(ProjogException e, String text) {
      int i = e.getMessage().indexOf(text);
      assertTrue(i > -1);
   }

   private File writeToFile(String contents) {
      return writeToTempFile(this.getClass(), contents);
   }
}
