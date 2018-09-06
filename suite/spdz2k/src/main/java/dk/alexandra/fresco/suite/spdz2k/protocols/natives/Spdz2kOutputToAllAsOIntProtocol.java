package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.List;

/**
 * Native protocol for opening a secret value to all parties.
 */
public class Spdz2kOutputToAllAsOIntProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<OInt, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private OInt opened;
  private Spdz2kSInt<PlainT> authenticatedElement;

  /**
   * Creates new {@link Spdz2kOutputToAllAsOIntProtocol}.
   *
   * @param share value to open
   */
  public Spdz2kOutputToAllAsOIntProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    OpenedValueStore<Spdz2kSInt<PlainT>, PlainT> openedValueStore = resourcePool
        .getOpenedValueStore();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      authenticatedElement = factory.toSpdz2kSInt(share);
      network.sendToAll(authenticatedElement.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      PlainT recombined = UInt.sum(shares);
      openedValueStore.pushOpenedValue(authenticatedElement, recombined);
      this.opened = recombined;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public OInt out() {
    return opened;
  }

}
