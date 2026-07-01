package cromveil.combatnumbers.config.screen;

public enum SliderFormat {

	INTEGER("%.0f"),
	ONE_DECIMAL("%.1f"),
	TWO_DECIMALS("%.2f");

	private final String pattern;

	SliderFormat(String pattern) {
		this.pattern = pattern;
	}

	public String format(float value) {
		return String.format(pattern, value);
	}
}
