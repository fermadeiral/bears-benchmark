package ru.r2cloud.jradio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import ru.r2cloud.jradio.aausat4.AAUSAT4;
import ru.r2cloud.jradio.aausat4.AAUSAT4Beacon;
import ru.r2cloud.jradio.blocks.ClockRecoveryMM;
import ru.r2cloud.jradio.blocks.CorrelateAccessCodeTag;
import ru.r2cloud.jradio.blocks.FixedLengthTagger;
import ru.r2cloud.jradio.blocks.FloatToChar;
import ru.r2cloud.jradio.blocks.Rail;
import ru.r2cloud.jradio.blocks.TaggedStreamToPdu;
import ru.r2cloud.jradio.csp.Priority;
import ru.r2cloud.jradio.source.WavFileSource;
import ru.r2cloud.jradio.source.WavFileSourceTest;
import ru.r2cloud.jradio.util.RepeatedWavSource;
import ru.r2cloud.jradio.util.ThroughputStream;

public class AAUSAT4Test {

	private AAUSAT4 input;

	public static void main(String[] args) throws Exception {
		final ThroughputStream throughputStream = new ThroughputStream(new RepeatedWavSource("aausat-4.wav", 1));
		ClockRecoveryMM clockRecovery = new ClockRecoveryMM(throughputStream, 20.0f, (float) (0.25 * 0.175 * 0.175), 0.005f, 0.175f, 0.005f);
		Rail rail = new Rail(clockRecovery, -1.0f, 1.0f);
		FloatToChar f2char = new FloatToChar(rail, 127.0f);
		CorrelateAccessCodeTag correlateTag = new CorrelateAccessCodeTag(f2char, 10, "010011110101101000110100010000110101010101000010", true);
		AAUSAT4 input = new AAUSAT4(new TaggedStreamToPdu(new FixedLengthTagger(correlateTag, AAUSAT4.VITERBI_TAIL_SIZE + 8))); // 8 for fsm
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println(throughputStream.getCurrent() + " bytes/s");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		t.start();
		while (input.hasNext()) {
			try {
				input.next();
			} catch (Exception e) {
				// do nothing
			}
		}
		input.close();
		t.interrupt();
		
		System.out.println("average " + throughputStream.getAverage() + " bytes/s");
		// average 17697814 bytes/s
	}

	@Test
	public void testSuccess() throws Exception {
		WavFileSource source = new WavFileSource(WavFileSourceTest.class.getClassLoader().getResourceAsStream("aausat-4.wav"));
		ClockRecoveryMM clockRecovery = new ClockRecoveryMM(source, 20.0f, (float) (0.25 * 0.175 * 0.175), 0.005f, 0.175f, 0.005f);
		Rail rail = new Rail(clockRecovery, -1.0f, 1.0f);
		FloatToChar f2char = new FloatToChar(rail, 127.0f);
		CorrelateAccessCodeTag correlateTag = new CorrelateAccessCodeTag(f2char, 10, "010011110101101000110100010000110101010101000010", true);
		input = new AAUSAT4(new TaggedStreamToPdu(new FixedLengthTagger(correlateTag, AAUSAT4.VITERBI_TAIL_SIZE + 8))); // 8 for fsm
		assertTrue(input.hasNext());
		AAUSAT4Beacon beacon = input.next();
		assertEquals(4, beacon.getCom().getBootCount());
		assertEquals(62, beacon.getCom().getPacketsReceived());
		assertEquals(568, beacon.getCom().getPacketsSend());
		assertEquals(-91, beacon.getCom().getLatestRssi());
		assertEquals(24, beacon.getCom().getLatestBitCorrection());
		assertEquals(0, beacon.getCom().getLatestByteCorrection());
		assertEquals(3, beacon.getEps().getBootCount());
		assertEquals(21524, beacon.getEps().getUptime());
		assertEquals(7960, beacon.getEps().getBatteryVoltage());
		assertEquals(-8, beacon.getEps().getCellDiff());
		assertEquals(-80, beacon.getEps().getBatteryCurrent());
		assertEquals(2540, beacon.getEps().getSolarPower());
		assertEquals(16, beacon.getEps().getTemperature());
		assertEquals(17, beacon.getEps().getPaTemperature());
		assertEquals(0, beacon.getAdcs1().getState());
		assertEquals(4411, beacon.getAdcs1().getBdot1());
		assertEquals(835, beacon.getAdcs1().getBdot2());
		assertEquals(-2278, beacon.getAdcs1().getBdot3());
		assertEquals(Priority.CSP_PRIO_CRITICAL, beacon.getPriority());
		assertEquals(343, beacon.getAis2().getBootCount());
		assertEquals(65535, beacon.getAis2().getUniqueMssi());
	}

	@After
	public void stop() throws Exception {
		if (input != null) {
			input.close();
		}
	}
}
