package cromveil.combatnumbers.animation.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import cromveil.combatnumbers.animation.ComposeMode;
import cromveil.combatnumbers.animation.ConstantInterpolator;
import cromveil.combatnumbers.animation.Easing;
import cromveil.combatnumbers.animation.Interpolator;
import cromveil.combatnumbers.animation.KeyframeDef;
import cromveil.combatnumbers.animation.KeyframeInterpolator;
import cromveil.combatnumbers.animation.SpringInterpolator;
import cromveil.combatnumbers.animation.SpringSolver;
import cromveil.combatnumbers.animation.TweenInterpolator;

import java.util.List;
import java.util.stream.Stream;

import static cromveil.combatnumbers.animation.ComposeMode.REPLACE;

public final class InterpolatorCodec {

	private InterpolatorCodec() {
	}

	private static final Codec<List<Float>> FLOAT_LIST = Codec.FLOAT.listOf();

	private static <T> ComposeMode parseCompose(DynamicOps<T> ops, MapLike<T> input) {
		if (input.get("compose") != null) {
			var result = ComposeMode.CODEC.parse(ops, input.get("compose"));
			if (result.result().isPresent())
				return result.result().get();
		}
		return REPLACE;
	}

	private static <T> void encodeCompose(RecordBuilder<T> prefix, DynamicOps<T> ops, ComposeMode mode) {
		if (mode != REPLACE)
			prefix.add("compose", ComposeMode.CODEC.encodeStart(ops, mode));
	}

	static final MapCodec<ConstantInterpolator> CONSTANT_CODEC = new MapCodec<>() {
		@Override
		public <T> RecordBuilder<T> encode(ConstantInterpolator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("type", ops.createString("constant"));
			encodeCompose(prefix, ops, input.compose());
			if (input.source() instanceof ConstantInterpolator.Fixed f) {
				prefix.add("value", Codec.FLOAT.encodeStart(ops, f.value()));
			}
			return prefix;
		}

		@Override
		public <T> DataResult<ConstantInterpolator> decode(DynamicOps<T> ops, MapLike<T> input) {
			ComposeMode compose = parseCompose(ops, input);
			var valResult = Codec.FLOAT.parse(ops, input.get("value"));
			if (valResult.result().isPresent()) {
				return DataResult.success(ConstantInterpolator.fixed(valResult.result().get(), compose));
			}
			return DataResult.error(() -> "constant requires 'value' field");
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"), ops.createString("compose"), ops.createString("value"));
		}
	};

