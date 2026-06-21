package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.config.ModConfig;
import cromveil.combatnumbers.detector.HealTypeTracker;
import cromveil.combatnumbers.events.CombatEvent;
import cromveil.combatnumbers.events.CombatNumbersEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityHealMixin implements HealTypeTracker {

	@Unique
	private float combatNumbers$actualHeal;

	@Unique
	@Nullable
	private Identifier combatNumbers$healType;

	@Override
	public void combatNumbers$setHealType(@Nullable Identifier type) {
		this.combatNumbers$healType = type;
	}

	@Override
	@Nullable
	public Identifier combatNumbers$getAndClearHealType() {
		Identifier t = this.combatNumbers$healType;
		this.combatNumbers$healType = null;
		return t;
	}

	@Inject(method = "heal", at = @At("HEAD"))
	private void onHealHead(float amount, CallbackInfo ci) {
		this.combatNumbers$actualHeal = 0f;
	}

	@ModifyArg(
		method = "heal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
		index = 0
	)
	private float combatNumbers$captureActualHeal(float newHealth) {
		LivingEntity self = (LivingEntity) (Object) this;
		float delta = newHealth - self.getHealth();
		if (delta > 0f) {
			this.combatNumbers$actualHeal = delta;
		}
		return newHealth;
	}

	@Inject(method = "heal", at = @At("RETURN"))
	private void onHealReturn(float amount, CallbackInfo ci) {
		if (!ModConfig.getInstance().enabled)
			return;

		LivingEntity self = (LivingEntity) (Object) this;
		if (self.isRemoved())
			return;

		float actualHeal = this.combatNumbers$actualHeal;
		if (actualHeal <= 0f)
			return;

		Identifier healType = this.combatNumbers$getAndClearHealType();
		if (healType == null) healType = CombatEvent.GENERIC_HEAL;

		CombatNumbersEvents.COMBAT.invoker().onEvent(
			new CombatEvent.Heal(self, actualHeal, healType, new HashSet<>()));
	}
}
