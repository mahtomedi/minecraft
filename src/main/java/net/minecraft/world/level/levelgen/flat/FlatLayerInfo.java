package net.minecraft.world.level.levelgen.flat;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FlatLayerInfo {
    private final BlockState blockState;
    private final int height;
    private int start;

    public FlatLayerInfo(int param0, Block param1) {
        this.height = param0;
        this.blockState = param1.defaultBlockState();
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int param0) {
        this.start = param0;
    }

    @Override
    public String toString() {
        return (this.height != 1 ? this.height + "*" : "") + Registry.BLOCK.getKey(this.blockState.getBlock());
    }
}
