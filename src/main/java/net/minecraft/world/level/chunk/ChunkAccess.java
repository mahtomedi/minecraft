package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

public interface ChunkAccess extends FeatureAccess {
    @Nullable
    BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    void setBlockEntity(BlockPos var1, BlockEntity var2);

    void addEntity(Entity var1);

    @Nullable
    default LevelChunkSection getHighestSection() {
        LevelChunkSection[] var0 = this.getSections();

        for(int var1 = var0.length - 1; var1 >= 0; --var1) {
            LevelChunkSection var2 = var0[var1];
            if (!LevelChunkSection.isEmpty(var2)) {
                return var2;
            }
        }

        return null;
    }

    default int getHighestSectionPosition() {
        LevelChunkSection var0 = this.getHighestSection();
        return var0 == null ? 0 : var0.bottomBlockY();
    }

    Set<BlockPos> getBlockEntitiesPos();

    LevelChunkSection[] getSections();

    @Nullable
    LevelLightEngine getLightEngine();

    default int getRawBrightness(BlockPos param0, int param1, boolean param2) {
        LevelLightEngine var0 = this.getLightEngine();
        if (var0 != null && this.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
            int var1 = param2 ? var0.getLayerListener(LightLayer.SKY).getLightValue(param0) - param1 : 0;
            int var2 = var0.getLayerListener(LightLayer.BLOCK).getLightValue(param0);
            return Math.max(var2, var1);
        } else {
            return 0;
        }
    }

    Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    void setHeightmap(Heightmap.Types var1, long[] var2);

    Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types var1);

    int getHeight(Heightmap.Types var1, int var2, int var3);

    ChunkPos getPos();

    void setLastSaveTime(long var1);

    Map<String, StructureStart> getAllStarts();

    void setAllStarts(Map<String, StructureStart> var1);

    default Biome getBiome(BlockPos param0) {
        int var0 = param0.getX() & 15;
        int var1 = param0.getZ() & 15;
        return this.getBiomes()[var1 << 4 | var0];
    }

    default boolean isYSpaceEmpty(int param0, int param1) {
        if (param0 < 0) {
            param0 = 0;
        }

        if (param1 >= 256) {
            param1 = 255;
        }

        for(int var0 = param0; var0 <= param1; var0 += 16) {
            if (!LevelChunkSection.isEmpty(this.getSections()[var0 >> 4])) {
                return false;
            }
        }

        return true;
    }

    Biome[] getBiomes();

    void setUnsaved(boolean var1);

    boolean isUnsaved();

    ChunkStatus getStatus();

    void removeBlockEntity(BlockPos var1);

    void setLightEngine(LevelLightEngine var1);

    default void markPosForPostprocessing(BlockPos param0) {
        LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", param0);
    }

    ShortList[] getPostProcessing();

    default void addPackedPostProcess(short param0, int param1) {
        getOrCreateOffsetList(this.getPostProcessing(), param1).add(param0);
    }

    default void setBlockEntityNbt(CompoundTag param0) {
        LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
    }

    @Nullable
    CompoundTag getBlockEntityNbt(BlockPos var1);

    @Nullable
    CompoundTag getBlockEntityNbtForSaving(BlockPos var1);

    default void setBiomes(Biome[] param0) {
        throw new UnsupportedOperationException();
    }

    Stream<BlockPos> getLights();

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

    default BitSet getCarvingMask(GenerationStep.Carving param0) {
        throw new RuntimeException("Meaningless in this context");
    }

    UpgradeData getUpgradeData();

    void setInhabitedTime(long var1);

    long getInhabitedTime();

    static ShortList getOrCreateOffsetList(ShortList[] param0, int param1) {
        if (param0[param1] == null) {
            param0[param1] = new ShortArrayList();
        }

        return param0[param1];
    }

    boolean isLightCorrect();

    void setLightCorrect(boolean var1);
}
