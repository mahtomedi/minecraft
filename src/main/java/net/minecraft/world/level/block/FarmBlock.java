package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmBlock extends Block {
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    public static final int MAX_MOISTURE = 7;

    protected FarmBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && !param0.canSurvive(param3, param4)) {
            param3.scheduleTick(param4, this, 1);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.above());
        return !var0.getMaterial().isSolid() || var0.getBlock() instanceof FenceGateBlock || var0.getBlock() instanceof MovingPistonBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return !this.defaultBlockState().canSurvive(param0.getLevel(), param0.getClickedPos())
            ? Blocks.DIRT.defaultBlockState()
            : super.getStateForPlacement(param0);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!param0.canSurvive(param1, param2)) {
            turnToDirt(param0, param1, param2);
        }

    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        int var0 = param0.getValue(MOISTURE);
        if (!isNearWater(param1, param2) && !param1.isRainingAt(param2.above())) {
            if (var0 > 0) {
                param1.setBlock(param2, param0.setValue(MOISTURE, Integer.valueOf(var0 - 1)), 2);
            } else if (!isUnderCrops(param1, param2)) {
                turnToDirt(param0, param1, param2);
            }
        } else if (var0 < 7) {
            param1.setBlock(param2, param0.setValue(MOISTURE, Integer.valueOf(7)), 2);
        }

    }

    @Override
    public void fallOn(Level param0, BlockState param1, BlockPos param2, Entity param3, float param4) {
        if (!param0.isClientSide
            && param0.random.nextFloat() < param4 - 0.5F
            && param3 instanceof LivingEntity
            && (param3 instanceof Player || param0.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
            && param3.getBbWidth() * param3.getBbWidth() * param3.getBbHeight() > 0.512F) {
            turnToDirt(param1, param0, param2);
        }

        super.fallOn(param0, param1, param2, param3, param4);
    }

    public static void turnToDirt(BlockState param0, Level param1, BlockPos param2) {
        param1.setBlockAndUpdate(param2, pushEntitiesUp(param0, Blocks.DIRT.defaultBlockState(), param1, param2));
    }

    private static boolean isUnderCrops(BlockGetter param0, BlockPos param1) {
        Block var0 = param0.getBlockState(param1.above()).getBlock();
        return var0 instanceof CropBlock || var0 instanceof StemBlock || var0 instanceof AttachedStemBlock;
    }

    private static boolean isNearWater(LevelReader param0, BlockPos param1) {
        for(BlockPos var0 : BlockPos.betweenClosed(param1.offset(-4, 0, -4), param1.offset(4, 1, 4))) {
            if (param0.getFluidState(var0).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(MOISTURE);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
