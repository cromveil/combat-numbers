package cromveil.combatnumbers.forge.modules.client;

import java.util.Map;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import cromveil.combatnumbers.client.animation.AnimationResolver;
import cromveil.combatnumbers.client.skins.SkinResolver;
import cromveil.combatnumbers.core.IClientSetup;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.core.animation.Timeline;
import cromveil.combatnumbers.core.styles.StyleTable;
import cromveil.combatnumbers.skins.SkinDefinition;

public final class SyncReceiver implements IClientSetup {

	private final AnimationResolver animationResolver;
	private final SkinResolver skinResolver;
	private StyleTable styleTable = StyleTable.EMPTY;

	public SyncReceiver(AnimationResolver animationResolver, SkinResolver skinResolver) {
		this.animationResolver = animationResolver;
		this.skinResolver = skinResolver;
	}

	public void setStyleTable(StyleTable styleTable) {
		this.styleTable = styleTable;
	}

	public void setAnimations(Map<StableId, Timeline> animations) {
		animationResolver.setServer(animations);
	}

	public void setSkins(Map<StableId, SkinDefinition> skins) {
		skinResolver.setServerSkinDefs(skins);
	}

	public void setTextures(Map<StableId, byte[]> textures) {
		skinResolver.setServerTextureBytes(textures);
	}

	public StyleTable styleTable() {
		return styleTable;
	}

	@Override
	public void register() {
		MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
			animationResolver.clearServer();
			skinResolver.clearServer();
			styleTable = StyleTable.EMPTY;
		});
	}
}
