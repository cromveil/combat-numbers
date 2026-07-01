package cromveil.combatnumbers.config.screen;

import java.util.Locale;

public enum SliderFormat {

	INTEGER("%.0f", 0),
	ONE_DECIMAL("%.1f", 1),
	TWO_DECIMALS("%.2f", 2);

	private final String pattern;
	private final int decimalPlaces;

	SliderFormat(String pattern, int decimalPlaces) {
		this.pattern = pattern;
		this.decimalPlaces = decimalPlaces;
	}

	public String format(double value) {
		return String.format(Locale.ROOT, pattern, value);
	}

	public double snap(double value) {
		double factor = Math.pow(10, decimalPlaces);
		return Math.round(value * factor) / factor;
	}
}
