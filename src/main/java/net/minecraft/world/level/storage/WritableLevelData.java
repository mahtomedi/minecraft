package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
    void setXSpawn(int var1);

    void setYSpawn(int var1);

    void setZSpawn(int var1);

    default void setSpawn(BlockPos param0) {
        this.setXSpawn(param0.getX());
        this.setYSpawn(param0.getY());
        this.setZSpawn(param0.getZ());
    }
}
