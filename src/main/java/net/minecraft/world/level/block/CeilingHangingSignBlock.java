package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CeilingHangingSignBlock extends SignBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final float AABB_OFFSET = 5.0F;
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final Map<Integer, VoxelShape> AABBS = Maps.newHashMap(
        ImmutableMap.of(
            0,
            Block.box(1.0, 0.0, 7.0, 15.0, 10.0, 9.0),
            4,
            Block.box(7.0, 0.0, 1.0, 9.0, 10.0, 15.0),
            8,
            Block.box(1.0, 0.0, 7.0, 15.0, 10.0, 9.0),
            12,
            Block.box(7.0, 0.0, 1.0, 9.0, 10.0, 15.0)
        )
    );

    public CeilingHangingSignBlock(BlockBehaviour.Properties param0, WoodType param1) {
        super(param0, param1);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(ROTATION, Integer.valueOf(0))
                .setValue(ATTACHED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var1 = param1.getBlockEntity(param2);
        if (var1 instanceof SignBlockEntity var0) {
            ItemStack var1x = param3.getItemInHand(param4);
            if (!var0.hasAnyClickCommands(param3) && var1x.getItem() instanceof BlockItem) {
                return InteractionResult.PASS;
            }
        }

        return super.use(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return param1.getBlockState(param2.above()).isFaceSturdy(param1, param2.above(), Direction.DOWN, SupportType.CENTER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Level var0 = param0.getLevel();
        FluidState var1 = var0.getFluidState(param0.getClickedPos());
        BlockPos var2 = param0.getClickedPos().above();
        BlockState var3 = var0.getBlockState(var2);
        boolean var4 = var3.is(BlockTags.ALL_HANGING_SIGNS);
        Direction var5 = Direction.fromYRot((double)param0.getRotation());
        boolean var6 = !Block.isFaceFull(var3.getCollisionShape(var0, var2), Direction.DOWN) || param0.isSecondaryUseActive();
        if (var4 && !param0.isSecondaryUseActive()) {
            if (var3.hasProperty(WallHangingSignBlock.FACING)) {
                Direction var7 = var3.getValue(WallHangingSignBlock.FACING);
                if (var7.getAxis().test(var5)) {
                    var6 = false;
                }
            } else if (var3.hasProperty(ROTATION)) {
                Optional<Direction> var8 = RotationSegment.convertToDirection(var3.getValue(ROTATION));
                if (var8.isPresent() && var8.get().getAxis().test(var5)) {
                    var6 = false;
                }
            }
        }

        int var9 = !var6 ? RotationSegment.convertToSegment(var5) : RotationSegment.convertToSegment(param0.getRotation());
        return this.defaultBlockState()
            .setValue(ATTACHED, Boolean.valueOf(var6))
            .setValue(ROTATION, Integer.valueOf(var9))
            .setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        VoxelShape var0 = AABBS.get(param0.getValue(ROTATION));
        return var0 == null ? SHAPE : var0;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.getShape(param0, param1, param2, CollisionContext.empty());
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.UP && !this.canSurvive(param0, param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(ROTATION, Integer.valueOf(param1.rotate(param0.getValue(ROTATION), 16)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(ROTATION, Integer.valueOf(param1.mirror(param0.getValue(ROTATION), 16)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(ROTATION, ATTACHED, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new HangingSignBlockEntity(param0, param1);
    }
}
