package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
    boolean setBlock(BlockPos var1, BlockState var2, int var3, int var4);

    default boolean setBlock(BlockPos param0, BlockState param1, int param2) {
        return this.setBlock(param0, param1, param2, 512);
    }

    boolean removeBlock(BlockPos var1, boolean var2);

    default boolean destroyBlock(BlockPos param0, boolean param1) {
        return this.destroyBlock(param0, param1, null);
    }

    default boolean destroyBlock(BlockPos param0, boolean param1, @Nullable Entity param2) {
        return this.destroyBlock(param0, param1, param2, 512);
    }

    boolean destroyBlock(BlockPos var1, boolean var2, @Nullable Entity var3, int var4);

    default boolean addFreshEntity(Entity param0) {
        return false;
    }
}
