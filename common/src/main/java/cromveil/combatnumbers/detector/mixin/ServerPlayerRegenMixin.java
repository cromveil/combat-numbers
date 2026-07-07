package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.detector.IHealTypeTracker;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerRegenMixin {

	private static final StableId NATURAL_REGEN = StableId.of(Constants.MOD_ID, "natural_regen");

	@Inject(method = "tickRegeneration", at = @At("HEAD"))
	private void combatNumbers$markNaturalRegen(CallbackInfo ci) {
		if (this instanceof IHealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(NATURAL_REGEN);
		}
	}
}
