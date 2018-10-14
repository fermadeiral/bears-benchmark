package ru.r2cloud.jradio;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import ru.r2cloud.jradio.blocks.BinarySlicer;
import ru.r2cloud.jradio.blocks.ClockRecoveryMM;
import ru.r2cloud.jradio.blocks.CorrelateAccessCodeTag;
import ru.r2cloud.jradio.blocks.FixedLengthTagger;
import ru.r2cloud.jradio.source.WavFileSource;
import ru.r2cloud.jradio.source.WavFileSourceTest;

public class FixedLengthTaggerTest {

	private FixedLengthTagger source;

	@Test
	public void success() throws Exception {
		source = new FixedLengthTagger(new CorrelateAccessCodeTag(new BinarySlicer(new ClockRecoveryMM(new WavFileSource(WavFileSourceTest.class.getClassLoader().getResourceAsStream("aausat-4.wav")), 20.0f, (float) (0.25 * 0.175 * 0.175), 0.005f, 0.175f, 0.005f)), 8, "010011110101101000110100010000110101010101000010"), 2008);
		try (InputStream is = BinarySlicerTest.class.getClassLoader().getResourceAsStream("FixedLengthTagger.bin")) {
			int expected = -1;
			while( (expected = is.read()) != -1 ) {
				byte actual = source.readByte();
				assertEquals(expected, actual);
			}
		}
		Map<String, Tag> tags = source.getContext().getTags();
		assertEquals(1, tags.size());
	}

	@After
	public void stop() throws Exception {
		if (source != null) {
			source.close();
		}
	}
}
