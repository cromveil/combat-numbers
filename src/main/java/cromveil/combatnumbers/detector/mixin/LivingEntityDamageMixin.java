package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.config.ModConfig;
import cromveil.combatnumbers.detector.CritTracker;
import cromveil.combatnumbers.events.CombatEvent;
import cromveil.combatnumbers.events.CombatNumbersEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import java.util.HashSet;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin implements CritTracker {

	@Unique
	private float combatNumbers$actualDamage;

	@Unique
	private int combatNumbers$critCount;

	@Override
	public void combatNumbers$setCritAttack(boolean crit) {
		if (crit) this.combatNumbers$critCount++;
	}

	@Override
	public boolean combatNumbers$consumeCritAttack() {
		if (this.combatNumbers$critCount > 0) {
			this.combatNumbers$critCount--;
			return true;
		}
		return false;
	}

	@Inject(method = "hurtServer", at = @At("HEAD"))
	private void onHurtServerHead(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		this.combatNumbers$actualDamage = 0f;
	}

	@ModifyArg(
		method = "actuallyHurt",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
		index = 0
	)
	private float combatNumbers$captureActualDamage(float newHealth) {
		LivingEntity self = (LivingEntity) (Object) this;
		float actualDamage = self.getHealth() - newHealth;
		if (actualDamage > 0f) {
			this.combatNumbers$actualDamage = actualDamage;
		}
		return newHealth;
	}

	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void onHurtServerReturn(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			this.combatNumbers$critCount = 0;
			return;
		}
		if (!ModConfig.getInstance().enabled)
			return;

		LivingEntity self = (LivingEntity) (Object) this;
		if (self.isRemoved())
			return;

		float finalDamage = this.combatNumbers$actualDamage;
		if (finalDamage <= 0f)
			return;

		Set<Identifier> flags = new HashSet<>();
		if (this.combatNumbers$consumeCritAttack()) {
			flags.add(Identifier.fromNamespaceAndPath("combatnumbers", "crit"));
		}

		CombatNumbersEvents.COMBAT.invoker().onEvent(
			new CombatEvent.Damage(self, source, amount, finalDamage, flags));
	}
}
