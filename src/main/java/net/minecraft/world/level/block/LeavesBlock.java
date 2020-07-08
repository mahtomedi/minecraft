package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LeavesBlock extends Block {
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

    public LeavesBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(DISTANCE) == 7 && !param0.getValue(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.getValue(PERSISTENT) && param0.getValue(DISTANCE) == 7) {
            dropResources(param0, param1, param2);
            param1.removeBlock(param2, false);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        param1.setBlock(param2, updateDistance(param0, param1, param2), 3);
    }

    @Override
    public int getLightBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        return 1;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        int var0 = getDistanceAt(param2) + 1;
        if (var0 != 1 || param0.getValue(DISTANCE) != var0) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        return param0;
    }

    private static BlockState updateDistance(BlockState param0, LevelAccessor param1, BlockPos param2) {
        int var0 = 7;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(Direction var2 : Direction.values()) {
            var1.setWithOffset(param2, var2);
            var0 = Math.min(var0, getDistanceAt(param1.getBlockState(var1)) + 1);
            if (var0 == 1) {
                break;
            }
        }

        return param0.setValue(DISTANCE, Integer.valueOf(var0));
    }

    private static int getDistanceAt(BlockState param0) {
        if (BlockTags.LOGS.contains(param0.getBlock())) {
            return 0;
        } else {
            return param0.getBlock() instanceof LeavesBlock ? param0.getValue(DISTANCE) : 7;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param1.isRainingAt(param2.above())) {
            if (param3.nextInt(15) == 1) {
                BlockPos var0 = param2.below();
                BlockState var1 = param1.getBlockState(var0);
                if (!var1.canOcclude() || !var1.isFaceSturdy(param1, var0, Direction.UP)) {
                    double var2 = (double)param2.getX() + param3.nextDouble();
                    double var3 = (double)param2.getY() - 0.05;
                    double var4 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.DRIPPING_WATER, var2, var3, var4, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(DISTANCE, PERSISTENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)), param0.getLevel(), param0.getClickedPos());
    }
}
