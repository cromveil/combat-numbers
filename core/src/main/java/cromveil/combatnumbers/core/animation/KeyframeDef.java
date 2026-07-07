package cromveil.combatnumbers.core.animation;

public record KeyframeDef(float time, float value, Easing easing) {

	public KeyframeDef {
		if (easing == null)
			easing = Easing.LINEAR;
	}

	public KeyframeDef(float time, float value) {
		this(time, value, Easing.LINEAR);
	}
}
