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

	// TODO: auto discovery from resource packs
	public static List<String> builtinThemeIds() {
		List<String> ids = new ArrayList<>();
		try (InputStream in = ThemeManager.class.getClassLoader().getResourceAsStream(INDEX_PATH)) {
			if (in == null) {
				return ids;
			}
			JsonObject root = JsonParser
					.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
					.getAsJsonObject();
			if (root.has("themes") && root.get("themes").isJsonArray()) {
				for (JsonElement element : root.getAsJsonArray("themes")) {
					if (element.isJsonObject() && element.getAsJsonObject().has("id")) {
						ids.add(element.getAsJsonObject().get("id").getAsString());
					} else if (element.isJsonPrimitive()) {
						ids.add(element.getAsString());
					}
				}
			}
		} catch (Exception e) {
			Constants.LOG.warn("Failed to read theme index", e);
		}
		return ids;
	}

	public record LoadedTheme(
			Map<Identifier, SkinDefinition> skins,
			Map<Identifier, Timeline> animations,
			TextureByteSource textureBytes) {
	}
}
