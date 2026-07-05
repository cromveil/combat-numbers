package cromveil.combatnumbers.client;

import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.client.animation.AnimationCompiler;
import cromveil.combatnumbers.core.client.animation.AnimationEvaluator;
import cromveil.combatnumbers.core.client.animation.AnimationInstance;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.skins.Skin;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.client.skins.TextureByteSource;
import cromveil.combatnumbers.client.theme.ThemeManager;
import cromveil.combatnumbers.Systems;
import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.resource.ModResourceAccessor;
import cromveil.combatnumbers.skins.SkinDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class ClientRuntime {

	private final AnimationCompiler animationCompiler = new AnimationCompiler();
	private final SkinResolver skinResolver = new SkinResolver();
	private final AnimationResolver animationResolver = new AnimationResolver();
	private final ThemeManager themeManager = new ThemeManager();

	private StyleTable styleTable = StyleTable.EMPTY;

	private Map<StableId, byte[]> serverTextureBytes = new LinkedHashMap<>();
	private Map<StableId, SkinDefinition> serverSkinDefs = Map.of();

	private String appliedTheme = null;
	private ModResourceAccessor lastResources;

	public void applyStyleTable(StyleTable table) {
		this.styleTable = table;
	}

	public void applyServerSkins(Map<StableId, SkinDefinition> defs) {
		this.serverSkinDefs = Map.copyOf(defs);
		rebuildServerSkins();
	}

	public void applyServerTextures(Map<StableId, byte[]> textures) {
		this.serverTextureBytes = new LinkedHashMap<>(textures);
		rebuildServerSkins();
	}

	public void applyServerAnimations(Map<StableId, Timeline> animations) {
		animationResolver.setServer(animations);
	}

	private void rebuildServerSkins() {
		skinResolver.setServer(serverSkinDefs, logical -> serverTextureBytes.get(logical));
	}

	public void applyResourcePackSkins(Map<StableId, SkinDefinition> defs, ModResourceAccessor resources) {
		this.lastResources = resources;
		skinResolver.setResourcePack(defs, resourceTextures(resources));
		reloadTheme();
	}

	public void applyResourcePackAnimations(Map<StableId, Timeline> animations) {
		animationResolver.setResourcePack(animations);
	}

	private static TextureByteSource resourceTextures(ModResourceAccessor resources) {
		return logical -> {
			StableId png = StableId.of(
					logical.namespace(), "textures/" + logical.path() + ".png");
			return resources.getBytes(png);
		};
	}

	public void reloadTheme() {
		if (lastResources != null) {
			ThemeManager.discoverThemes(lastResources);
		}
		appliedTheme = Config.get(ConfigIds.CLIENT_THEME);
		if (lastResources == null) {
			return;
		}
		var loaded = themeManager.load(appliedTheme, lastResources);
		if (loaded.isPresent()) {
			var theme = loaded.get();
			skinResolver.setTheme(theme.skins(), theme.textureBytes());
			animationResolver.setTheme(theme.animations());
		} else {
			skinResolver.setTheme(Map.of(), id -> null);
			animationResolver.setTheme(Map.of());
		}
	}

	public void onDisconnect() {
		skinResolver.clearServer();
		animationResolver.clearServer();
		serverTextureBytes.clear();
		serverSkinDefs = Map.of();
		styleTable = StyleTable.EMPTY;
		Systems.client().textManager().clear();
	}

	public void onRenderPacket(int entityId, float value, int skinIndex, int animationIndex) {
		if (!Config.get(ConfigIds.ENABLED)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		var level = mc.level;
		if (level == null) {
			return;
		}
		var entity = level.getEntity(entityId);
		if (!(entity instanceof LivingEntity livingEntity)) {
			return;
		}

		Vec3 camPos = mc.gameRenderer.mainCamera().position();
		Vec3 worldPos = livingEntity.getEyePosition();

		var clipCtx = new ClipContext(
				camPos, worldPos,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				mc.player);
		if (level.clip(clipCtx).getType() == HitResult.Type.BLOCK) {
			return;
		}

		StableId skinId = styleTable.skinAt(skinIndex);
		StableId animId = styleTable.animationAt(animationIndex);
		Skin skin = skinResolver.resolve(skinId);
		Timeline timeline = animationResolver.resolve(animId);

		String formattedValue = String.valueOf(Math.round(value));
		var visual = skin.createVisual(formattedValue);

		double gameTime = level.getGameTime()
				+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		long seed = ThreadLocalRandom.current().nextLong();
		AnimationEvaluator eval = animationCompiler.compile(timeline, formattedValue.length(), seed);
		AnimationInstance anim = new AnimationInstance(eval);

		Systems.client().textManager().add(new FloatingText(
				worldPos, formattedValue, visual, anim, skin.getScale(), gameTime));
	}
}
