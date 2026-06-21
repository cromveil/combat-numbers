package cromveil.combatnumbers.config;

import cromveil.combatnumbers.CombatNumbers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig instance;

	public boolean enabled = true;
	public float baseSize = 1.0f;
	public float nearFadeDistance = 1.5f;
	public float maxRenderDistance = 32.0f;
	public float distanceFalloffStart = 3.0f;
	public float distanceFalloffEnd = 32.0f;
	public float distanceMinScale = 0.3f;

	public static ModConfig getInstance() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static ModConfig load() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("combatnumbers.json");

		if (Files.exists(configPath)) {
			try {
				String json = Files.readString(configPath);
				ModConfig config = GSON.fromJson(json, ModConfig.class);
				if (config != null) {
					CombatNumbers.LOGGER.info("Loaded config from {}", configPath);
					return config;
				}
			} catch (IOException e) {
				CombatNumbers.LOGGER.error("Failed to load config, using defaults", e);
			}
		}

		ModConfig config = new ModConfig();
		CombatNumbers.LOGGER.info("No config found, creating default at {}", configPath);
		save(config, configPath);
		return config;
	}

	private static void save(ModConfig config, Path configPath) {
		try {
			Files.createDirectories(configPath.getParent());
			Files.writeString(configPath, GSON.toJson(config));
		} catch (IOException e) {
			CombatNumbers.LOGGER.error("Failed to save config", e);
		}
	}
}
