package cromveil.combatnumbers.config.screen;

class ScrollTracker {

	private int contentHeight;
	private int visibleHeight;
	private int scrollableHeight;
	private double offset;

	void reconfigure(int contentHeight, int visibleHeight) {
		this.contentHeight = contentHeight;
		this.visibleHeight = visibleHeight;
		this.scrollableHeight = Math.max(0, contentHeight - visibleHeight);
		offset = Math.clamp(offset, -scrollableHeight, 0);
	}

	boolean needsScroll() {
		return scrollableHeight > 0;
	}

	int pixelOffset() {
		return (int) Math.round(offset);
	}

	void add(double delta) {
		offset = Math.clamp(offset + delta, -scrollableHeight, 0);
	}

	double progress() {
		if (scrollableHeight <= 0) return 0;
		return -offset / scrollableHeight;
	}

	int thumbHeight(int trackHeight) {
		if (scrollableHeight <= 0) return trackHeight;
		double ratio = (double) visibleHeight / contentHeight;
		return Math.max(12, (int) (trackHeight * ratio));
	}

	int thumbY(int trackHeight, int thumbHeight) {
		if (scrollableHeight <= 0 || thumbHeight >= trackHeight) return 0;
		return (int) (progress() * (trackHeight - thumbHeight));
	}
}
