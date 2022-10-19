package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
    @Override
    default long dayTime() {
        return this.getLevelData().getDayTime();
    }

    long nextSubTickCount();

    LevelTickAccess<Block> getBlockTicks();

    private <T> ScheduledTick<T> createTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        return new ScheduledTick<>(param1, param0, this.getLevelData().getGameTime() + (long)param2, param3, this.nextSubTickCount());
    }

    private <T> ScheduledTick<T> createTick(BlockPos param0, T param1, int param2) {
        return new ScheduledTick<>(param1, param0, this.getLevelData().getGameTime() + (long)param2, this.nextSubTickCount());
    }

    default void scheduleTick(BlockPos param0, Block param1, int param2, TickPriority param3) {
        this.getBlockTicks().schedule(this.createTick(param0, param1, param2, param3));
    }

    default void scheduleTick(BlockPos param0, Block param1, int param2) {
        this.getBlockTicks().schedule(this.createTick(param0, param1, param2));
    }

    LevelTickAccess<Fluid> getFluidTicks();

    default void scheduleTick(BlockPos param0, Fluid param1, int param2, TickPriority param3) {
        this.getFluidTicks().schedule(this.createTick(param0, param1, param2, param3));
    }

    default void scheduleTick(BlockPos param0, Fluid param1, int param2) {
        this.getFluidTicks().schedule(this.createTick(param0, param1, param2));
    }

    LevelData getLevelData();

    DifficultyInstance getCurrentDifficultyAt(BlockPos var1);

    @Nullable
    MinecraftServer getServer();

    default Difficulty getDifficulty() {
        return this.getLevelData().getDifficulty();
    }

    ChunkSource getChunkSource();

    @Override
    default boolean hasChunk(int param0, int param1) {
        return this.getChunkSource().hasChunk(param0, param1);
    }

    RandomSource getRandom();

    default void blockUpdated(BlockPos param0, Block param1) {
    }

    default void neighborShapeChanged(Direction param0, BlockState param1, BlockPos param2, BlockPos param3, int param4, int param5) {
        NeighborUpdater.executeShapeUpdate(this, param0, param1, param2, param3, param4, param5 - 1);
    }

    default void playSound(@Nullable Player param0, BlockPos param1, SoundEvent param2, SoundSource param3) {
        this.playSound(param0, param1, param2, param3, 1.0F, 1.0F);
    }

    void playSound(@Nullable Player var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

    void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4);

    default void levelEvent(int param0, BlockPos param1, int param2) {
        this.levelEvent(null, param0, param1, param2);
    }

    void gameEvent(GameEvent var1, Vec3 var2, GameEvent.Context var3);

    default void gameEvent(@Nullable Entity param0, GameEvent param1, Vec3 param2) {
        this.gameEvent(param1, param2, new GameEvent.Context(param0, null));
    }

    default void gameEvent(@Nullable Entity param0, GameEvent param1, BlockPos param2) {
        this.gameEvent(param1, param2, new GameEvent.Context(param0, null));
    }

    default void gameEvent(GameEvent param0, BlockPos param1, GameEvent.Context param2) {
        this.gameEvent(param0, Vec3.atCenterOf(param1), param2);
    }
}
