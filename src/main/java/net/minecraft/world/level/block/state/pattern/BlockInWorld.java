package net.minecraft.world.level.block.state.pattern;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInWorld {
    private final LevelReader level;
    private final BlockPos pos;
    private final boolean loadChunks;
    private BlockState state;
    private BlockEntity entity;
    private boolean cachedEntity;

    public BlockInWorld(LevelReader param0, BlockPos param1, boolean param2) {
        this.level = param0;
        this.pos = param1.immutable();
        this.loadChunks = param2;
    }

    public BlockState getState() {
        if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos))) {
            this.state = this.level.getBlockState(this.pos);
        }

        return this.state;
    }

    @Nullable
    public BlockEntity getEntity() {
        if (this.entity == null && !this.cachedEntity) {
            this.entity = this.level.getBlockEntity(this.pos);
            this.cachedEntity = true;
        }

        return this.entity;
    }

    public LevelReader getLevel() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public static Predicate<BlockInWorld> hasState(Predicate<BlockState> param0) {
        return param1 -> param1 != null && param0.test(param1.getState());
    }
}
