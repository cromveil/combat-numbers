package cromveil.combatnumbers.forge;

import cromveil.combatnumbers.client.FloatingTextFactory;
import cromveil.combatnumbers.forge.modules.client.SyncReceiver;

public final class ForgeClientBridge {

	private static FloatingTextFactory factory;
	private static SyncReceiver syncReceiver;

	private ForgeClientBridge() {}

	public static void init(FloatingTextFactory factory, SyncReceiver syncReceiver) {
		ForgeClientBridge.factory = factory;
		ForgeClientBridge.syncReceiver = syncReceiver;
	}

	public static FloatingTextFactory factory() {
		return factory;
	}

	public static SyncReceiver syncReceiver() {
		return syncReceiver;
	}
}