	static final MapCodec<ConstantInterpolator> RANDOM_CODEC = new MapCodec<>() {
		@Override
		public <T> RecordBuilder<T> encode(ConstantInterpolator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("type", ops.createString("random"));
			encodeCompose(prefix, ops, input.compose());
			var range = (ConstantInterpolator.Range) input.source();
			prefix.add("min", Codec.FLOAT.encodeStart(ops, range.min()));
			prefix.add("max", Codec.FLOAT.encodeStart(ops, range.max()));
			if (range.signed())
				prefix.add("signed", Codec.BOOL.encodeStart(ops, true));
			return prefix;
		}

		@Override
		public <T> DataResult<ConstantInterpolator> decode(DynamicOps<T> ops, MapLike<T> input) {
			ComposeMode compose = parseCompose(ops, input);
			var minResult = Codec.FLOAT.parse(ops, input.get("min"));
			var maxResult = Codec.FLOAT.parse(ops, input.get("max"));
			boolean signed = false;
			if (input.get("signed") != null) {
				var sResult = Codec.BOOL.parse(ops, input.get("signed"));
				if (sResult.result().isPresent())
					signed = sResult.result().get();
			}
			if (minResult.result().isPresent() && maxResult.result().isPresent()) {
				return DataResult.success(
						ConstantInterpolator.range(minResult.result().get(), maxResult.result().get(), signed, compose));
			}
			return DataResult.error(() -> "random requires 'min' and 'max' fields");
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"), ops.createString("compose"),
					ops.createString("min"), ops.createString("max"), ops.createString("signed"));
		}
	};

	static final MapCodec<TweenInterpolator> TWEEN_CODEC = new MapCodec<>() {
		@Override
		public <T> RecordBuilder<T> encode(TweenInterpolator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("type", ops.createString("tween"));
			encodeCompose(prefix, ops, input.compose());
			if (!Float.isNaN(input.from()))
				prefix.add("from", Codec.FLOAT.encodeStart(ops, input.from()));
			if (input.toInterpolator() != null) {
				prefix.add("to", INTERPOLATOR_CODEC.encodeStart(ops, input.toInterpolator()));
			} else {
				prefix.add("to", Codec.FLOAT.encodeStart(ops, input.toValue()));
			}
			if (input.easing() != Easing.LINEAR)
				prefix.add("easing", Easing.CODEC.encodeStart(ops, input.easing()));
			return prefix;
		}

		@Override
		public <T> DataResult<TweenInterpolator> decode(DynamicOps<T> ops, MapLike<T> input) {
			ComposeMode compose = parseCompose(ops, input);
			float from = Float.NaN;
			if (input.get("from") != null) {
				var fr = Codec.FLOAT.parse(ops, input.get("from"));
				if (fr.result().isPresent())
					from = fr.result().get();
			}

			float toValue = 0f;
			Interpolator toInterpolator = null;
			if (input.get("to") != null) {
				var toResult = INTERPOLATOR_CODEC.parse(ops, input.get("to"));
				if (toResult.result().isPresent()) {
					Interpolator parsed = toResult.result().get();
					if (parsed instanceof ConstantInterpolator c && c.source() instanceof ConstantInterpolator.Fixed f) {
						toValue = f.value();
					} else {
						toInterpolator = parsed;
					}
				} else {
					var floatResult = Codec.FLOAT.parse(ops, input.get("to"));
					if (floatResult.result().isPresent())
						toValue = floatResult.result().get();
				}
			}

			Easing easing = Easing.LINEAR;
			if (input.get("easing") != null) {
				var eResult = Easing.CODEC.parse(ops, input.get("easing"));
				if (eResult.result().isPresent())
					easing = eResult.result().get();
			}

			return DataResult.success(
					toInterpolator != null
							? TweenInterpolator.toInterpolator(from, toInterpolator, easing, compose)
							: TweenInterpolator.toValue(from, toValue, easing, compose));
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"), ops.createString("compose"),
					ops.createString("from"), ops.createString("to"), ops.createString("easing"));
		}
	};

	private static final Codec<KeyframeDef> KEYFRAME_DEF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.FLOAT.fieldOf("time").forGetter(KeyframeDef::time),
			Codec.FLOAT.fieldOf("value").forGetter(KeyframeDef::value),
			Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(KeyframeDef::easing))
			.apply(inst, KeyframeDef::new));

	static final MapCodec<KeyframeInterpolator> KEYFRAME_CODEC = new MapCodec<>() {
		@Override
		public <T> RecordBuilder<T> encode(KeyframeInterpolator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("type", ops.createString("keyframe"));
			encodeCompose(prefix, ops, input.compose());
			prefix.add("keyframes", KEYFRAME_DEF_CODEC.listOf().encodeStart(ops, input.keyframes()));
			if (input.fallbackEasing() != Easing.LINEAR)
				prefix.add("easing", Easing.CODEC.encodeStart(ops, input.fallbackEasing()));
			return prefix;
		}

		@Override
		public <T> DataResult<KeyframeInterpolator> decode(DynamicOps<T> ops, MapLike<T> input) {
			ComposeMode compose = parseCompose(ops, input);

			Easing fallback = Easing.LINEAR;
			List<Easing> perSegmentEasings = List.of();
			if (input.get("easing") != null) {
				var listResult = Easing.LIST_CODEC.parse(ops, input.get("easing"));
				if (listResult.result().isPresent()) {
					perSegmentEasings = listResult.result().get();
				} else {
					var eResult = Easing.CODEC.parse(ops, input.get("easing"));
					if (eResult.result().isPresent())
						fallback = eResult.result().get();
				}
			}

			if (input.get("keyframes") != null) {
				var kfResult = KEYFRAME_DEF_CODEC.listOf().parse(ops, input.get("keyframes"));
				if (kfResult.result().isPresent()) {
					return DataResult.success(
							new KeyframeInterpolator(kfResult.result().get(), fallback, compose));
				}
			}

			if (input.get("values") != null) {
				var valsResult = FLOAT_LIST.parse(ops, input.get("values"));
				if (valsResult.result().isEmpty())
					return DataResult.error(() -> "keyframe 'values' must be an array of numbers");
				var valsList = valsResult.result().get();
				float[] values = new float[valsList.size()];
				for (int i = 0; i < valsList.size(); i++)
					values[i] = valsList.get(i);

				float[] times = null;
				if (input.get("times") != null) {
					var timesResult = FLOAT_LIST.parse(ops, input.get("times"));
					if (timesResult.result().isPresent()) {
						var timesList = timesResult.result().get();
						times = new float[timesList.size()];
						for (int i = 0; i < timesList.size(); i++)
							times[i] = timesList.get(i);
					}
				}
				return DataResult.success(
						KeyframeInterpolator.fromValues(values, times, perSegmentEasings, fallback, compose));
			}

			return DataResult.error(
					() -> "keyframe requires 'keyframes' (array of objects) or 'values' (array of numbers)");
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"), ops.createString("compose"),
					ops.createString("keyframes"), ops.createString("values"),
					ops.createString("times"), ops.createString("easing"));
		}
	};

	static final MapCodec<SpringInterpolator> SPRING_CODEC = new MapCodec<>() {
		@Override
		public <T> RecordBuilder<T> encode(SpringInterpolator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("type", ops.createString("spring"));
			encodeCompose(prefix, ops, input.compose());
			if (!Float.isNaN(input.from()))
				prefix.add("from", Codec.FLOAT.encodeStart(ops, input.from()));
			prefix.add("to", Codec.FLOAT.encodeStart(ops, input.to()));
			prefix.add("velocity", Codec.FLOAT.encodeStart(ops, input.velocity()));
			if (input.stiffness() != SpringSolver.DEFAULT_STIFFNESS)
				prefix.add("stiffness", Codec.FLOAT.encodeStart(ops, input.stiffness()));
			if (input.damping() != SpringSolver.DEFAULT_DAMPING)
				prefix.add("damping", Codec.FLOAT.encodeStart(ops, input.damping()));
			return prefix;
		}

		@Override
		public <T> DataResult<SpringInterpolator> decode(DynamicOps<T> ops, MapLike<T> input) {
			ComposeMode compose = parseCompose(ops, input);
			float from = Float.NaN;
			if (input.get("from") != null) {
				var fr = Codec.FLOAT.parse(ops, input.get("from"));
				if (fr.result().isPresent())
					from = fr.result().get();
			}
			float to = 0f;
			if (input.get("to") != null) {
				var tr = Codec.FLOAT.parse(ops, input.get("to"));
				if (tr.result().isPresent())
					to = tr.result().get();
			}
			float velocity = 0f;
			if (input.get("velocity") != null) {
				var vr = Codec.FLOAT.parse(ops, input.get("velocity"));
				if (vr.result().isPresent())
					velocity = vr.result().get();
			}
			float stiffness = SpringSolver.DEFAULT_STIFFNESS;
			if (input.get("stiffness") != null) {
				var sr = Codec.FLOAT.parse(ops, input.get("stiffness"));
				if (sr.result().isPresent())
					stiffness = sr.result().get();
			}
			float damping = SpringSolver.DEFAULT_DAMPING;
			if (input.get("damping") != null) {
				var dr = Codec.FLOAT.parse(ops, input.get("damping"));
				if (dr.result().isPresent())
					damping = dr.result().get();
			}
			return DataResult.success(new SpringInterpolator(from, to, velocity, stiffness, damping, compose));
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"), ops.createString("compose"),
					ops.createString("from"), ops.createString("to"), ops.createString("velocity"),
					ops.createString("stiffness"), ops.createString("damping"));
		}
	};

	public static final Codec<Interpolator> INTERPOLATOR_CODEC = Codec.STRING.dispatch(
			InterpolatorCodec::typeName,
			InterpolatorCodec::codecFor);

	private static String typeName(Interpolator ip) {
		return switch (ip) {
			case ConstantInterpolator c -> c.source() instanceof ConstantInterpolator.Fixed ? "constant" : "random";
			case TweenInterpolator _ -> "tween";
			case KeyframeInterpolator _ -> "keyframe";
			case SpringInterpolator _ -> "spring";
		};
	}

	private static MapCodec<? extends Interpolator> codecFor(String type) {
		return switch (type) {
			case "constant" -> CONSTANT_CODEC;
			case "random" -> RANDOM_CODEC;
			case "tween" -> TWEEN_CODEC;
			case "keyframe" -> KEYFRAME_CODEC;
			case "spring" -> SPRING_CODEC;
			default -> throw new IllegalArgumentException("Unknown interpolator type: " + type);
		};
	}
}
