package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.HealTypeTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect")
public class RegenerationEffectMixin {

	private static final Identifier REGEN_EFFECT = Identifier.fromNamespaceAndPath("combatnumbers", "regen_effect");

	@Inject(method = "applyEffectTick", at = @At("HEAD"))
	private void combatNumbers$markRegenEffect(ServerLevel level, LivingEntity mob, int amplification,
			CallbackInfoReturnable<Boolean> cir) {
		if (mob instanceof HealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(REGEN_EFFECT);
		}
	}
}
