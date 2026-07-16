package cromveil.combatnumbers.client;

import cromveil.combatnumbers.packets.StatsResponsePacket;
import cromveil.combatnumbers.platform.IPlatformNetwork;

public final class ClientStatsCache {

	public static IPlatformNetwork network;
	public static StatsResponsePacket current;

	private ClientStatsCache() {}
}
