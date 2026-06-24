package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.HealTypeTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public class HealOrHarmEffectMixin {

	private static final Identifier INSTANT_HEALTH = Identifier.fromNamespaceAndPath("combatnumbers", "instant_health");

	@Inject(method = "applyEffectTick", at = @At("HEAD"))
	private void combatNumbers$markInstantHealthTick(ServerLevel level, LivingEntity mob, int amplification,
			CallbackInfoReturnable<Boolean> cir) {
		if (mob instanceof HealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(INSTANT_HEALTH);
		}
	}

	@Inject(method = "applyInstantaneousEffect", at = @At("HEAD"))
	private void combatNumbers$markInstantHealthEffect(ServerLevel serverLevel, @Nullable Entity source, @Nullable Entity owner,
			LivingEntity mob, int amplification, double scale, CallbackInfo ci) {
		if (mob instanceof HealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(INSTANT_HEALTH);
		}
	}
}
