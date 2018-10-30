package org.apache.olingo.jpa.processor.core.test;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class AssertList {
  static public void assertEquals(Object exp, Object act) {
    assertTrue(EqualsBuilder.reflectionEquals(exp, act));
  }
}
