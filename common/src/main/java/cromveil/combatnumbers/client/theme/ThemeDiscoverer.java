package cromveil.combatnumbers.client.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.theme.ThemeInfo;
import cromveil.combatnumbers.resource.IModResourceAccessor;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ThemeDiscoverer {

	private static final String INDEX_PATH = "assets/" + Constants.MOD_ID + "/themes/index.json";
	private static List<ThemeInfo> cachedThemes;

	private ThemeDiscoverer() {
	}

	public static synchronized void discover(IModResourceAccessor resources) {
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
		try (InputStream in = ThemeDiscoverer.class.getClassLoader().getResourceAsStream(INDEX_PATH)) {
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

	private static void loadResourcePackThemes(List<ThemeInfo> out, IModResourceAccessor resources) {
		String prefix = "themes";
		for (StableId file : resources.findResources(prefix,
				path -> path.endsWith("/theme.json"))) {
			String path = file.path();
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
}
