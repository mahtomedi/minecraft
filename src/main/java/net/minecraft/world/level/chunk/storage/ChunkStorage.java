package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
    private final IOWorker worker;
    protected final DataFixer fixerUpper;
    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    public ChunkStorage(File param0, DataFixer param1, boolean param2) {
        this.fixerUpper = param1;
        this.worker = new IOWorker(new RegionFileStorage(param0, param2), "chunk");
    }

    public CompoundTag upgradeChunkTag(DimensionType param0, Supplier<DimensionDataStorage> param1, CompoundTag param2) {
        int var0 = getVersion(param2);
        int var1 = 1493;
        if (var0 < 1493) {
            param2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, param2, var0, 1493);
            if (param2.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                if (this.legacyStructureHandler == null) {
                    this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(param0, param1.get());
                }

                param2 = this.legacyStructureHandler.updateFromLegacy(param2);
            }
        }

        param2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, param2, Math.max(1493, var0));
        if (var0 < SharedConstants.getCurrentVersion().getWorldVersion()) {
            param2.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        }

        return param2;
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
        this.worker.synchronize().join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}
