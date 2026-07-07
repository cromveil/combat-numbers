package cromveil.combatnumbers.fabric.impl.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import cromveil.combatnumbers.StableIdMapper;
import cromveil.combatnumbers.core.StableId;
import cromveil.combatnumbers.resource.IReloadListenerRegistry;
import cromveil.combatnumbers.resource.IResourceLoadCallback;
import cromveil.combatnumbers.resource.MinecraftReloadListener;

public class FabricReloadRegistry implements IReloadListenerRegistry {

	@Override
	public <T> void registerServerData(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		var delegate = new MinecraftReloadListener<>(codec, directory, consumer);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
				new IdentifiableResourceReloadListener() {
					@Override
					public ResourceLocation getFabricId() {
						return StableIdMapper.to(name);
					}
					@Override
					public CompletableFuture<Void> reload(PreparationBarrier barrier,
							ResourceManager manager, ProfilerFiller prepProfiler,
							ProfilerFiller reloadProfiler, Executor bgExecutor,
							Executor gameExecutor) {
						return delegate.reload(barrier, manager, prepProfiler,
								reloadProfiler, bgExecutor, gameExecutor);
					}
					@Override
					public String getName() {
						return delegate.getName();
					}
				});
	}

	@Override
	public <T> void registerClientResources(StableId name, String directory,
			Codec<T> codec, IResourceLoadCallback<T> consumer) {
		var delegate = new MinecraftReloadListener<>(codec, directory, consumer);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(new IdentifiableResourceReloadListener() {
					@Override
					public ResourceLocation getFabricId() {
						return StableIdMapper.to(name);
					}
					@Override
					public CompletableFuture<Void> reload(PreparationBarrier barrier,
							ResourceManager manager, ProfilerFiller prepProfiler,
							ProfilerFiller reloadProfiler, Executor bgExecutor,
							Executor gameExecutor) {
						return delegate.reload(barrier, manager, prepProfiler,
								reloadProfiler, bgExecutor, gameExecutor);
					}
					@Override
					public String getName() {
						return delegate.getName();
					}
				});
	}
}