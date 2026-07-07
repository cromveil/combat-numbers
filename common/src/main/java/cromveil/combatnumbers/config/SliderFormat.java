package cromveil.combatnumbers.config;

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

	public static SliderFormat fromDecimalPlaces(int decimalPlaces) {
		return switch (decimalPlaces) {
			case 0 -> INTEGER;
			case 1 -> ONE_DECIMAL;
			case 2 -> TWO_DECIMALS;
			default -> throw new IllegalArgumentException("Unsupported decimal places: " + decimalPlaces);
		};
	}

	public String format(double value) {
		return String.format(Locale.ROOT, pattern, value);
	}

	public double snap(double value) {
		double factor = Math.pow(10, decimalPlaces);
		return Math.round(value * factor) / factor;
	}
}
