package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;

public class SingleBaseStoneSource implements BaseStoneSource {
    private final BlockState state;

    public SingleBaseStoneSource(BlockState param0) {
        this.state = param0;
    }

    @Override
    public BlockState getBaseStone(int param0, int param1, int param2) {
        return this.state;
    }
}
