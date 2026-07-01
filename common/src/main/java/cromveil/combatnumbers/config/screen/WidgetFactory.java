package cromveil.combatnumbers.config.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

final class WidgetFactory {

	private final Font font;

	WidgetFactory(Font font) {
		this.font = font;
	}

	AbstractWidget create(ConfigOption<?> opt, int x, int y, int width, Runnable onChanged) {
		return switch (opt.type()) {
			case BOOLEAN -> makeCheckbox(opt, x, y, width, onChanged);
			case CYCLE -> makeCycle(opt, x, y, width, onChanged);
			case SLIDER -> makeSlider(opt, x, y, width, onChanged);
		};
	}

	Button makeResetButton(int x, int y, Runnable onReset) {
		return Button.builder(Component.literal("\u21A9"), button -> onReset.run())
				.bounds(x, y, Layout.RESET_BTN_WIDTH, Layout.WIDGET_HEIGHT)
				.build();
	}

	@SuppressWarnings("unchecked")
	private AbstractWidget makeCheckbox(ConfigOption<?> opt, int x, int y, int width, Runnable onChanged) {
		Checkbox cb = Checkbox.builder(Component.empty(), font)
				.selected(((ConfigOption<Boolean>) opt).get())
				.onValueChange((chk, val) -> {
					((ConfigOption<Boolean>) opt).set(val);
					onChanged.run();
				})
				.pos(x, y)
				.build();
		cb.setWidth(width);
		return cb;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private AbstractWidget makeCycle(ConfigOption<?> opt, int x, int y, int width, Runnable onChanged) {
		Function display = (Function) makeCycleDisplay(opt);
		Object current = opt.get();
		List values = opt.cycleValues;

		return (AbstractWidget) CycleButton.builder(display, current)
				.withValues(values)
				.displayOnlyValue()
				.create(x, y, width, Layout.WIDGET_HEIGHT, Component.empty(),
						(btn, val) -> {
							((ConfigOption) opt).set(val);
							onChanged.run();
						});
	}

	private <T> Function<T, Component> makeCycleDisplay(ConfigOption<T> opt) {
		if (opt.emptyValue == null) {
			return opt.displayFn;
		}
		return val -> {
			if (opt.emptyValue.equals(val)) {
				return opt.emptyDisplay;
			}
			return opt.displayFn.apply(val);
		};
	}

	@SuppressWarnings("unchecked")
	private AbstractWidget makeSlider(ConfigOption<?> opt, int x, int y, int width, Runnable onChanged) {
		ConfigOption<Double> dOpt = (ConfigOption<Double>) opt;
		double min = dOpt.sliderMin;
		double max = dOpt.sliderMax;
		SliderFormat format = dOpt.sliderFormat;
		double current = dOpt.get();
		double normalized = (current - min) / (max - min);

		return new AbstractSliderButton(x, y, width, Layout.WIDGET_HEIGHT, Component.empty(), normalized) {
			{
				updateMessage();
			}

			@Override
			protected void updateMessage() {
				double actual = format.snap(min + (max - min) * this.value);
				setMessage(Component.literal(format.format(actual)));
			}

			@Override
			protected void applyValue() {
				double actual = format.snap(min + (max - min) * this.value);
				dOpt.set(actual);
				this.value = (actual - min) / (max - min);
				onChanged.run();
			}
		};
	}
}
