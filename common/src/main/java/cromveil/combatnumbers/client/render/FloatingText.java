package cromveil.combatnumbers.client.render;

import cromveil.combatnumbers.animation.Channel;
import cromveil.combatnumbers.client.animation.AnimationInstance;
import cromveil.combatnumbers.client.animation.ChannelBuffer;
import cromveil.combatnumbers.client.skins.SkinRenderer;
import net.minecraft.world.phys.Vec3;

public class FloatingText {

	private static final float MS_PER_TICK = 50f;

	public final Vec3 worldPos;
	public final String formattedText;
	public final SkinRenderer visual;
	public final AnimationInstance animation;
	public final float scale;

	private final double createdAtGameTime;
	private double currentGameTime;
	private final ChannelBuffer channelBuffer = new ChannelBuffer();
	private int lastSampledChar = -1;
	private float lastSampledElapsed = -1f;

	public FloatingText(Vec3 worldPos, String formattedText, SkinRenderer visual,
			AnimationInstance animation, float scale, double gameTime) {
		this.worldPos = worldPos;
		this.formattedText = formattedText;
		this.visual = visual;
		this.animation = animation;
		this.scale = scale;
		this.createdAtGameTime = gameTime;
		this.currentGameTime = gameTime;
	}

	public void setGameTime(double gameTime) {
		this.currentGameTime = gameTime;
		animation.update(getElapsedMs());
	}

	private float getElapsedMs() {
		return (float) ((currentGameTime - createdAtGameTime) * MS_PER_TICK);
	}

	public int getCharCount() {
		return Math.max(1, formattedText.length());
	}

	public boolean hasPerCharStagger() {
		return animation.hasStagger() && formattedText.length() > 1;
	}

	public void sampleChannels(int charIdx) {
		float elapsed = getElapsedMs();
		if (charIdx == lastSampledChar && elapsed == lastSampledElapsed) {
			return;
		}
		lastSampledChar = charIdx;
		lastSampledElapsed = elapsed;
		animation.sample(charIdx, channelBuffer, elapsed);
	}

	public float getChannel(Channel ch) {
		return channelBuffer.get(ch);
	}

	public float getAlpha() {
		sampleChannels(0);
		return Math.clamp(channelBuffer.get(Channel.OPACITY), 0f, 1f);
	}

	public float getScaleMultiplier() {
		sampleChannels(0);
		return channelBuffer.get(Channel.SCALE);
	}

	public float getCharScaleMultiplier(int charIdx) {
		sampleChannels(charIdx);
		return channelBuffer.get(Channel.SCALE);
	}

	public float getCharAlpha(int charIdx) {
		sampleChannels(charIdx);
		return Math.clamp(channelBuffer.get(Channel.OPACITY), 0f, 1f);
	}

	public Vec3 getMovementOffset() {
		sampleChannels(0);
		return new Vec3(
				channelBuffer.get(Channel.X),
				channelBuffer.get(Channel.Y),
				channelBuffer.get(Channel.Z));
	}

	public Vec3 getCharMovementOffset(int charIdx) {
		sampleChannels(charIdx);
		return new Vec3(
				channelBuffer.get(Channel.X),
				channelBuffer.get(Channel.Y),
				channelBuffer.get(Channel.Z));
	}

	public float getRotation() {
		sampleChannels(0);
		return channelBuffer.get(Channel.ROTATION);
	}

	public float getCharRotation(int charIdx) {
		sampleChannels(charIdx);
		return channelBuffer.get(Channel.ROTATION);
	}

	public Vec3 getRenderPos() {
		return worldPos.add(getMovementOffset());
	}

	public Vec3 getCharRenderPos(int charIdx) {
		return worldPos.add(getCharMovementOffset(charIdx));
	}

	public boolean isExpired() {
		return animation.isComplete(getElapsedMs());
	}
}
