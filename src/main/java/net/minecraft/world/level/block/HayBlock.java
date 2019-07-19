package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class HayBlock extends RotatedPillarBlock {
    public HayBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        param2.causeFallDamage(param3, 0.2F);
    }
}
