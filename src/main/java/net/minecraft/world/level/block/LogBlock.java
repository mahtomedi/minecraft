package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

public class LogBlock extends RotatedPillarBlock {
    private final MaterialColor woodMaterialColor;

    public LogBlock(MaterialColor param0, Block.Properties param1) {
        super(param1);
        this.woodMaterialColor = param0;
    }

    @Override
    public MaterialColor getMapColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.getValue(AXIS) == Direction.Axis.Y ? this.woodMaterialColor : this.materialColor;
    }
}
