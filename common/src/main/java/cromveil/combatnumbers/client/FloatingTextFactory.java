package cromveil.combatnumbers.client;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.render.FloatingText;
import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.client.skins.ISkin;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.animation.runtime.AnimationCompiler;
import cromveil.combatnumbers.core.animation.runtime.IAnimationEvaluator;
import cromveil.combatnumbers.core.animation.runtime.AnimationInstance;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.styles.StyleTable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FloatingTextFactory {

	private final IConfigState config;
	private final FloatingTextManager textManager;
	private final SkinResolver skinResolver;
	private final AnimationResolver animationResolver;
	private final AnimationCompiler animationCompiler;
	private final Supplier<StyleTable> styleTable;

	public FloatingTextFactory(IConfigState config, FloatingTextManager textManager,
			SkinResolver skinResolver, AnimationResolver animationResolver,
			AnimationCompiler animationCompiler, Supplier<StyleTable> styleTable) {
		this.config = config;
		this.textManager = textManager;
		this.skinResolver = skinResolver;
		this.animationResolver = animationResolver;
		this.animationCompiler = animationCompiler;
		this.styleTable = styleTable;
	}

	public void onRenderPacket(int entityId, float value, int skinIndex, int animationIndex) {
		if (!config.get(Configs.ENABLED)) {
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

		Vec3 camPos = mc.gameRenderer.getMainCamera().position();
		Vec3 worldPos = livingEntity.getEyePosition();

		if (!config.get(Configs.SHOW_THROUGH_WALLS)) {
			var clipCtx = new ClipContext(
					camPos, worldPos,
					ClipContext.Block.COLLIDER,
					ClipContext.Fluid.NONE,
					mc.player);
			if (level.clip(clipCtx).getType() == HitResult.Type.BLOCK) {
				return;
			}
		}

		StyleTable table = styleTable.get();
		StableId skinId = table.skinAt(skinIndex);
		StableId animId = table.animationAt(animationIndex);
		ISkin skin = skinResolver.resolve(skinId);
		Timeline timeline = animationResolver.resolve(animId);

		String formattedValue = String.valueOf(Math.round(value));
		var visual = skin.createVisual(formattedValue);

		double gameTime = level.getGameTime()
				+ mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		long seed = ThreadLocalRandom.current().nextLong();
		IAnimationEvaluator eval = animationCompiler.compile(timeline, formattedValue.length(), seed);
		AnimationInstance anim = new AnimationInstance(eval);

		textManager.add(new FloatingText(
				worldPos, formattedValue, visual, anim, skin.getScale(), gameTime));
	}
}
