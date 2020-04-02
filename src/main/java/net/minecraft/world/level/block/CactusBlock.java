package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
    protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    protected CactusBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BlockPos var0 = param2.above();
        if (param1.isEmptyBlock(var0)) {
            int var1 = 1;

            while(param1.getBlockState(param2.below(var1)).getBlock() == this) {
                ++var1;
            }

            if (var1 < 3) {
                int var2 = param0.getValue(AGE);
                if (var2 == 15) {
                    param1.setBlockAndUpdate(var0, this.defaultBlockState());
                    BlockState var3 = param0.setValue(AGE, Integer.valueOf(0));
                    param1.setBlock(param2, var3, 4);
                    var3.neighborChanged(param1, var0, this, param2, false);
                } else {
                    param1.setBlock(param2, param0.setValue(AGE, Integer.valueOf(var2 + 1)), 4);
                }

            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return OUTLINE_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            BlockState var1 = param1.getBlockState(param2.relative(var0));
            Material var2 = var1.getMaterial();
            if (var2.isSolid() || param1.getFluidState(param2.relative(var0)).is(FluidTags.LAVA)) {
                return false;
            }
        }

        Block var3 = param1.getBlockState(param2.below()).getBlock();
        return (var3 == Blocks.CACTUS || var3 == Blocks.SAND || var3 == Blocks.RED_SAND) && !param1.getBlockState(param2.above()).getMaterial().isLiquid();
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        param3.hurt(DamageSource.CACTUS, 1.0F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
