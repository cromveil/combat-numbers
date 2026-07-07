package cromveil.combatnumbers.client;

/**
 * Necessary to bridge state from the mod instance to mixins.
 */
public final class MixinBridge {
	public static RenderContext CONTEXT;

	private MixinBridge() {
	}

	public static void init(RenderContext ctx) {
		CONTEXT = ctx;
	}
}
