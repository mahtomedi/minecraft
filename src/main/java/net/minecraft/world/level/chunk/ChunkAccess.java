package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

public interface ChunkAccess extends BlockGetter, FeatureAccess {
    default GameEventDispatcher getEventDispatcher(int param0) {
        return GameEventDispatcher.NOOP;
    }

    @Nullable
    BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    void setBlockEntity(BlockEntity var1);

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
        return var0 == null ? this.getMinBuildHeight() : var0.bottomBlockY();
    }

    Set<BlockPos> getBlockEntitiesPos();

    LevelChunkSection[] getSections();

    Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    void setHeightmap(Heightmap.Types var1, long[] var2);

    Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types var1);

    int getHeight(Heightmap.Types var1, int var2, int var3);

    ChunkPos getPos();

    Map<StructureFeature<?>, StructureStart<?>> getAllStarts();

    void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> var1);

    default boolean isYSpaceEmpty(int param0, int param1) {
        if (param0 < this.getMinBuildHeight()) {
            param0 = this.getMinBuildHeight();
        }

        if (param1 >= this.getMaxBuildHeight()) {
            param1 = this.getMaxBuildHeight() - 1;
        }

        for(int var0 = param0; var0 <= param1; var0 += 16) {
            if (!LevelChunkSection.isEmpty(this.getSections()[this.getSectionIndex(var0)])) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    ChunkBiomeContainer getBiomes();

    void setUnsaved(boolean var1);

    boolean isUnsaved();

    ChunkStatus getStatus();

    void removeBlockEntity(BlockPos var1);

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

    Stream<BlockPos> getLights();

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

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
