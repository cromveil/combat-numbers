package cromveil.combatnumbers.modules.server;

import java.util.function.Supplier;

import cromveil.combatnumbers.config.Configs;
import cromveil.combatnumbers.core.IServerSetup;
import cromveil.combatnumbers.core.config.IConfigState;
import cromveil.combatnumbers.core.events.CombatNumbersEvents;
import cromveil.combatnumbers.core.events.DispatchEvent;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.packets.RenderPacket;
import cromveil.combatnumbers.platform.IPlatformNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class BroadcastModule implements IServerSetup {

	private final IConfigState config;
	private final IPlatformNetwork network;
	private final Supplier<StyleTable> styleTable;
	private final EntityLevelResolver entities;
	public BroadcastModule(IConfigState config,
			IPlatformNetwork network,
			Supplier<StyleTable> styleTable,
			EntityLevelResolver entities) {
		this.config = config;
		this.network = network;
		this.styleTable = styleTable;
		this.entities = entities;
	}

	@Override
	public void register() {
		network.registerClientbound(RenderPacket.TYPE, RenderPacket.STREAM_CODEC);
		CombatNumbersEvents.DISPATCH.register(this::onDispatch);
	}

	private void onDispatch(DispatchEvent event) {
		ServerLevel level = entities.resolve(event.entityId());
		if (level == null)
			return;

		var entity = level.getEntity(event.entityId());
		if (!(entity instanceof LivingEntity living))
			return;

		StyleTable table = styleTable.get();
		RenderPacket packet = new RenderPacket(
				event.entityId(), event.value(),
				table.skinIndex(event.skinId()),
				table.animationIndex(event.animationId()));

		sendToPlayersInRange(level, living, packet);
	}

	private void sendToPlayersInRange(ServerLevel level, LivingEntity entity, RenderPacket packet) {
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();

		double maxDistSq = config.get(Configs.MAX_RENDER_DISTANCE);
		maxDistSq *= maxDistSq;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(x, y, z) > maxDistSq)
				continue;
			network.sendToPlayer(player, packet);
		}
	}
}
