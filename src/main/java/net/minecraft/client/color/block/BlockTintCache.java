package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(() -> new BlockTintCache.LatestCacheInfo());
    private final Long2ObjectLinkedOpenHashMap<int[]> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public int getColor(BlockPos param0, IntSupplier param1) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        BlockTintCache.LatestCacheInfo var2 = this.latestChunkOnThread.get();
        if (var2.x != var0 || var2.z != var1) {
            var2.x = var0;
            var2.z = var1;
            var2.cache = this.findOrCreateChunkCache(var0, var1);
        }

        int var3 = param0.getX() & 15;
        int var4 = param0.getZ() & 15;
        int var5 = var4 << 4 | var3;
        int var6 = var2.cache[var5];
        if (var6 != -1) {
            return var6;
        } else {
            int var7 = param1.getAsInt();
            var2.cache[var5] = var7;
            return var7;
        }
    }

    public void invalidateForChunk(int param0, int param1) {
        try {
            this.lock.writeLock().lock();

            for(int var0 = -1; var0 <= 1; ++var0) {
                for(int var1 = -1; var1 <= 1; ++var1) {
                    long var2 = ChunkPos.asLong(param0 + var0, param1 + var1);
                    this.cache.remove(var2);
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }

    }

    public void invalidateAll() {
        try {
            this.lock.writeLock().lock();
            this.cache.clear();
        } finally {
            this.lock.writeLock().unlock();
        }

    }

    private int[] findOrCreateChunkCache(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        this.lock.readLock().lock();

        int[] var1;
        try {
            var1 = this.cache.get(var0);
        } finally {
            this.lock.readLock().unlock();
        }

        if (var1 != null) {
            return var1;
        } else {
            int[] var3 = new int[256];
            Arrays.fill(var3, -1);

            try {
                this.lock.writeLock().lock();
                if (this.cache.size() >= 256) {
                    this.cache.removeFirst();
                }

                this.cache.put(var0, var3);
            } finally {
                this.lock.writeLock().unlock();
            }

            return var3;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LatestCacheInfo {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        public int[] cache;

        private LatestCacheInfo() {
        }
    }
}
