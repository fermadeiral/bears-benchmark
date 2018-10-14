package ru.r2cloud.jradio.sink;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;

import ru.r2cloud.jradio.source.WavFileSource;

public class WavFileSinkTest {

	@Test
	public void testStereo() throws IOException, UnsupportedAudioFileException {
		assertWavData("stereo.wav");
	}
	
	@Test
	public void testMono() throws IOException, UnsupportedAudioFileException {
		assertWavData("aausat-4.wav");
	}

	private static void assertWavData(String name) throws IOException, UnsupportedAudioFileException {
		WavFileSource source = new WavFileSource(WavFileSinkTest.class.getClassLoader().getResourceAsStream(name));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WavFileSink.process(source, baos);
		baos.close();
		AudioInputStream ais = AudioSystem.getAudioInputStream(WavFileSinkTest.class.getClassLoader().getResourceAsStream(name));
		AudioInputStream ais2 = AudioSystem.getAudioInputStream(new ByteArrayInputStream(baos.toByteArray()));
		byte[] expected = new byte[ais.getFormat().getFrameSize()];
		byte[] actual = new byte[ais2.getFormat().getFrameSize()];
		while (ais.read(expected, 0, expected.length) != -1 && ais2.read(actual, 0, actual.length) != -1) {
			for (int i = 0; i < expected.length; i++) {
				assertEquals(expected[i], actual[i]);
			}
		}
	}

}
