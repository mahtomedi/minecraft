package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderRegionCache {
    private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

    @Nullable
    public RenderChunkRegion createRegion(Level param0, BlockPos param1, BlockPos param2, int param3) {
        int var0 = SectionPos.blockToSectionCoord(param1.getX() - param3);
        int var1 = SectionPos.blockToSectionCoord(param1.getZ() - param3);
        int var2 = SectionPos.blockToSectionCoord(param2.getX() + param3);
        int var3 = SectionPos.blockToSectionCoord(param2.getZ() + param3);
        RenderRegionCache.ChunkInfo[][] var4 = new RenderRegionCache.ChunkInfo[var2 - var0 + 1][var3 - var1 + 1];

        for(int var5 = var0; var5 <= var2; ++var5) {
            for(int var6 = var1; var6 <= var3; ++var6) {
                var4[var5 - var0][var6 - var1] = this.chunkInfoCache
                    .computeIfAbsent(
                        ChunkPos.asLong(var5, var6),
                        param1x -> new RenderRegionCache.ChunkInfo(param0.getChunk(ChunkPos.getX(param1x), ChunkPos.getZ(param1x)))
                    );
            }
        }

        if (isAllEmpty(param1, param2, var0, var1, var4)) {
            return null;
        } else {
            RenderChunk[][] var7 = new RenderChunk[var2 - var0 + 1][var3 - var1 + 1];

            for(int var8 = var0; var8 <= var2; ++var8) {
                for(int var9 = var1; var9 <= var3; ++var9) {
                    var7[var8 - var0][var9 - var1] = var4[var8 - var0][var9 - var1].renderChunk();
                }
            }

            return new RenderChunkRegion(param0, var0, var1, var7);
        }
    }

    private static boolean isAllEmpty(BlockPos param0, BlockPos param1, int param2, int param3, RenderRegionCache.ChunkInfo[][] param4) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        int var2 = SectionPos.blockToSectionCoord(param1.getX());
        int var3 = SectionPos.blockToSectionCoord(param1.getZ());

        for(int var4 = var0; var4 <= var2; ++var4) {
            for(int var5 = var1; var5 <= var3; ++var5) {
                LevelChunk var6 = param4[var4 - param2][var5 - param3].chunk();
                if (!var6.isYSpaceEmpty(param0.getY(), param1.getY())) {
                    return false;
                }
            }
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    static final class ChunkInfo {
        private final LevelChunk chunk;
        @Nullable
        private RenderChunk renderChunk;

        ChunkInfo(LevelChunk param0) {
            this.chunk = param0;
        }

        public LevelChunk chunk() {
            return this.chunk;
        }

        public RenderChunk renderChunk() {
            if (this.renderChunk == null) {
                this.renderChunk = new RenderChunk(this.chunk);
            }

            return this.renderChunk;
        }
    }
}
