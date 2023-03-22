package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallHangingSignBlock extends SignBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape PLANK_NORTHSOUTH = Block.box(0.0, 14.0, 6.0, 16.0, 16.0, 10.0);
    public static final VoxelShape PLANK_EASTWEST = Block.box(6.0, 14.0, 0.0, 10.0, 16.0, 16.0);
    public static final VoxelShape SHAPE_NORTHSOUTH = Shapes.or(PLANK_NORTHSOUTH, Block.box(1.0, 0.0, 7.0, 15.0, 10.0, 9.0));
    public static final VoxelShape SHAPE_EASTWEST = Shapes.or(PLANK_EASTWEST, Block.box(7.0, 0.0, 1.0, 9.0, 10.0, 15.0));
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
        ImmutableMap.of(Direction.NORTH, SHAPE_NORTHSOUTH, Direction.SOUTH, SHAPE_NORTHSOUTH, Direction.EAST, SHAPE_EASTWEST, Direction.WEST, SHAPE_EASTWEST)
    );

    public WallHangingSignBlock(BlockBehaviour.Properties param0, WoodType param1) {
        super(param0.sound(param1.hangingSignSoundType()), param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var1 = param1.getBlockEntity(param2);
        if (var1 instanceof SignBlockEntity var0) {
            ItemStack var1x = param3.getItemInHand(param4);
            SignText var2 = var0.getTextFacingPlayer(param3);
            if (!var2.hasAnyClickCommands(param3) && var1x.getItem() instanceof BlockItem) {
                return InteractionResult.PASS;
            }
        }

        return super.use(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return AABBS.get(param0.getValue(FACING));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.getShape(param0, param1, param2, CollisionContext.empty());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((Direction)param0.getValue(FACING)) {
            case EAST:
            case WEST:
                return PLANK_EASTWEST;
            default:
                return PLANK_NORTHSOUTH;
        }
    }

    public boolean canPlace(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING).getClockWise();
        Direction var1 = param0.getValue(FACING).getCounterClockWise();
        return this.canAttachTo(param1, param0, param2.relative(var0), var1) || this.canAttachTo(param1, param0, param2.relative(var1), var0);
    }

    public boolean canAttachTo(LevelReader param0, BlockState param1, BlockPos param2, Direction param3) {
        BlockState var0 = param0.getBlockState(param2);
        return var0.is(BlockTags.WALL_HANGING_SIGNS)
            ? var0.getValue(FACING).getAxis().test(param1.getValue(FACING))
            : var0.isFaceSturdy(param0, param2, param3, SupportType.FULL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = this.defaultBlockState();
        FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
        LevelReader var2 = param0.getLevel();
        BlockPos var3 = param0.getClickedPos();

        for(Direction var4 : param0.getNearestLookingDirections()) {
            if (var4.getAxis().isHorizontal() && !var4.getAxis().test(param0.getClickedFace())) {
                Direction var5 = var4.getOpposite();
                var0 = var0.setValue(FACING, var5);
                if (var0.canSurvive(var2, var3) && this.canPlace(var0, var2, var3)) {
                    return var0.setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
                }
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1.getAxis() == param0.getValue(FACING).getClockWise().getAxis() && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public float getYRotationDegrees(BlockState param0) {
        return param0.getValue(FACING).toYRot();
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new HangingSignBlockEntity(param0, param1);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
