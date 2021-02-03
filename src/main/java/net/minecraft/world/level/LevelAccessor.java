package net.minecraft.world.level;

import java.util.Random;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
    @Override
    default long dayTime() {
        return this.getLevelData().getDayTime();
    }

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

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

    default void levelEvent(int param0, BlockPos param1, int param2) {
        this.levelEvent(null, param0, param1, param2);
    }

    void gameEvent(@Nullable Entity var1, GameEvent var2, BlockPos var3);

    default void gameEvent(GameEvent param0, BlockPos param1) {
        this.gameEvent(null, param0, param1);
    }

    default void gameEvent(GameEvent param0, Entity param1) {
        this.gameEvent(null, param0, param1.blockPosition());
    }

    default void gameEvent(@Nullable Entity param0, GameEvent param1, Entity param2) {
        this.gameEvent(param0, param1, param2.blockPosition());
    }
}
