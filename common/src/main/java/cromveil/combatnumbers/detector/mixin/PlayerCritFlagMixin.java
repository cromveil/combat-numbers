package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.ICritTracker;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerCritFlagMixin {

	@Unique
	private float combatNumbers$attackStrength;

	@Inject(method = "attack", at = @At("HEAD"))
	private void combatNumbers$captureAttackStrength(Entity target, CallbackInfo ci) {
		this.combatNumbers$attackStrength = ((Player) (Object) this).getAttackStrengthScale(0.5f);
	}

	// < 1.21.11: `canCriticalAttack()` not available, need to manually check conditions for crit and tag crit during `attack()`
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private void combatNumbers$markCritOnTarget(Entity entity, CallbackInfo ci) {
		if (entity instanceof ICritTracker tracker && canCriticalAttack()) {
			tracker.combatNumbers$setCritAttack(true);
		}
	}

	@Unique
	private boolean canCriticalAttack() {
		Player self = (Player) (Object) this;
		return this.combatNumbers$attackStrength > 0.9f
				&& self.fallDistance > 0.0f
				&& !self.onGround()
				&& !self.onClimbable()
				&& !self.isInWater()
				&& !self.hasEffect(MobEffects.BLINDNESS)
				&& !self.isPassenger()
				&& !self.isSprinting();
	}
}
