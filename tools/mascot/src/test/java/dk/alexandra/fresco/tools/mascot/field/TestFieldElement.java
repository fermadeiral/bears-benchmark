package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;

import org.junit.Test;

public class TestFieldElement {

  private final BigInteger modulus = new BigInteger("251");
  private final int bitLength = 8;

  // Positive tests

  @Test
  public void testFieldElementConstructors() {
    FieldElement elOne = new FieldElement(new BigInteger("11"), modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement elThree = new FieldElement(elTwo);
    FieldElement elFour = new FieldElement("11", "251", bitLength);
    CustomAsserts.assertEquals(elOne, elTwo);
    CustomAsserts.assertEquals(elOne, elThree);
    CustomAsserts.assertEquals(elOne, elFour);
  }

  @Test
  public void testAdd() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(33, modulus, bitLength);
    CustomAsserts.assertEquals(expected, elOne.add(elTwo));
  }

  @Test
  public void testPow() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement expected = new FieldElement(233, modulus, bitLength);
    CustomAsserts.assertEquals(expected, elOne.pow(2));
  }

  @Test
  public void testSubtract() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(11, modulus, bitLength);
    CustomAsserts.assertEquals(expected, elOne.subtract(elTwo));
  }

  @Test
  public void testMultiply() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(242, modulus, bitLength);
    CustomAsserts.assertEquals(expected, elOne.multiply(elTwo));
  }

  @Test
  public void testNegate() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement expected = new FieldElement(229, modulus, bitLength);
    CustomAsserts.assertEquals(expected, elOne.negate());
  }

  @Test
  public void testIsZero() {
    FieldElement zero = new FieldElement(0, modulus, bitLength);
    FieldElement notZero = new FieldElement(1, modulus, bitLength);
    assertTrue(zero.isZero());
    assertFalse(notZero.isZero());
  }
  
  @Test
  public void testToBigInteger() {
    assertEquals(new BigInteger("22"), new FieldElement(22, modulus, bitLength).toBigInteger());
  }

  @Test
  public void testGetBit() {
    assertEquals(false, new FieldElement(22, modulus, bitLength).getBit(0));
    assertEquals(true, new FieldElement(22, modulus, bitLength).getBit(1));
  }

  @Test
  public void testSelect() {
    FieldElement el = new FieldElement(22, modulus, bitLength);
    CustomAsserts.assertEquals(el, el.select(true));
    CustomAsserts.assertEquals(new FieldElement(BigInteger.ZERO, modulus, bitLength),
        el.select(false));
  }

  @Test
  public void testConvertToBitVectorSingleByte() {
    FieldElement el = new FieldElement("11", "251", 8);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesSmall() {
    FieldElement el = new FieldElement("11", "65521", 16);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0x0, (byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesLarge() {
    FieldElement el = new FieldElement("65520", "65521", 16);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xFF, (byte) 0xF0};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorAndBack() {
    FieldElement el = new FieldElement("777", "65521", 16);
    StrictBitVector bv = el.toBitVector();
    FieldElement actual = new FieldElement(bv.toByteArray(), new BigInteger("65521"), 16);
    CustomAsserts.assertEquals(el, actual);
  }

  @Test
  public void testGetters() {
    FieldElement el = new FieldElement("777", "65521", 16);
    assertEquals(new BigInteger("65521"), el.getModulus());
    assertEquals(16, el.getBitLength());
  }

  @Test
  public void testToString() {
    FieldElement el = new FieldElement("777", "65521", 16);
    assertEquals("FieldElement [value=777, modulus=65521, bitLength=16]", el.toString());
  }

  // Negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLength() {
    new FieldElement(111, modulus, 7);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegative() {
    new FieldElement(-111, modulus, 8);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegativeMod() {
    new FieldElement(111, BigInteger.valueOf(-251), 8);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLengthMismatch() {
    new FieldElement(111, BigInteger.valueOf(1111), 8);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckValueTooLarge() {
    new FieldElement(252, BigInteger.valueOf(251), 8);
  }

}
