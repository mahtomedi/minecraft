package net.minecraft.world.level;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
    default float getMoonBrightness() {
        return Dimension.MOON_BRIGHTNESS_PER_PHASE[this.getDimension().getMoonPhase(this.getLevelData().getDayTime())];
    }

    default float getTimeOfDay(float param0) {
        return this.getDimension().getTimeOfDay(this.getLevelData().getDayTime(), param0);
    }

    @OnlyIn(Dist.CLIENT)
    default int getMoonPhase() {
        return this.getDimension().getMoonPhase(this.getLevelData().getDayTime());
    }

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

    Level getLevel();

    LevelData getLevelData();

    DifficultyInstance getCurrentDifficultyAt(BlockPos var1);

    default Difficulty getDifficulty() {
        return this.getLevelData().getDifficulty();
    }

    ChunkSource getChunkSource();

    @Override
    default boolean hasChunk(int param0, int param1) {
        return this.getChunkSource().hasChunk(param0, param1);
    }

    Random getRandom();

    default void blockUpdated(BlockPos param0, Block param1) {
    }

    void playSound(@Nullable Player var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

    void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4);

    default int getHeight() {
        return this.dimensionType().hasCeiling() ? 128 : 256;
    }

    default void levelEvent(int param0, BlockPos param1, int param2) {
        this.levelEvent(null, param0, param1, param2);
    }

    @Override
    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return EntityGetter.super.getEntityCollisions(param0, param1, param2);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        return EntityGetter.super.isUnobstructed(param0, param1);
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types param0, BlockPos param1) {
        return LevelReader.super.getHeightmapPos(param0, param1);
    }
}
