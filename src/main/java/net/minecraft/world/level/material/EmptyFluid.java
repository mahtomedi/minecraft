package net.minecraft.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyFluid extends Fluid {
    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.SOLID;
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    public boolean canBeReplacedWith(FluidState param0, BlockGetter param1, BlockPos param2, Fluid param3, Direction param4) {
        return true;
    }

    @Override
    public Vec3 getFlow(BlockGetter param0, BlockPos param1, FluidState param2) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 0;
    }

    @Override
    protected boolean isEmpty() {
        return true;
    }

    @Override
    protected float getExplosionResistance() {
        return 0.0F;
    }

    @Override
    public float getHeight(FluidState param0, BlockGetter param1, BlockPos param2) {
        return 0.0F;
    }

    @Override
    public float getOwnHeight(FluidState param0) {
        return 0.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState param0) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState param0) {
        return false;
    }

    @Override
    public int getAmount(FluidState param0) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }
}
