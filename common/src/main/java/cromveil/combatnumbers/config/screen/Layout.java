package cromveil.combatnumbers.config.screen;

final class Layout {

	static final int ROW_HEIGHT = 26;
	static final int LABEL_WIDTH = 130;
	static final int CONTENT_START_Y = 35;
	static final int RESET_BTN_WIDTH = 18;
	static final int GAP = 8;
	static final int PADDING = 10;
	static final int BOTTOM_MARGIN = 40;
	static final int WIDGET_HEIGHT = 20;
	static final int BOTTOM_BTN_WIDTH = 100;

	private Layout() {}

	static int labelX() {
		return PADDING;
	}

	static int widgetX() {
		return PADDING + LABEL_WIDTH + GAP;
	}

	static int resetX(int screenWidth) {
		return screenWidth - PADDING - RESET_BTN_WIDTH;
	}

	static int widgetEndX(int screenWidth) {
		return resetX(screenWidth) - GAP;
	}

	static int widgetWidth(int screenWidth) {
		return widgetEndX(screenWidth) - widgetX();
	}

	static int rowY(int index, int scrollOffset) {
		return CONTENT_START_Y + index * ROW_HEIGHT + scrollOffset;
	}

	static int labelY(int rowY) {
		return rowY + 4;
	}

	static int contentHeight(int optionCount) {
		return optionCount * ROW_HEIGHT;
	}

	static int availableHeight(int screenHeight) {
		return screenHeight - CONTENT_START_Y - BOTTOM_MARGIN;
	}

	static int titleY() {
		return 15;
	}

	static int scissorTop() {
		return CONTENT_START_Y;
	}

	static int scissorBottom(int screenHeight) {
		return screenHeight - BOTTOM_MARGIN;
	}

	static int scrollbarLeft(int screenWidth) {
		return screenWidth - 8;
	}

	static int scrollbarRight(int screenWidth) {
		return screenWidth - 4;
	}

	static int scrollbarTop() {
		return CONTENT_START_Y - 5;
	}

	static int scrollbarBottom(int screenHeight) {
		return screenHeight - BOTTOM_MARGIN - 5;
	}

	static int buttonY(int screenHeight) {
		return screenHeight - 35;
	}

	static int saveLeft(int screenWidth) {
		return screenWidth / 2 - 155;
	}

	static int cancelLeft(int screenWidth) {
		return screenWidth / 2 - 50;
	}

	static int resetAllLeft(int screenWidth) {
		return screenWidth / 2 + 55;
	}
}
