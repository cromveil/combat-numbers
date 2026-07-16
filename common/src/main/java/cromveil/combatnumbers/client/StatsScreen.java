package cromveil.combatnumbers.client;

import cromveil.combatnumbers.packets.RequestStatsPacket;
import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.packets.StatsResponsePacket.SourceEntry;
import cromveil.combatnumbers.packets.StatsResponsePacket.TypeEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StatsScreen extends Screen {

	private static final int COLUMN_SPACING = 4;
	private static final Component TITLE = Component.literal("Combat Numbers - Statistics");

	private final Minecraft client;

	private StatsList typeList;
	private StatsList tagList;
	private SourceList sourceList;

	private EditBox typeSearch;
	private EditBox tagSearch;
	private EditBox sourceSearch;

	private ResourceLocation selectedType;
	private ResourceLocation selectedTag;
	private String selectedSource;

	private String typeFilterText = "";
	private String tagFilterText = "";
	private String sourceFilterText = "";

	public StatsScreen() {
		super(TITLE);
		this.client = Minecraft.getInstance();
	}

	@Override
	protected void init() {
		int colWidth = (width - COLUMN_SPACING * 4) / 3;
		int searchTop = 30;
		int searchHeight = 16;
		int listTop = searchTop + searchHeight + 4;
		int listHeight = height - listTop - 30;

		int x1 = COLUMN_SPACING;
		int x2 = x1 + colWidth + COLUMN_SPACING;
		int x3 = x2 + colWidth + COLUMN_SPACING;

		if (typeSearch != null) removeWidget(typeSearch);
		typeSearch = new EditBox(font, x1, searchTop, colWidth, searchHeight,
				Component.literal("Filter types"));
		typeSearch.setValue(typeFilterText);
		typeSearch.setResponder(s -> { typeFilterText = s; rebuildLists(); });
		addRenderableWidget(typeSearch);

		if (tagSearch != null) removeWidget(tagSearch);
		tagSearch = new EditBox(font, x2, searchTop, colWidth, searchHeight,
				Component.literal("Filter tags"));
		tagSearch.setValue(tagFilterText);
		tagSearch.setResponder(s -> { tagFilterText = s; rebuildLists(); });
		addRenderableWidget(tagSearch);

		if (sourceSearch != null) removeWidget(sourceSearch);
		sourceSearch = new EditBox(font, x3, searchTop, colWidth, searchHeight,
				Component.literal("Filter sources"));
		sourceSearch.setValue(sourceFilterText);
		sourceSearch.setResponder(s -> { sourceFilterText = s; rebuildLists(); });
		addRenderableWidget(sourceSearch);

		if (typeList != null) removeWidget(typeList);
		typeList = new StatsList(this, client, colWidth, listHeight, listTop, this::onTypeSelected);
		typeList.setPosition(x1, listTop);
		addRenderableWidget(typeList);

		if (tagList != null) removeWidget(tagList);
		tagList = new StatsList(this, client, colWidth, listHeight, listTop, this::onTagSelected);
		tagList.setPosition(x2, listTop);
		addRenderableWidget(tagList);

		if (sourceList != null) removeWidget(sourceList);
		sourceList = new SourceList(this, client, colWidth, listHeight, listTop, this::onSourceSelected);
		sourceList.setPosition(x3, listTop);
		addRenderableWidget(sourceList);

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
				.pos(width / 2 - 100, height - 24)
				.size(200, 20)
				.build());

		refreshFromCache();
		requestStats();
	}

	private void requestStats() {
		if (ClientStatsCache.network != null)
			ClientStatsCache.network.sendToServer(
					new RequestStatsPacket(selectedType, selectedTag, selectedSource));
	}

	public void refreshFromCache() {
		StatsResponsePacket stats = ClientStatsCache.current;
		if (stats == null)
			return;

		rebuildLists(stats);
	}

	private void rebuildLists() {
		StatsResponsePacket stats = ClientStatsCache.current;
		if (stats == null)
			return;
		rebuildLists(stats);
	}

	private void rebuildLists(StatsResponsePacket stats) {
		typeList.updateEntries(
				filterTypeEntries(fillZeroTypes(stats.damageTypes())),
				selectedType);
		tagList.updateEntries(
				filterTagEntries(fillZeroTags(stats.damageTags())),
				selectedTag);
		sourceList.updateEntries(
				filterSourceEntries(stats.sources()),
				selectedSource);
	}

	private List<TypeEntry> filterTypeEntries(List<TypeEntry> entries) {
		if (typeFilterText.isEmpty())
			return entries;
		String lower = typeFilterText.toLowerCase();
		return entries.stream()
				.filter(e -> e.key().toString().toLowerCase().contains(lower))
				.toList();
	}

	private List<TypeEntry> filterTagEntries(List<TypeEntry> entries) {
		if (tagFilterText.isEmpty())
			return entries;
		String lower = tagFilterText.toLowerCase();
		return entries.stream()
				.filter(e -> e.key().toString().toLowerCase().contains(lower))
				.toList();
	}

	private List<SourceEntry> filterSourceEntries(List<SourceEntry> entries) {
		if (sourceFilterText.isEmpty())
			return entries;
		String lower = sourceFilterText.toLowerCase();
		return entries.stream()
				.filter(e -> e.key().toLowerCase().contains(lower))
				.toList();
	}

	private List<TypeEntry> fillZeroTypes(List<TypeEntry> serverEntries) {
		if (selectedType != null || selectedTag != null || selectedSource != null)
			return serverEntries;

		Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();
		for (TypeEntry e : serverEntries)
			counts.put(e.key(), e.count());

		var level = Minecraft.getInstance().level;
		if (level != null) {
			var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
			registry.keySet().forEach(loc -> counts.putIfAbsent(loc, 0));
		}

		return counts.entrySet().stream()
				.sorted(Map.Entry.<ResourceLocation, Integer>comparingByValue().reversed()
						.thenComparing(Map.Entry.comparingByKey()))
				.map(e -> new TypeEntry(e.getKey(), e.getValue()))
				.toList();
	}

	private List<TypeEntry> fillZeroTags(List<TypeEntry> serverEntries) {
		if (selectedType != null || selectedTag != null || selectedSource != null)
			return serverEntries;

		Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();
		for (TypeEntry e : serverEntries)
			counts.put(e.key(), e.count());

		var level = Minecraft.getInstance().level;
		if (level != null) {
			var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
			registry.getTags().forEach(t -> counts.putIfAbsent(t.getFirst().location(), 0));
		}

		return counts.entrySet().stream()
				.sorted(Map.Entry.<ResourceLocation, Integer>comparingByValue().reversed()
						.thenComparing(Map.Entry.comparingByKey()))
				.map(e -> new TypeEntry(e.getKey(), e.getValue()))
				.toList();
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.render(gui, mouseX, mouseY, delta);
		gui.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);

		int colWidth = (width - COLUMN_SPACING * 4) / 3;
		int x1 = COLUMN_SPACING;
		int x2 = x1 + colWidth + COLUMN_SPACING;
		int x3 = x2 + colWidth + COLUMN_SPACING;

		gui.drawCenteredString(font, "Damage Types", x1 + colWidth / 2, 16, 0xAAAAAA);
		gui.drawCenteredString(font, "Damage Tags", x2 + colWidth / 2, 16, 0xAAAAAA);
		gui.drawCenteredString(font, "Sources", x3 + colWidth / 2, 16, 0xAAAAAA);

		renderListTooltip(gui, mouseX, mouseY, typeList);
		renderListTooltip(gui, mouseX, mouseY, tagList);
		renderSourceTooltip(gui, mouseX, mouseY, sourceList);
	}

	private void renderListTooltip(GuiGraphics gui, int mouseX, int mouseY, StatsList list) {
		var entry = list.getHoveredTruncated(mouseX, mouseY);
		if (entry == null)
			return;
		gui.renderTooltip(font,
				List.of(Component.literal(entry.fullText),
						Component.literal(""),
						Component.literal("Count: " + entry.count)),
				java.util.Optional.empty(), mouseX, mouseY);
	}

	private void renderSourceTooltip(GuiGraphics gui, int mouseX, int mouseY, SourceList list) {
		var entry = list.getHoveredTruncated(mouseX, mouseY);
		if (entry == null)
			return;
		gui.renderTooltip(font,
				List.of(Component.literal(entry.fullText),
						Component.literal(""),
						Component.literal("Count: " + entry.count)),
				java.util.Optional.empty(), mouseX, mouseY);
	}

	private void onTypeSelected(ResourceLocation key) {
		if (key != null && key.equals(selectedType)) {
			selectedType = null;
		} else {
			selectedType = key;
			selectedTag = null;
			selectedSource = null;
		}
		requestStats();
	}

	private void onTagSelected(ResourceLocation key) {
		if (key != null && key.equals(selectedTag)) {
			selectedTag = null;
		} else {
			selectedType = null;
			selectedTag = key;
			selectedSource = null;
		}
		requestStats();
	}

	private void onSourceSelected(String key) {
		if (key != null && key.equals(selectedSource)) {
			selectedSource = null;
		} else {
			selectedType = null;
			selectedTag = null;
			selectedSource = key;
		}
		requestStats();
	}

	private static class StatsList extends ObjectSelectionList<StatsList.StatsEntry> {

		private final StatsScreen screen;
		private final java.util.function.Consumer<ResourceLocation> onSelect;

		StatsList(StatsScreen screen, Minecraft client, int width, int height,
				int top, java.util.function.Consumer<ResourceLocation> onSelect) {
			super(client, width, height, top, 18);
			this.screen = screen;
			this.onSelect = onSelect;
		}

		void updateEntries(List<TypeEntry> entries, ResourceLocation selected) {
			clearEntries();
			for (TypeEntry e : entries) {
				addEntry(new StatsEntry(screen.font, e.key(), e.key().toString(), e.count(),
						selected != null && selected.equals(e.key()),
						() -> onSelect.accept(e.key())));
			}
		}

		@Override
		public int getRowWidth() {
			return width - 10;
		}

		@Override
		protected int getScrollbarPosition() {
			return getX() + width - 6;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (mouseX < getX() || mouseX > getX() + width)
				return false;
			return super.mouseClicked(mouseX, mouseY, button);
		}

		public StatsEntry getHoveredTruncated(double mouseX, double mouseY) {
			StatsEntry entry = getEntryAtPosition(mouseX, mouseY);
			if (entry != null && entry.truncated)
				return entry;
			return null;
		}

		public static class StatsEntry extends ObjectSelectionList.Entry<StatsEntry> {

			private final net.minecraft.client.gui.Font font;
			private final ResourceLocation key;
			private final String display;
			private final int count;
			private final boolean selected;
			private final Runnable onClick;
			boolean truncated;
			String fullText;

			StatsEntry(net.minecraft.client.gui.Font font, ResourceLocation key,
					String display, int count, boolean selected, Runnable onClick) {
				this.font = font;
				this.key = key;
				this.display = display;
				this.count = count;
				this.selected = selected;
				this.onClick = onClick;
			}

			@Override
			public Component getNarration() {
				return Component.literal(display + " (" + count + ")");
			}

			@Override
			public void render(GuiGraphics gui, int index, int top, int left,
					int width, int height, int mouseX, int mouseY,
					boolean hovered, float delta) {
				int color = selected ? 0xFFFF55 : 0xFFFFFF;
				String text = display + " (" + count + ")";
				fullText = text;

				int availWidth = width - 8;
				String rendered = font.plainSubstrByWidth(text, availWidth);
				truncated = !rendered.equals(text);
				gui.drawString(font, rendered, left + 4, top + 2, color);
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				onClick.run();
				return true;
			}
		}
	}

	private static class SourceList extends ObjectSelectionList<SourceList.SrcEntry> {

		private final StatsScreen screen;
		private final java.util.function.Consumer<String> onSelect;

		SourceList(StatsScreen screen, Minecraft client, int width, int height,
				int top, java.util.function.Consumer<String> onSelect) {
			super(client, width, height, top, 18);
			this.screen = screen;
			this.onSelect = onSelect;
		}

		void updateEntries(List<SourceEntry> entries, String selected) {
			clearEntries();
			for (SourceEntry e : entries) {
				addEntry(new SrcEntry(screen.font, e.key(), e.count(),
						selected != null && selected.equals(e.key()),
						() -> onSelect.accept(e.key())));
			}
		}

		@Override
		public int getRowWidth() {
			return width - 10;
		}

		@Override
		protected int getScrollbarPosition() {
			return getX() + width - 6;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (mouseX < getX() || mouseX > getX() + width)
				return false;
			return super.mouseClicked(mouseX, mouseY, button);
		}

		public SrcEntry getHoveredTruncated(double mouseX, double mouseY) {
			SrcEntry entry = getEntryAtPosition(mouseX, mouseY);
			if (entry != null && entry.truncated)
				return entry;
			return null;
		}

		public static class SrcEntry extends ObjectSelectionList.Entry<SrcEntry> {

			private final net.minecraft.client.gui.Font font;
			private final String key;
			private final int count;
			private final boolean selected;
			private final Runnable onClick;
			boolean truncated;
			String fullText;

			SrcEntry(net.minecraft.client.gui.Font font, String key, int count,
					boolean selected, Runnable onClick) {
				this.font = font;
				this.key = key;
				this.count = count;
				this.selected = selected;
				this.onClick = onClick;
			}

			@Override
			public Component getNarration() {
				return Component.literal(key + " (" + count + ")");
			}

			@Override
			public void render(GuiGraphics gui, int index, int top, int left,
					int width, int height, int mouseX, int mouseY,
					boolean hovered, float delta) {
				int color = selected ? 0xFFFF55 : 0xFFFFFF;
				String text = key + " (" + count + ")";
				fullText = text;

				int availWidth = width - 8;
				String rendered = font.plainSubstrByWidth(text, availWidth);
				truncated = !rendered.equals(text);
				gui.drawString(font, rendered, left + 4, top + 2, color);
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				onClick.run();
				return true;
			}
		}
	}
}
