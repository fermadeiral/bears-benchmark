package ru.r2cloud.jradio.lrpt;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import ru.r2cloud.jradio.PhaseAmbiguityResolver;
import ru.r2cloud.jradio.Tag;
import ru.r2cloud.jradio.blocks.CorrelateAccessCodeTag;
import ru.r2cloud.jradio.blocks.TaggedStreamToPdu;
import ru.r2cloud.jradio.fec.ViterbiSoft;
import ru.r2cloud.jradio.fec.ccsds.Randomize;
import ru.r2cloud.jradio.fec.ccsds.ReedSolomon;
import ru.r2cloud.jradio.fec.ccsds.UncorrectableException;
import ru.r2cloud.jradio.util.Metrics;

public class LRPT implements Iterable<VCDU>, Iterator<VCDU>, Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(LRPT.class);
	private final MetricRegistry registry = Metrics.getRegistry();

	private final TaggedStreamToPdu input;
	private final Counter count;
	private final ViterbiSoft viterbiSoft;
	private final PhaseAmbiguityResolver phaseAmbiguityResolver;
	private final int spacecraftId;

	private VCDU currentVcdu;
	// previous is used for restoring partial packets
	private VCDU previous = null;

	public LRPT(TaggedStreamToPdu input, PhaseAmbiguityResolver phaseAmbiguityResolver, int spacecraftId) {
		this.input = input;
		if (registry != null) {
			count = registry.counter(LRPT.class.getName());
		} else {
			count = null;
		}
		this.viterbiSoft = new ViterbiSoft((byte) 0x4f, (byte) 0x6d, false, VCDU.VITERBI_TAIL_SIZE);
		this.phaseAmbiguityResolver = phaseAmbiguityResolver;
		this.spacecraftId = spacecraftId;
	}

	@Override
	public VCDU next() {
		return currentVcdu;
	}

	@Override
	public boolean hasNext() {
		while (true) {
			try {
				byte[] rawBytes = input.readBytes();
				if (rawBytes != null && rawBytes.length != 0) {
					Tag currentTag = input.getContext().getCurrent();
					phaseAmbiguityResolver.rotateSoft(rawBytes, (Long) currentTag.get(CorrelateAccessCodeTag.ACCESS_CODE));
					byte[] viterbi = viterbiSoft.decode(rawBytes);
					Randomize.shuffle(viterbi);
					try {
						byte[] current = ReedSolomon.decode(viterbi, 4);
						currentVcdu = new VCDU();
						currentVcdu.readExternal(previous, current);
						// reed solomon might pass for array [0,0,0,0,0,0...0]
						// ensure version is not 0 (according to spec it should be 01)
						if (currentVcdu.getVersion() != 1 || (currentVcdu.getPartial() == null && currentVcdu.getPackets().isEmpty())) {
							continue;
						}
						// another check to filter out broken packets
						if (currentVcdu.getId() == null || currentVcdu.getId().getSpacecraftId() != spacecraftId) {
							continue;
						}
						if (previous == null) {
							LOG.info("detected meteor-m image. frame: " + currentVcdu.getCounter());
						}
						if (count != null) {
							count.inc();
						}
						previous = currentVcdu;
						return true;
					} catch (UncorrectableException e) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("unable to decode reed solomon: " + e.getMessage());
						}
						continue;
					}
				} else {
					return false;
				}
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<VCDU> iterator() {
		return this;
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

}
