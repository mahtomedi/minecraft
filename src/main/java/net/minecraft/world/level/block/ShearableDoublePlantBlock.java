package net.minecraft.world.level.block;

import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ShearableDoublePlantBlock extends DoublePlantBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;

    public ShearableDoublePlantBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        boolean var0 = super.canBeReplaced(param0, param1);
        return var0 && param1.getItemInHand().getItem() == this.asItem() ? false : var0;
    }
}
