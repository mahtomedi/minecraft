package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
    public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
    private final IOWorker worker;
    protected final DataFixer fixerUpper;
    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    public ChunkStorage(Path param0, DataFixer param1, boolean param2) {
        this.fixerUpper = param1;
        this.worker = new IOWorker(param0, param2, "chunk");
    }

    public CompoundTag upgradeChunkTag(
        ResourceKey<Level> param0, Supplier<DimensionDataStorage> param1, CompoundTag param2, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> param3
    ) {
        int var0 = getVersion(param2);
        if (var0 < 1493) {
            param2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, param2, var0, 1493);
            if (param2.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                if (this.legacyStructureHandler == null) {
                    this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(param0, param1.get());
                }

                param2 = this.legacyStructureHandler.updateFromLegacy(param2);
            }
        }

        injectDatafixingContext(param2, param0, param3);
        param2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, param2, Math.max(1493, var0));
        if (var0 < SharedConstants.getCurrentVersion().getWorldVersion()) {
            param2.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        }

        param2.remove("__context");
        return param2;
    }

    public static void injectDatafixingContext(CompoundTag param0, ResourceKey<Level> param1, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> param2) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("dimension", param1.location().toString());
        param2.ifPresent(param1x -> var0.putString("generator", param1x.location().toString()));
        param0.put("__context", var0);
    }

    public static int getVersion(CompoundTag param0) {
        return param0.contains("DataVersion", 99) ? param0.getInt("DataVersion") : -1;
    }

    @Nullable
    public CompoundTag read(ChunkPos param0) throws IOException {
        return this.worker.load(param0);
    }

    public void write(ChunkPos param0, CompoundTag param1) {
        this.worker.store(param0, param1);
        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(param0.toLong());
        }

    }

    public void flushWorker() {
        this.worker.synchronize(true).join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public ChunkScanAccess chunkScanner() {
        return this.worker;
    }
}
