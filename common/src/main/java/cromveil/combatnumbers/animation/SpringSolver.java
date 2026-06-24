package cromveil.combatnumbers.animation;

public final class SpringSolver {

	private SpringSolver() {
	}

	public static final float DEFAULT_STIFFNESS = 170f;
	public static final float DEFAULT_DAMPING = 26f;

	private static final float SETTLE_THRESHOLD = 0.005f;
	private static final float ZETA_EPSILON = 1e-6f;

	public static float computeMass(float damping, float durationSec) {
		return -damping * durationSec / (2f * (float) Math.log(SETTLE_THRESHOLD));
	}

	public static float evaluateSpring(float x0, float target, float velocity,
			float stiffness, float damping, float mass, float timeSec) {
		float dx0 = x0 - target;
		float k = stiffness;
		float c = damping;
		float m = mass;
		float omega0 = (float) Math.sqrt(k / m);
		float zeta = c / (2f * omega0 * m);

		float x;
		if (zeta < 1f - ZETA_EPSILON) {
			float omegaD = omega0 * (float) Math.sqrt(1f - zeta * zeta);
			float A = dx0;
			float B = (velocity + zeta * omega0 * A) / omegaD;
			float decay = (float) Math.exp(-zeta * omega0 * timeSec);
			x = decay * (A * (float) Math.cos(omegaD * timeSec) + B * (float) Math.sin(omegaD * timeSec));
		} else if (zeta > 1f + ZETA_EPSILON) {
			float omegaZ = omega0 * (float) Math.sqrt(zeta * zeta - 1f);
			float r1 = -zeta * omega0 + omegaZ;
			float r2 = -zeta * omega0 - omegaZ;
			float C1 = (velocity - r2 * dx0) / (r1 - r2);
			float C2 = dx0 - C1;
			x = C1 * (float) Math.exp(r1 * timeSec) + C2 * (float) Math.exp(r2 * timeSec);
		} else {
			float A = dx0;
			float B = velocity + omega0 * A;
			float decay = (float) Math.exp(-omega0 * timeSec);
			x = decay * (A + B * timeSec);
		}

		return target + x;
	}

	public static float[] solveSpring(float x0, float target, float velocity,
			float stiffness, float damping, float durationSec, int sampleRate) {
		int n = sampleCount(durationSec, sampleRate);
		if (n <= 0)
			return new float[0];
		float[] buf = new float[n + 1];
		float m = computeMass(damping, durationSec);
		for (int i = 0; i <= n; i++) {
			float tau = (float) i / sampleRate;
			buf[i] = evaluateSpring(x0, target, velocity, stiffness, damping, m, tau);
		}
		return buf;
	}

	public static int sampleCount(float durationSec, int sampleRate) {
		return (int) (durationSec * sampleRate);
	}
}
