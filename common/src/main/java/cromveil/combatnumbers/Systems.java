package cromveil.combatnumbers;

import cromveil.combatnumbers.client.render.FloatingTextManager;
import cromveil.combatnumbers.core.config.ConfigStore;

public final class Systems {

	private static Server server;
	private static Client client;

	private Systems() {}

	public static void initServer(Server s) { server = s; }
	public static void initClient(Client c) { client = c; }

	public static Server server() { return server; }
	public static Client client() { return client; }

	public record Server(
			ConfigStore config,
			cromveil.combatnumbers.core.events.Event<cromveil.combatnumbers.core.events.CombatNumbersEvents.CombatCallback> combatEvent,
			cromveil.combatnumbers.core.events.Event<cromveil.combatnumbers.core.events.CombatNumbersEvents.RenderCallback> renderEvent) {}

	public record Client(ConfigStore config, FloatingTextManager textManager) {}
}
