package cromveil.combatnumbers.animation;

import static cromveil.combatnumbers.animation.ComposeMode.REPLACE;

public record ConstantInterpolator(RandomSource source, ComposeMode compose) implements Interpolator {

	public ConstantInterpolator {
		if (compose == null)
			compose = REPLACE;
	}

	public sealed interface RandomSource permits Fixed, Range {
		float resolve(java.util.Random rng);
	}

	public record Fixed(float value) implements RandomSource {
		@Override
		public float resolve(java.util.Random rng) {
			return value;
		}

		@Override
		public String toString() {
			return Float.toString(value);
		}
	}

	public record Range(float min, float max, boolean signed) implements RandomSource {
		public Range {
			if (min > max) {
				float tmp = min;
				min = max;
				max = tmp;
			}
		}

		public Range(float min, float max) {
			this(min, max, false);
		}

		@Override
		public float resolve(java.util.Random rng) {
			float value = min + rng.nextFloat() * (max - min);
			if (signed && rng.nextBoolean())
				value = -value;
			return value;
		}

		@Override
		public String toString() {
			return (signed ? "\u00b1" : "") + min + ".." + max;
		}
	}

	public static ConstantInterpolator fixed(float value, ComposeMode compose) {
		return new ConstantInterpolator(new Fixed(value), compose);
	}

	public static ConstantInterpolator fixed(float value) {
		return fixed(value, REPLACE);
	}

	public static ConstantInterpolator range(float min, float max, boolean signed, ComposeMode compose) {
		return new ConstantInterpolator(new Range(min, max, signed), compose);
	}

	public static ConstantInterpolator range(float min, float max, boolean signed) {
		return range(min, max, signed, REPLACE);
	}

	public static ConstantInterpolator range(float min, float max, ComposeMode compose) {
		return new ConstantInterpolator(new Range(min, max, false), compose);
	}

	public static ConstantInterpolator range(float min, float max) {
		return range(min, max, REPLACE);
	}

	@Override
	public float startValue() {
		if (source instanceof Fixed f)
			return f.value();
		return Float.NaN;
	}

	@Override
	public float endValue() {
		if (source instanceof Fixed f)
			return f.value();
		return Float.NaN;
	}
}
