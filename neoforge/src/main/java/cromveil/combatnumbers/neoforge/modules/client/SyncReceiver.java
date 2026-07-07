package cromveil.combatnumbers.neoforge.modules.client;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.styles.StyleTable;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

import cromveil.combatnumbers.neoforge.ClientPayloadHandlers;

public final class SyncReceiver implements IClientSetup {

	private final AnimationResolver animationResolver;
	private final SkinResolver skinResolver;
	public SyncReceiver(AnimationResolver animationResolver, SkinResolver skinResolver) {
		this.animationResolver = animationResolver;
		this.skinResolver = skinResolver;
	}

	@Override
	public void register() {
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> {
			animationResolver.clearServer();
			skinResolver.clearServer();
		});
	}

	public StyleTable styleTable() {
		return ClientPayloadHandlers.styleTable();
	}
}