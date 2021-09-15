package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
    private final Long2ObjectLinkedOpenHashMap<BlockTintCache.CacheData> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ToIntFunction<BlockPos> source;

    public BlockTintCache(ToIntFunction<BlockPos> param0) {
        this.source = param0;
    }

    public int getColor(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        BlockTintCache.LatestCacheInfo var2 = this.latestChunkOnThread.get();
        if (var2.x != var0 || var2.z != var1 || var2.cache == null) {
            var2.x = var0;
            var2.z = var1;
            var2.cache = this.findOrCreateChunkCache(var0, var1);
        }

        int[] var3 = var2.cache.getLayer(param0.getY());
        int var4 = param0.getX() & 15;
        int var5 = param0.getZ() & 15;
        int var6 = var5 << 4 | var4;
        int var7 = var3[var6];
        if (var7 != -1) {
            return var7;
        } else {
            int var8 = this.source.applyAsInt(param0);
            var3[var6] = var8;
            return var8;
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

    private BlockTintCache.CacheData findOrCreateChunkCache(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        this.lock.readLock().lock();

        try {
            BlockTintCache.CacheData var1 = this.cache.get(var0);
            if (var1 != null) {
                return var1;
            }
        } finally {
            this.lock.readLock().unlock();
        }

        this.lock.writeLock().lock();

        BlockTintCache.CacheData var3;
        try {
            BlockTintCache.CacheData var2 = this.cache.get(var0);
            if (var2 == null) {
                var3 = new BlockTintCache.CacheData();
                if (this.cache.size() >= 256) {
                    this.cache.removeFirst();
                }

                this.cache.put(var0, var3);
                return var3;
            }

            var3 = var2;
        } finally {
            this.lock.writeLock().unlock();
        }

        return var3;
    }

    @OnlyIn(Dist.CLIENT)
    static class CacheData {
        private Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap<>(16);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private static final int BLOCKS_PER_LAYER = Mth.square(16);

        public int[] getLayer(int param0) {
            this.lock.readLock().lock();

            try {
                int[] var0 = this.cache.get(param0);
                if (var0 != null) {
                    return var0;
                }
            } finally {
                this.lock.readLock().unlock();
            }

            this.lock.writeLock().lock();

            int[] var12;
            try {
                var12 = this.cache.computeIfAbsent(param0, param0x -> this.allocateLayer());
            } finally {
                this.lock.writeLock().unlock();
            }

            return var12;
        }

        private int[] allocateLayer() {
            int[] var0 = new int[BLOCKS_PER_LAYER];
            Arrays.fill(var0, -1);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LatestCacheInfo {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        @Nullable
        BlockTintCache.CacheData cache;

        private LatestCacheInfo() {
        }
    }
}
