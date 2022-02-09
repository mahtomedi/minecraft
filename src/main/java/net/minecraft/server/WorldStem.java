package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(
    CloseableResourceManager resourceManager, ReloadableServerResources dataPackResources, RegistryAccess.Frozen registryAccess, WorldData worldData
) implements AutoCloseable {
    public static CompletableFuture<WorldStem> load(
        WorldStem.InitConfig param0, WorldStem.DataPackConfigSupplier param1, WorldStem.WorldDataSupplier param2, Executor param3, Executor param4
    ) {
        DataPackConfig var0 = param1.get();
        DataPackConfig var1 = MinecraftServer.configurePackRepository(param0.packRepository(), var0, param0.safeMode());
        List<PackResources> var2 = param0.packRepository().openAllSelected();
        CloseableResourceManager var3 = new MultiPackResourceManager(PackType.SERVER_DATA, var2);
        Pair<WorldData, RegistryAccess.Frozen> var4 = param2.get(var3, var1);
        WorldData var5 = var4.getFirst();
        RegistryAccess.Frozen var6 = var4.getSecond();
        return ReloadableServerResources.loadResources(var3, var6, param0.commandSelection(), param0.functionCompilationLevel(), param3, param4)
            .whenComplete((param1x, param2x) -> {
                if (param2x != null) {
                    var3.close();
                }
    
            })
            .thenApply(param3x -> new WorldStem(var3, param3x, var6, var5));
    }

    @Override
    public void close() {
        this.resourceManager.close();
    }

    public void updateGlobals() {
        this.dataPackResources.updateRegistryTags(this.registryAccess);
    }

    @FunctionalInterface
    public interface DataPackConfigSupplier extends Supplier<DataPackConfig> {
        static WorldStem.DataPackConfigSupplier loadFromWorld(LevelStorageSource.LevelStorageAccess param0) {
            return () -> {
                DataPackConfig var0x = param0.getDataPacks();
                if (var0x == null) {
                    throw new IllegalStateException("Failed to load data pack config");
                } else {
                    return var0x;
                }
            };
        }
    }

    public static record InitConfig(PackRepository packRepository, Commands.CommandSelection commandSelection, int functionCompilationLevel, boolean safeMode) {
    }

    @FunctionalInterface
    public interface WorldDataSupplier {
        Pair<WorldData, RegistryAccess.Frozen> get(ResourceManager var1, DataPackConfig var2);

        static WorldStem.WorldDataSupplier loadFromWorld(LevelStorageSource.LevelStorageAccess param0) {
            return (param1, param2) -> {
                RegistryAccess.Writable var0x = RegistryAccess.builtinCopy();
                DynamicOps<Tag> var1 = RegistryOps.createAndLoad(NbtOps.INSTANCE, var0x, param1);
                WorldData var2 = param0.getDataTag(var1, param2);
                if (var2 == null) {
                    throw new IllegalStateException("Failed to load world");
                } else {
                    return Pair.of(var2, var0x.freeze());
                }
            };
        }
    }
}
