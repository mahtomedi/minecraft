package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractGlassBlock extends HalfTransparentBlock {
    protected AbstractGlassBlock(Block.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShadeBrightness(BlockState param0, BlockGetter param1, BlockPos param2) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }

    @Override
    public boolean isSuffocating(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return false;
    }
}
