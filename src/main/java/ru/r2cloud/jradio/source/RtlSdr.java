package ru.r2cloud.jradio.source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import ru.r2cloud.jradio.Context;
import ru.r2cloud.jradio.FloatInput;

public class RtlSdr implements FloatInput {

	private static final int BUFFER_SIZE = 2048;

	private final InputStream iqStream;
	private final Context context;

	private float[] lookupTable;
	private byte[] buffer;
	private int currentBufIndex = 0;
	private int maxBytes;

	public RtlSdr(InputStream iqStream, float sampleRate) {
		if (iqStream == null) {
			throw new IllegalArgumentException("iqstream cannot be null");
		}
		this.iqStream = iqStream;
		buffer = new byte[BUFFER_SIZE];
		maxBytes = buffer.length;
		lookupTable = new float[0x100];
		for (int i = 0; i < 0x100; ++i) {
			lookupTable[i] = (((i & 0xff)) - 127.4f) * (1.0f / 128.0f);
		}
		context = new Context();
		context.setChannels(2);
		context.setSampleSizeInBits(8);
		context.setSampleRate(sampleRate);
	}

	@Override
	public float readFloat() throws IOException {
		if (currentBufIndex == 0 || currentBufIndex >= maxBytes) {
			currentBufIndex = 0;
			maxBytes = iqStream.read(buffer);
			if (maxBytes == -1) {
				throw new EOFException();
			}
		}
		float result = lookupTable[buffer[currentBufIndex] & 0xFF];
		currentBufIndex++;
		return result;
	}

	@Override
	public void close() throws IOException {
		iqStream.close();
	}

	@Override
	public Context getContext() {
		return context;
	}
}
