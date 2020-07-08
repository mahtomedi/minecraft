package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MushroomBlock extends BushBlock implements BonemealableBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);

    public MushroomBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param3.nextInt(25) == 0) {
            int var0 = 5;
            int var1 = 4;

            for(BlockPos var2 : BlockPos.betweenClosed(param2.offset(-4, -1, -4), param2.offset(4, 1, 4))) {
                if (param1.getBlockState(var2).is(this)) {
                    if (--var0 <= 0) {
                        return;
                    }
                }
            }

            BlockPos var3 = param2.offset(param3.nextInt(3) - 1, param3.nextInt(2) - param3.nextInt(2), param3.nextInt(3) - 1);

            for(int var4 = 0; var4 < 4; ++var4) {
                if (param1.isEmptyBlock(var3) && param0.canSurvive(param1, var3)) {
                    param2 = var3;
                }

                var3 = param2.offset(param3.nextInt(3) - 1, param3.nextInt(2) - param3.nextInt(2), param3.nextInt(3) - 1);
            }

            if (param1.isEmptyBlock(var3) && param0.canSurvive(param1, var3)) {
                param1.setBlock(var3, param0, 2);
            }
        }

    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.isSolidRender(param1, param2);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        if (var1.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        } else {
            return param1.getRawBrightness(param2, 0) < 13 && this.mayPlaceOn(var1, param1, var0);
        }
    }

    public boolean growMushroom(ServerLevel param0, BlockPos param1, BlockState param2, Random param3) {
        param0.removeBlock(param1, false);
        ConfiguredFeature<?, ?> var0;
        if (this == Blocks.BROWN_MUSHROOM) {
            var0 = Features.HUGE_BROWN_MUSHROOM;
        } else {
            if (this != Blocks.RED_MUSHROOM) {
                param0.setBlock(param1, param2, 3);
                return false;
            }

            var0 = Features.HUGE_RED_MUSHROOM;
        }

        if (var0.place(param0, param0.getChunkSource().getGenerator(), param3, param1)) {
            return true;
        } else {
            param0.setBlock(param1, param2, 3);
            return false;
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return (double)param1.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        this.growMushroom(param0, param2, param3, param1);
    }
}
