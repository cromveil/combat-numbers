package cromveil.combatnumbers.modules.server;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.platform.IPlatformNetwork;

import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class StatsCommand {

	private StatsCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
			DamageStatsTracker tracker, IPlatformNetwork network) {
		dispatcher.register(
				Commands.literal(Constants.MOD_ID)
						.then(Commands.literal("stats")
								.executes(ctx -> {
									ServerPlayer player = ctx.getSource().getPlayerOrException();
									StatsSnapshot snapshot = tracker.getSnapshot(
											Optional.empty(), Optional.empty(), Optional.empty());
									network.sendToPlayer(player, StatsResponsePacket.from(snapshot));
									return 1;
								})));
	}
}
