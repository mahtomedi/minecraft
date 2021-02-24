package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class BulkSectionAccess implements AutoCloseable {
    private final LevelAccessor level;
    private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap<>();
    @Nullable
    private LevelChunkSection lastSection;
    private long lastSectionKey;

    public BulkSectionAccess(LevelAccessor param0) {
        this.level = param0;
    }

    public LevelChunkSection getSection(BlockPos param0) {
        long var0 = SectionPos.asLong(param0);
        if (this.lastSection != null && this.lastSectionKey == var0) {
            return this.lastSection;
        } else {
            this.lastSection = this.acquiredSections.computeIfAbsent(var0, param1 -> {
                ChunkAccess var0x = this.level.getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()));
                LevelChunkSection var1x = var0x.getOrCreateSection(var0x.getSectionIndex(param0.getY()));
                var1x.acquire();
                return var1x;
            });
            this.lastSectionKey = var0;
            return this.lastSection;
        }
    }

    public BlockState getBlockState(BlockPos param0) {
        LevelChunkSection var0 = this.getSection(param0);
        int var1 = SectionPos.sectionRelative(param0.getX());
        int var2 = SectionPos.sectionRelative(param0.getY());
        int var3 = SectionPos.sectionRelative(param0.getZ());
        return var0.getBlockState(var1, var2, var3);
    }

    @Override
    public void close() {
        for(LevelChunkSection var0 : this.acquiredSections.values()) {
            var0.release();
        }

    }
}
