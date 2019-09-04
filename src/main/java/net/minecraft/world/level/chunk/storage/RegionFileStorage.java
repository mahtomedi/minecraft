package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;

public abstract class RegionFileStorage implements AutoCloseable {
    protected final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
    private final File folder;

    protected RegionFileStorage(File param0) {
        this.folder = param0;
    }

    private RegionFile getRegionFile(ChunkPos param0) throws IOException {
        long var0 = ChunkPos.asLong(param0.getRegionX(), param0.getRegionZ());
        RegionFile var1 = this.regionCache.getAndMoveToFirst(var0);
        if (var1 != null) {
            return var1;
        } else {
            if (this.regionCache.size() >= 256) {
                this.regionCache.removeLast().close();
            }

            if (!this.folder.exists()) {
                this.folder.mkdirs();
            }

            File var2 = new File(this.folder, "r." + param0.getRegionX() + "." + param0.getRegionZ() + ".mca");
            RegionFile var3 = new RegionFile(var2, this.folder);
            this.regionCache.putAndMoveToFirst(var0, var3);
            return var3;
        }
    }

    @Nullable
    public CompoundTag read(ChunkPos param0) throws IOException {
        RegionFile var0 = this.getRegionFile(param0);

        Object var5;
        try (DataInputStream var1 = var0.getChunkDataInputStream(param0)) {
            if (var1 != null) {
                return NbtIo.read(var1);
            }

            var5 = null;
        }

        return (CompoundTag)var5;
    }

    protected void write(ChunkPos param0, CompoundTag param1) throws IOException {
        RegionFile var0 = this.getRegionFile(param0);

        try (DataOutputStream var1 = var0.getChunkDataOutputStream(param0)) {
            NbtIo.write(param1, var1);
        }

    }

    @Override
    public void close() throws IOException {
        for(RegionFile var0 : this.regionCache.values()) {
            var0.close();
        }

    }
}
