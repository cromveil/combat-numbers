package cromveil.combatnumbers.client.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import cromveil.combatnumbers.Constants;
import cromveil.combatnumbers.animation.Timeline;
import cromveil.combatnumbers.animation.codec.TimelineCodec;
import cromveil.combatnumbers.client.skins.TextureByteSource;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ThemeManager {

	private static final String INDEX_PATH = "assets/" + Constants.MOD_ID + "/themes/index.json";
	private static List<ThemeInfo> cachedThemes;

	public synchronized static void discoverThemes(ResourceManager resources) {
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

	private static void loadResourcePackThemes(List<ThemeInfo> out, ResourceManager resources) {
		String prefix = "themes";
		var found = resources.listResources(prefix,
				location -> location.getPath().endsWith("/theme.json"));
		for (var entry : found.entrySet()) {
			String path = entry.getKey().getPath();
			String dir = path.substring(prefix.length() + 1,
					path.length() - "/theme.json".length());
			if (dir.isEmpty()) {
				continue;
			}
			if (containsId(out, dir)) {
				continue;
			}
			ThemeInfo info = parseThemeJson(dir, entry.getValue());
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

	private static ThemeInfo parseThemeJson(String id, Resource resource) {
		try (InputStream in = resource.open()) {
			JsonObject obj = JsonParser
					.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
					.getAsJsonObject();
			String name = obj.has("name") ? obj.get("name").getAsString() : id;
			String description = obj.has("description") ? obj.get("description").getAsString() : null;
			return new ThemeInfo(id, name, false, description);
		} catch (Exception e) {
			Constants.LOG.warn("Failed to parse theme metadata for '{}'", id, e);
			return null;
		}
	}

	public Optional<LoadedTheme> load(String themeId, ResourceManager resources) {
		if (themeId == null || themeId.isBlank()) {
			return Optional.empty();
		}

		String base = "themes/" + themeId;
		Map<Identifier, SkinDefinition> skins = discover(resources, base + "/skins", SkinDefinition.CODEC);
		Map<Identifier, Timeline> animations = discover(resources, base + "/animations", TimelineCodec.CODEC);

		if (skins.isEmpty() && animations.isEmpty()) {
			Constants.LOG.warn("Theme '{}' has no skins or animations under {}", themeId,
					"assets/" + Constants.MOD_ID + "/" + base);
			return Optional.empty();
		}

		String textureBase = base + "/textures/";
		TextureByteSource textureBytes = logical -> readBytes(resources,
				Identifier.fromNamespaceAndPath(Constants.MOD_ID, textureBase + logical.getPath() + ".png"));

		Constants.LOG.info("Loaded theme '{}': {} skins, {} animations", themeId, skins.size(), animations.size());
		return Optional.of(new LoadedTheme(skins, animations, textureBytes));
	}

	private static <T> Map<Identifier, T> discover(ResourceManager resources, String dir, Codec<T> codec) {
		Map<Identifier, T> result = new LinkedHashMap<>();
		var found = resources.listResources(dir, location -> location.getPath().endsWith(".json"));
		for (var entry : found.entrySet()) {
			Identifier file = entry.getKey();
			String path = file.getPath();
			String name = path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length());
			Identifier id = Identifier.fromNamespaceAndPath(file.getNamespace(), name);
			T value = parse(file, entry.getValue(), codec);
			if (value != null) {
				result.put(id, value);
			}
		}
		return result;
	}

	private static <T> T parse(Identifier file, Resource resource, Codec<T> codec) {
		try (InputStream in = resource.open()) {
			JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
			Optional<T> value = result.result();
			if (value.isEmpty()) {
				Constants.LOG.warn("Failed to parse theme file {}: {}", file,
						result.error().map(DataResult.Error::message).orElse("unknown error"));
			}
			return value.orElse(null);
		} catch (Exception e) {
			Constants.LOG.warn("Failed to read theme file {}", file, e);
			return null;
		}
	}

	private static byte[] readBytes(ResourceManager resources, Identifier location) {
		Optional<Resource> resource = resources.getResource(location);
		if (resource.isEmpty()) {
			return null;
		}
		try (InputStream in = resource.get().open()) {
			return in.readAllBytes();
		} catch (Exception e) {
			Constants.LOG.warn("Failed to read theme texture {}", location, e);
			return null;
		}
	}

	public record LoadedTheme(
			Map<Identifier, SkinDefinition> skins,
			Map<Identifier, Timeline> animations,
			TextureByteSource textureBytes) {
	}
}
