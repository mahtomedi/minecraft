package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndRodBlock extends DirectionalBlock {
    protected static final VoxelShape Y_AXIS_AABB = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 16.0);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 6.0, 6.0, 16.0, 10.0, 10.0);

    protected EndRodBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(FACING, param1.mirror(param0.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch(param0.getValue(FACING).getAxis()) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getClickedFace();
        BlockState var1 = param0.getLevel().getBlockState(param0.getClickedPos().relative(var0.getOpposite()));
        return var1.getBlock() == this && var1.getValue(FACING) == var0
            ? this.defaultBlockState().setValue(FACING, var0.getOpposite())
            : this.defaultBlockState().setValue(FACING, var0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        Direction var0 = param0.getValue(FACING);
        double var1 = (double)param2.getX() + 0.55 - (double)(param3.nextFloat() * 0.1F);
        double var2 = (double)param2.getY() + 0.55 - (double)(param3.nextFloat() * 0.1F);
        double var3 = (double)param2.getZ() + 0.55 - (double)(param3.nextFloat() * 0.1F);
        double var4 = (double)(0.4F - (param3.nextFloat() + param3.nextFloat()) * 0.4F);
        if (param3.nextInt(5) == 0) {
            param1.addParticle(
                ParticleTypes.END_ROD,
                var1 + (double)var0.getStepX() * var4,
                var2 + (double)var0.getStepY() * var4,
                var3 + (double)var0.getStepZ() * var4,
                param3.nextGaussian() * 0.005,
                param3.nextGaussian() * 0.005,
                param3.nextGaussian() * 0.005
            );
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.NORMAL;
    }
}
