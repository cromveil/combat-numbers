package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.ICritTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerCritFlagMixin {

	@Inject(method = "canCriticalAttack", at = @At("RETURN"))
	private void combatNumbers$markCritOnTarget(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && entity instanceof ICritTracker tracker) {
			tracker.combatNumbers$setCritAttack(true);
		}
	}
}
