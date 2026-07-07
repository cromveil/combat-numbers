package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.detector.IHealTypeTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class ServerPlayerRegenMixin {

	private static final StableId NATURAL_REGEN = StableId.of(Constants.MOD_ID, "natural_regen");

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
	private void combatNumbers$markNaturalRegen(Player player, CallbackInfo ci) {
		if (player instanceof IHealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(NATURAL_REGEN);
		}
	}
}
