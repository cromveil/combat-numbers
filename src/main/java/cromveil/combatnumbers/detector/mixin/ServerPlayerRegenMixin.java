package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.detector.HealTypeTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerRegenMixin {

	private static final Identifier NATURAL_REGEN = Identifier.fromNamespaceAndPath("combatnumbers", "natural_regen");

	@Inject(method = "tickRegeneration", at = @At("HEAD"))
	private void combatNumbers$markNaturalRegen(CallbackInfo ci) {
		if (this instanceof HealTypeTracker tracker) {
			tracker.combatNumbers$setHealType(NATURAL_REGEN);
		}
	}
}
