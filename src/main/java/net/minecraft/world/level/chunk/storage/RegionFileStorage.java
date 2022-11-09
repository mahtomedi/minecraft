package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable {
    public static final String ANVIL_EXTENSION = ".mca";
    private static final int MAX_CACHE_SIZE = 256;
    private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
    private final Path folder;
    private final boolean sync;

    RegionFileStorage(Path param0, boolean param1) {
        this.folder = param0;
        this.sync = param1;
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

            FileUtil.createDirectoriesSafe(this.folder);
            Path var2 = this.folder.resolve("r." + param0.getRegionX() + "." + param0.getRegionZ() + ".mca");
            RegionFile var3 = new RegionFile(var2, this.folder, this.sync);
            this.regionCache.putAndMoveToFirst(var0, var3);
            return var3;
        }
    }

    @Nullable
    public CompoundTag read(ChunkPos param0) throws IOException {
        RegionFile var0 = this.getRegionFile(param0);

        CompoundTag var4;
        try (DataInputStream var1 = var0.getChunkDataInputStream(param0)) {
            if (var1 == null) {
                return null;
            }

            var4 = NbtIo.read(var1);
        }

        return var4;
    }

    public void scanChunk(ChunkPos param0, StreamTagVisitor param1) throws IOException {
        RegionFile var0 = this.getRegionFile(param0);

        try (DataInputStream var1 = var0.getChunkDataInputStream(param0)) {
            if (var1 != null) {
                NbtIo.parse(var1, param1);
            }
        }

    }

    protected void write(ChunkPos param0, @Nullable CompoundTag param1) throws IOException {
        RegionFile var0 = this.getRegionFile(param0);
        if (param1 == null) {
            var0.clear(param0);
        } else {
            try (DataOutputStream var1 = var0.getChunkDataOutputStream(param0)) {
                NbtIo.write(param1, var1);
            }
        }

    }

    @Override
    public void close() throws IOException {
        ExceptionCollector<IOException> var0 = new ExceptionCollector<>();

        for(RegionFile var1 : this.regionCache.values()) {
            try {
                var1.close();
            } catch (IOException var5) {
                var0.add(var5);
            }
        }

        var0.throwIfPresent();
    }

    public void flush() throws IOException {
        for(RegionFile var0 : this.regionCache.values()) {
            var0.flush();
        }

    }
}
