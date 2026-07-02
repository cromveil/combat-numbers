package cromveil.combatnumbers.config.screen;

import cromveil.combatnumbers.config.ConfigStore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {

	private final Screen parent;
	private final String prefix;
	private final Supplier<List<ConfigOption<?>>> optionsSupplier;
	private final ConfigStore store;

	private List<ConfigOption<?>> options;

	private final ScrollTracker scroll = new ScrollTracker();
	private final WidgetFactory widgetFactory;
	private final List<AbstractWidget> rowWidgets = new ArrayList<>();
	private final List<Button> resetButtons = new ArrayList<>();
	private final List<FormattedCharSequence> rowLabels = new ArrayList<>();

	private Button saveBtn;
	private Button cancelBtn;
	private Button resetAllBtn;

	public ConfigScreen(Screen parent, Component title, String prefix,
			Supplier<List<ConfigOption<?>>> optionsSupplier, ConfigStore store) {
		super(title);
		this.parent = parent;
		this.prefix = prefix;
		this.optionsSupplier = optionsSupplier;
		this.store = store;
		this.widgetFactory = new WidgetFactory(font);
	}

	@Override
	protected void init() {
		clearWidgets();
		rowWidgets.clear();
		resetButtons.clear();

		options = List.copyOf(optionsSupplier.get());
		for (ConfigOption<?> opt : options) {
			opt.load();
		}

		scroll.reconfigure(Layout.contentHeight(options.size()), Layout.availableHeight(height));

		rowLabels.clear();
		for (ConfigOption<?> opt : options) {
			rowLabels.add(opt.label(prefix).getVisualOrderText());
		}

		buildWidgets();
	}

	private void buildWidgets() {
		int widgetX = Layout.widgetX();
		int widgetW = Layout.widgetWidth(width);

		for (int i = 0; i < options.size(); i++) {
			ConfigOption<?> opt = options.get(i);
			int y = Layout.rowY(i, scroll.pixelOffset());
			int resetX = Layout.resetX(width);

			Button resetBtn = widgetFactory.makeResetButton(resetX, y, () -> {
				opt.reset();
				rebuild();
			});
			resetBtn.active = !opt.isAtDefault();
			resetBtn.setTooltip(Tooltip.create(
					Component.translatable(prefix + ".option.reset.tooltip")));

			AbstractWidget[] wRef = new AbstractWidget[1];
			Runnable onChanged = () -> {
				resetBtn.active = !opt.isAtDefault();
				var descFn = opt.description();
				if (descFn != null) {
					Component descComp = descFn.apply(opt.get());
					if (descComp != null) {
						wRef[0].setTooltip(Tooltip.create(descComp));
					}
				}
			};

			AbstractWidget w = widgetFactory.create(opt, widgetX, y, widgetW, onChanged);
			wRef[0] = w;
			var descFn = opt.description();
			if (descFn != null) {
				Component descComp = descFn.apply(opt.get());
				if (descComp != null) {
					w.setTooltip(Tooltip.create(descComp));
				}
			} else {
				w.setTooltip(Tooltip.create(opt.tooltip(prefix)));
			}
			addRenderableWidget(w);
			rowWidgets.add(w);

			addRenderableWidget(resetBtn);
			resetButtons.add(resetBtn);
		}

		int btnY = Layout.buttonY(height);
		saveBtn = Button.builder(Component.translatable("config.combatnumbers.save"), btn -> {
			for (ConfigOption<?> opt : options) {
				opt.save();
			}
			store.save();
			onClose();
		}).bounds(Layout.saveLeft(width), btnY, Layout.BOTTOM_BTN_WIDTH, Layout.WIDGET_HEIGHT).build();
		addRenderableWidget(saveBtn);

		cancelBtn = Button.builder(Component.translatable("gui.cancel"), btn -> onClose())
				.bounds(Layout.cancelLeft(width), btnY, Layout.BOTTOM_BTN_WIDTH, Layout.WIDGET_HEIGHT).build();
		addRenderableWidget(cancelBtn);

		resetAllBtn = Button.builder(Component.translatable("config.combatnumbers.resetAll"), btn -> {
			for (ConfigOption<?> opt : options) {
				opt.reset();
			}
			rebuild();
		}).bounds(Layout.resetAllLeft(width), btnY, Layout.BOTTOM_BTN_WIDTH, Layout.WIDGET_HEIGHT).build();
		resetAllBtn.setTooltip(Tooltip.create(
				Component.translatable("config.combatnumbers.resetAll.tooltip")));
		addRenderableWidget(resetAllBtn);
	}

	private void rebuild() {
		clearWidgets();
		rowWidgets.clear();
		resetButtons.clear();
		buildWidgets();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean isDuplicatePress) {
		double mx = event.x();
		double my = event.y();
		if (saveBtn != null && saveBtn.isMouseOver(mx, my)) {
			return saveBtn.mouseClicked(event, isDuplicatePress);
		}
		if (cancelBtn != null && cancelBtn.isMouseOver(mx, my)) {
			return cancelBtn.mouseClicked(event, isDuplicatePress);
		}
		if (resetAllBtn != null && resetAllBtn.isMouseOver(mx, my)) {
			return resetAllBtn.mouseClicked(event, isDuplicatePress);
		}
		return super.mouseClicked(event, isDuplicatePress);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.drawCenteredString(font, title, width / 2, Layout.titleY(), 0xFFFFFFFF);

		int contentTop = Layout.scissorTop();
		int contentBottom = Layout.scissorBottom(height);
		graphics.fill(0, contentTop, width, contentBottom, 0x60000000);

		if (scroll.needsScroll()) {
			graphics.enableScissor(0, contentTop, width, contentBottom);
		}

		for (int i = 0; i < options.size(); i++) {
			int rowY = Layout.rowY(i, scroll.pixelOffset());
			graphics.drawString(font, rowLabels.get(i), Layout.labelX(), Layout.labelY(rowY), 0xFFFFFFFF, true);
		}

		for (AbstractWidget w : rowWidgets) {
			w.render(graphics, mouseX, mouseY, partialTick);
		}
		for (Button b : resetButtons) {
			b.render(graphics, mouseX, mouseY, partialTick);
		}

		if (scroll.needsScroll()) {
			graphics.disableScissor();
		}

		if (saveBtn != null) {
			saveBtn.render(graphics, mouseX, mouseY, partialTick);
		}
		if (cancelBtn != null) {
			cancelBtn.render(graphics, mouseX, mouseY, partialTick);
		}
		if (resetAllBtn != null) {
			resetAllBtn.render(graphics, mouseX, mouseY, partialTick);
		}

		if (scroll.needsScroll()) {
			drawScrollbar(graphics);
		}
	}

	private void drawScrollbar(GuiGraphics graphics) {
		int sbL = Layout.scrollbarLeft(width);
		int sbR = Layout.scrollbarRight(width);
		int sbT = Layout.scrollbarTop();
		int sbB = Layout.scrollbarBottom(height);
		int trackH = sbB - sbT;

		graphics.fill(sbL, sbT, sbR, sbB, 0x30FFFFFF);

		int thumbH = scroll.thumbHeight(trackH);
		int thumbY = sbT + scroll.thumbY(trackH, thumbH);
		graphics.fill(sbL, thumbY, sbR, thumbY + thumbH, 0xA0FFFFFF);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!scroll.needsScroll()) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
		int before = scroll.pixelOffset();
		scroll.add(scrollY * 12);
		if (scroll.pixelOffset() != before) {
			repositionWidgets();
		}
		return true;
	}

	private void repositionWidgets() {
		int offset = scroll.pixelOffset();
		for (int i = 0; i < options.size(); i++) {
			int y = Layout.rowY(i, offset);
			rowWidgets.get(i).setY(y);
			resetButtons.get(i).setY(y);
		}
	}

	@Override
	public void onClose() {
		minecraft.setScreenAndShow(parent);
	}
}
