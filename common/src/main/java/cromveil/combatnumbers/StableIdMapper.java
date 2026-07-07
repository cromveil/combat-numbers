package cromveil.combatnumbers;

import cromveil.combatnumbers.core.StableId;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class StableIdMapper {

	private StableIdMapper() {
	}

	public static StableId from(ResourceLocation id) {
		return StableId.of(id.getNamespace(), id.getPath());
	}

	public static ResourceLocation to(StableId id) {
		return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
	}

	public static <B, V, P> StreamCodec<B, P> stableIdMapCodec(
			StreamCodec<B, Map<ResourceLocation, V>> rawCodec,
			Function<P, Map<StableId, V>> getter,
			Function<Map<StableId, V>, P> constructor) {
		return StreamCodec.of(
				(buf, packet) -> {
					Map<ResourceLocation, V> raw = new LinkedHashMap<>();
					getter.apply(packet).forEach((k, v) -> raw.put(to(k), v));
					rawCodec.encode(buf, raw);
				},
				buf -> {
					Map<ResourceLocation, V> raw = rawCodec.decode(buf);
					Map<StableId, V> map = new LinkedHashMap<>();
					raw.forEach((k, v) -> map.put(from(k), v));
					return constructor.apply(map);
				});
	}
}
