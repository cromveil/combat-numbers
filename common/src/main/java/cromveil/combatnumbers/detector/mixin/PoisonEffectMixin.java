package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.PoisonTickTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public class PoisonEffectMixin {

	@Inject(method = "applyEffectTick", at = @At("HEAD"))
	private void combatNumbers$markPoisonTick(ServerLevel level, LivingEntity mob, int amplifier,
			CallbackInfoReturnable<Boolean> cir) {
		if (mob instanceof PoisonTickTracker tracker) {
			tracker.combatNumbers$setPoisonTick(true);
		}
	}

	@Inject(method = "applyEffectTick", at = @At("RETURN"))
	private void combatNumbers$clearPoisonTickLeftover(ServerLevel level, LivingEntity mob, int amplifier,
			CallbackInfoReturnable<Boolean> cir) {
		if (mob instanceof PoisonTickTracker tracker) {
			tracker.combatNumbers$getAndClearPoisonTick();
		}
	}
}
