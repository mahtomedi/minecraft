package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WitherWallSkullBlock extends WallSkullBlock {
    protected WitherWallSkullBlock(BlockBehaviour.Properties param0) {
        super(SkullBlock.Types.WITHER_SKELETON, param0);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        Blocks.WITHER_SKELETON_SKULL.setPlacedBy(param0, param1, param2, param3, param4);
    }
}
