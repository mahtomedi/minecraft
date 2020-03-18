package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TwistingVinesPlant extends GrowingPlantBodyBlock {
    public TwistingVinesPlant(BlockBehaviour.Properties param0) {
        super(param0, Direction.UP, NetherVines.SHAPE, false);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock)Blocks.TWISTING_VINES;
    }
}
