package cromveil.combatnumbers.client.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.skins.TextureByteSource;
import cromveil.combatnumbers.resource.ModResourceAccessor;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ThemeManager {

	private static final String INDEX_PATH = "assets/" + Constants.MOD_ID + "/themes/index.json";
	private static List<ThemeInfo> cachedThemes;

	public synchronized static void discoverThemes(ModResourceAccessor resources) {
		List<ThemeInfo> themes = new ArrayList<>();
		loadBuiltinThemes(themes);
		loadResourcePackThemes(themes, resources);
		cachedThemes = List.copyOf(themes);
	}

	public static List<String> themeIds() {
		return ensureDiscovered().stream().map(ThemeInfo::id).toList();
	}

	public static Component displayName(String id) {
		ThemeInfo info = info(id);
		if (info == null) {
			return Component.literal(id);
		}
		String name = info.name() != null ? info.name() : info.id();
		if (info.builtIn()) {
			name += " (built-in)";
		}
		return Component.literal(name);
	}

	public static String description(String id) {
		ThemeInfo info = info(id);
		return info != null ? info.description() : null;
	}

	public static ThemeInfo info(String id) {
		for (ThemeInfo info : ensureDiscovered()) {
			if (info.id().equals(id)) {
				return info;
			}
		}
		return null;
	}

	private static List<ThemeInfo> ensureDiscovered() {
		if (cachedThemes == null) {
			List<ThemeInfo> themes = new ArrayList<>();
			loadBuiltinThemes(themes);
			cachedThemes = List.copyOf(themes);
		}
		return cachedThemes;
	}

	private static void loadBuiltinThemes(List<ThemeInfo> out) {
		try (InputStream in = ThemeManager.class.getClassLoader().getResourceAsStream(INDEX_PATH)) {
			if (in == null) {
				return;
			}
			JsonObject root = JsonParser
					.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
					.getAsJsonObject();
			if (root.has("themes") && root.get("themes").isJsonArray()) {
				for (JsonElement element : root.getAsJsonArray("themes")) {
					ThemeInfo info = parseThemeEntry(element, true);
					if (info != null) {
						out.add(info);
					}
				}
			}
		} catch (Exception e) {
			Constants.LOG.warn("Failed to read built-in theme index", e);
		}
	}

	private static void loadResourcePackThemes(List<ThemeInfo> out, ModResourceAccessor resources) {
		String prefix = "themes";
		for (Identifier file : resources.findResources(prefix,
				path -> path.endsWith("/theme.json"))) {
			String path = file.getPath();
			String dir = path.substring(prefix.length() + 1,
					path.length() - "/theme.json".length());
			if (dir.isEmpty()) {
				continue;
			}
			if (containsId(out, dir)) {
				continue;
			}
			byte[] bytes = resources.getBytes(file);
			if (bytes == null) {
				continue;
			}
			ThemeInfo info = parseThemeJson(dir, new String(bytes, StandardCharsets.UTF_8));
			if (info != null) {
				out.add(info);
			}
		}
	}

	private static boolean containsId(List<ThemeInfo> themes, String id) {
		for (ThemeInfo info : themes) {
			if (info.id().equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static ThemeInfo parseThemeEntry(JsonElement element, boolean builtIn) {
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();
			if (!obj.has("id")) {
				return null;
			}
			String id = obj.get("id").getAsString();
			String name = obj.has("name") ? obj.get("name").getAsString() : id;
			String description = obj.has("description") ? obj.get("description").getAsString() : null;
			return new ThemeInfo(id, name, builtIn, description);
		}
		if (element.isJsonPrimitive()) {
			String id = element.getAsString();
			return new ThemeInfo(id, id, builtIn, null);
		}
		return null;
	}

	private static ThemeInfo parseThemeJson(String id, String jsonContent) {
		try {
			JsonObject obj = JsonParser.parseString(jsonContent).getAsJsonObject();
			String name = obj.has("name") ? obj.get("name").getAsString() : id;
			String description = obj.has("description") ? obj.get("description").getAsString() : null;
			return new ThemeInfo(id, name, false, description);
		} catch (Exception e) {
			Constants.LOG.warn("Failed to parse theme metadata for '{}'", id, e);
			return null;
		}
	}

	public Optional<LoadedTheme> load(String themeId, ModResourceAccessor resources) {
		if (themeId == null || themeId.isBlank()) {
			return Optional.empty();
		}

		String base = "themes/" + themeId;
		Map<Identifier, SkinDefinition> skins = resources.loadJsonDirectory(
				base + "/skins", SkinDefinition.CODEC);
		Map<Identifier, Timeline> animations = resources.loadJsonDirectory(
				base + "/animations", TimelineCodec.CODEC);

		if (skins.isEmpty() && animations.isEmpty()) {
			Constants.LOG.warn("Theme '{}' has no skins or animations under {}", themeId,
					"assets/" + Constants.MOD_ID + "/" + base);
			return Optional.empty();
		}

		String textureBase = base + "/textures/";
		TextureByteSource textureBytes = logical -> resources.getBytes(
				Identifier.fromNamespaceAndPath(Constants.MOD_ID,
						textureBase + logical.getPath() + ".png"));

		Constants.LOG.info("Loaded theme '{}': {} skins, {} animations",
				themeId, skins.size(), animations.size());
		return Optional.of(new LoadedTheme(skins, animations, textureBytes));
	}

	public record LoadedTheme(
			Map<Identifier, SkinDefinition> skins,
			Map<Identifier, Timeline> animations,
			TextureByteSource textureBytes) {
	}
}
