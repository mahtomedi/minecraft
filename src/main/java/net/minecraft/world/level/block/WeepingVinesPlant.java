package net.minecraft.world.level.block;

import net.minecraft.core.Direction;

public class WeepingVinesPlant extends GrowingPlantBodyBlock {
    public WeepingVinesPlant(Block.Properties param0) {
        super(param0, Direction.DOWN, NetherVines.SHAPE, false);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock)Blocks.WEEPING_VINES;
    }
}
