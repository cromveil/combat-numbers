package cromveil.combatnumbers.gametest;

import java.util.ArrayList;
import java.util.List;

import cromveil.combatnumbers.events.CombatNumbersEvents;
import cromveil.combatnumbers.events.RenderEvent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

public class CombatNumbersGameTest {

	private static final BlockPos SPAWN_POS = new BlockPos(2, 2, 2);

	@GameTest(maxTicks = 40)
	public void basicDamagePacket(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		LivingEntity zombie = helper.spawn(EntityTypes.ZOMBIE, SPAWN_POS);
		DamageSource source = level.damageSources().magic();

		List<RenderEvent> captured = new ArrayList<>();
		CombatNumbersEvents.RENDER.register(captured::add);
		helper.hurt(zombie, source, 5.0f);

		helper.assertTrue(captured.size() == 1,
			"Expected 1 instance, got " + captured.size());

		RenderEvent inst = captured.getFirst();
		helper.assertTrue(inst.entity().getId() == zombie.getId(),
			"Instance entityId " + inst.entity().getId() + " != zombie id " + zombie.getId());
		helper.assertTrue(inst.value() > 0f && inst.value() <= 5.0f,
			"Instance value " + inst.value() + " not in range (0, 5.0]");

		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void twoDamagesInSameTick(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		LivingEntity zombie = helper.spawn(EntityTypes.ZOMBIE, SPAWN_POS);
		DamageSource source = level.damageSources().magic();

		List<RenderEvent> captured = new ArrayList<>();
		CombatNumbersEvents.RENDER.register(captured::add);
		helper.hurt(zombie, source, 5.0f);
		helper.hurt(zombie, source, 10.0f);

		helper.assertTrue(captured.size() == 2,
			"Expected 2 instances, got " + captured.size());

		helper.assertTrue(captured.get(0).entity().getId() == zombie.getId(),
			"First instance for wrong entity");
		helper.assertTrue(captured.get(1).entity().getId() == zombie.getId(),
			"Second instance for wrong entity");

		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void lastHitDamageIsAccurate(GameTestHelper helper) {
		final float DAMAGE = 1000f;

		ServerLevel level = helper.getLevel();
		LivingEntity zombie = helper.spawn(EntityTypes.ZOMBIE, SPAWN_POS);
		DamageSource source = level.damageSources().magic();

		List<RenderEvent> captured = new ArrayList<>();
		CombatNumbersEvents.RENDER.register(captured::add);
		helper.hurt(zombie, source, DAMAGE);

		helper.assertTrue(captured.size() == 1,
			"Expected 1 instance, got " + captured.size());

		RenderEvent inst = captured.getFirst();
		helper.assertTrue(inst.value() == DAMAGE,
			"Wrong last hit damage:" + inst.value() + ", expected:" + DAMAGE);

		helper.succeed();
	}
}
