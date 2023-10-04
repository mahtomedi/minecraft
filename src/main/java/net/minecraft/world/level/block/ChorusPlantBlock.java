package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ChorusPlantBlock extends PipeBlock {
    public static final MapCodec<ChorusPlantBlock> CODEC = simpleCodec(ChorusPlantBlock::new);

    @Override
    public MapCodec<ChorusPlantBlock> codec() {
        return CODEC;
    }

    protected ChorusPlantBlock(BlockBehaviour.Properties param0) {
        super(0.3125F, param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(UP, Boolean.valueOf(false))
                .setValue(DOWN, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return getStateWithConnections(param0.getLevel(), param0.getClickedPos(), this.defaultBlockState());
    }

    public static BlockState getStateWithConnections(BlockGetter param0, BlockPos param1, BlockState param2) {
        BlockState var0 = param0.getBlockState(param1.below());
        BlockState var1 = param0.getBlockState(param1.above());
        BlockState var2 = param0.getBlockState(param1.north());
        BlockState var3 = param0.getBlockState(param1.east());
        BlockState var4 = param0.getBlockState(param1.south());
        BlockState var5 = param0.getBlockState(param1.west());
        Block var6 = param2.getBlock();
        return param2.trySetValue(DOWN, Boolean.valueOf(var0.is(var6) || var0.is(Blocks.CHORUS_FLOWER) || var0.is(Blocks.END_STONE)))
            .trySetValue(UP, Boolean.valueOf(var1.is(var6) || var1.is(Blocks.CHORUS_FLOWER)))
            .trySetValue(NORTH, Boolean.valueOf(var2.is(var6) || var2.is(Blocks.CHORUS_FLOWER)))
            .trySetValue(EAST, Boolean.valueOf(var3.is(var6) || var3.is(Blocks.CHORUS_FLOWER)))
            .trySetValue(SOUTH, Boolean.valueOf(var4.is(var6) || var4.is(Blocks.CHORUS_FLOWER)))
            .trySetValue(WEST, Boolean.valueOf(var5.is(var6) || var5.is(Blocks.CHORUS_FLOWER)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            param3.scheduleTick(param4, this, 1);
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            boolean var0 = param2.is(this) || param2.is(Blocks.CHORUS_FLOWER) || param1 == Direction.DOWN && param2.is(Blocks.END_STONE);
            return param0.setValue(PROPERTY_BY_DIRECTION.get(param1), Boolean.valueOf(var0));
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.below());
        boolean var1 = !param1.getBlockState(param2.above()).isAir() && !var0.isAir();

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            BlockPos var3 = param2.relative(var2);
            BlockState var4 = param1.getBlockState(var3);
            if (var4.is(this)) {
                if (var1) {
                    return false;
                }

                BlockState var5 = param1.getBlockState(var3.below());
                if (var5.is(this) || var5.is(Blocks.END_STONE)) {
                    return true;
                }
            }
        }

        return var0.is(this) || var0.is(Blocks.END_STONE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
