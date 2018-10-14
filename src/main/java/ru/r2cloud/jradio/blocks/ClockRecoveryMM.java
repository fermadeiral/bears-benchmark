package ru.r2cloud.jradio.blocks;

import java.io.IOException;

import ru.r2cloud.jradio.Context;
import ru.r2cloud.jradio.FloatInput;
import ru.r2cloud.jradio.util.MathUtils;

public class ClockRecoveryMM implements FloatInput {

	private float omega;
	private float omegaMid;
	private float omegaLim;
	private float gainOmega;
	private float mu;
	private float gainMu;
	private float omegaRelativeLimit;
	private MMSEFIRInterpolator interp;
	private FloatInput source;

	private float[] curBuf = new float[8];
	private float lastSample = 0.0f;

	public ClockRecoveryMM(FloatInput source, float omega, float gainOmega, float mu, float gainMu, float omegaRelativeLimit) {
		super();
		this.gainOmega = gainOmega;
		this.mu = mu;
		this.gainMu = gainMu;
		this.omegaRelativeLimit = omegaRelativeLimit;
		this.source = source;
		// this block decimates the stream
		// however it is unknown in advance the decimation rate
		// put null, to let downstream blocks aware of it
		this.source.getContext().setTotalSamples(null);
		interp = new MMSEFIRInterpolator();
		setOmega(omega);
	}

	@Override
	public float readFloat() throws IOException {
		for (int i = 0; i < curBuf.length; i++) {
			curBuf[i] = source.readFloat();
		}

		float mmVal;
		float result = interp.interpolate(curBuf, mu);

		mmVal = slice(lastSample) * result - slice(result) * lastSample;
		lastSample = result;

		omega = omega + gainOmega * mmVal;
		omega = omegaMid + MathUtils.branchless_clip(omega - omegaMid, omegaLim);
		mu = mu + omega + gainMu * mmVal;

		int skip = (int) Math.floor(mu) - curBuf.length;

		mu = mu - (float) Math.floor(mu);

		for (int i = 0; i < skip; i++) {
			source.readFloat();
		}

		return result;
	}

	private static float slice(float x) {
		return x < 0 ? -1.0F : 1.0F;
	}

	private void setOmega(float omega) {
		this.omega = omega;
		omegaMid = omega;
		omegaLim = round(omegaMid * omegaRelativeLimit);
	}

	private static float round(float f) {
		return (float) Math.round(f * 1000000) / 1000000;
	}

	public float getMu() {
		return mu;
	}

	public void setMu(float mu) {
		this.mu = mu;
	}

	public float getGainMu() {
		return gainMu;
	}

	public void setGainMu(float gainMu) {
		this.gainMu = gainMu;
	}

	public float getOmegaRelativeLimit() {
		return omegaRelativeLimit;
	}

	public void setOmegaRelativeLimit(float omegaRelativeLimit) {
		this.omegaRelativeLimit = omegaRelativeLimit;
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

	@Override
	public Context getContext() {
		return source.getContext();
	}
}
