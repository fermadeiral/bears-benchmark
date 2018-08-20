package dk.alexandra.fresco.tools.mascot.bit;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestBitConverter extends NetworkedTest {

  FieldElementPrg getJointPrg(int prgSeedLength) {
    return new FieldElementPrgImpl(new StrictBitVector(prgSeedLength));
  }

  public List<FieldElement> runConvertToBits(MascotTestContext ctx, FieldElement macKeyShare,
      List<FieldElement> randomValues) {
    FieldElementPrg prg = getJointPrg(ctx.getPrgSeedLength());
    ElementGeneration elementGeneration = new ElementGeneration(ctx.getResourcePool(),
        ctx.getNetwork(), macKeyShare, prg);
    OnlinePhase onlinePhase = new OnlinePhase(ctx.getResourcePool(), ctx.getNetwork(),
        new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(),
            elementGeneration, prg),
        elementGeneration, macKeyShare);
    BitConverter bitConverter = new BitConverter(ctx.getResourcePool(), ctx.getNetwork(),
        elementGeneration, onlinePhase, macKeyShare);
    List<AuthenticatedElement> closed = (ctx.getMyId() == 1) ? elementGeneration.input(randomValues)
        : elementGeneration.input(1, randomValues.size());
    List<AuthenticatedElement> bits = bitConverter.convertToBits(closed);
    List<FieldElement> opened = elementGeneration.open(bits);
    elementGeneration.check(bits, opened);
    return opened;
  }

  @Test
  public void testTwoPartiesBatchedConvertToBits() {
    initContexts(Arrays.asList(1, 2));

    // left party mac key share
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);

    // right party mac key share
    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

    // party one inputs
    List<FieldElement> randomValues =
        MascotTestUtils.generateSingleRow(new int[]{12, 11, 1, 2}, modulus, modBitLength);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask =
        () -> runConvertToBits(contexts.get(1), macKeyShareOne, randomValues);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runConvertToBits(contexts.get(2), macKeyShareTwo, randomValues);

    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    List<FieldElement> partyOneOutput = results.get(0);
    List<FieldElement> partyTwoOutput = results.get(1);

    // outputs should be same
    CustomAsserts.assertEquals(partyOneOutput, partyTwoOutput);

    // outputs should be bits
    for (FieldElement actualBit : partyOneOutput) {
      // compute b * (1 - b), which is 0 iff b is a bit
      FieldElement bitCheck = actualBit
          .multiply(new FieldElement(1, modulus, modBitLength).subtract(actualBit));
      assertEquals("Not a bit " + actualBit, true, bitCheck.isZero());
    }
  }

}
