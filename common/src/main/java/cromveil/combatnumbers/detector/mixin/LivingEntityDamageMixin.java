package cromveil.combatnumbers.detector.mixin;

import cromveil.combatnumbers.config.Config;
import cromveil.combatnumbers.config.ConfigIds;
import cromveil.combatnumbers.core.ResourceId;
import cromveil.combatnumbers.core.events.CombatEvent;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.detector.CritTracker;
import cromveil.combatnumbers.detector.PoisonTickTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin implements CritTracker, PoisonTickTracker {

	@Unique
	private float combatNumbers$actualDamage;

	@Unique
	private int combatNumbers$critCount;

	@Unique
	private boolean combatNumbers$poisonTick;

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

	@Override
	public void combatNumbers$setPoisonTick(boolean value) {
		this.combatNumbers$poisonTick = value;
	}

	@Override
	public boolean combatNumbers$getAndClearPoisonTick() {
		boolean value = this.combatNumbers$poisonTick;
		this.combatNumbers$poisonTick = false;
		return value;
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
			this.combatNumbers$poisonTick = false;
			return;
		}
		if (!Config.get(ConfigIds.ENABLED))
			return;

		LivingEntity self = (LivingEntity) (Object) this;
		if (self.isRemoved())
			return;

		float finalDamage = this.combatNumbers$actualDamage;
		if (finalDamage <= 0f)
			return;

		Set<ResourceId> flags = new LinkedHashSet<>();
		if (this.combatNumbers$consumeCritAttack()) {
			flags.add(ResourceId.of("combatnumbers", "crit"));
		}
		if (this.combatNumbers$getAndClearPoisonTick()) {
			flags.add(ResourceId.of("combatnumbers", "poison_tick"));
		}

		Optional<ResourceId> typeKey = source.typeHolder().unwrapKey()
				.map(k -> ResourceId.of(k.identifier().getNamespace(), k.identifier().getPath()));

		Set<ResourceId> tags = source.typeHolder().tags()
				.map(t -> ResourceId.of(t.location().getNamespace(), t.location().getPath()))
				.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

		Optional<Integer> attackerId = Optional.empty();
		var attacker = source.getEntity();
		if (attacker != null) {
			attackerId = Optional.of(attacker.getId());
		}

		CombatNumbersEvents.COMBAT.invoker().onEvent(
			new CombatEvent.Damage(self.getId(), typeKey, tags, amount, finalDamage, flags, attackerId));
	}
}
