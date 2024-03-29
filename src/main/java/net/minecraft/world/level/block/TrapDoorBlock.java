package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<TrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(param0x -> param0x.type), propertiesCodec())
                .apply(param0, TrapDoorBlock::new)
    );
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final int AABB_THICKNESS = 3;
    protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
    protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
    private final BlockSetType type;

    @Override
    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    protected TrapDoorBlock(BlockSetType param0, BlockBehaviour.Properties param1) {
        super(param1.sound(param0.soundType()));
        this.type = param0;
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, Boolean.valueOf(false))
                .setValue(HALF, Half.BOTTOM)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (!param0.getValue(OPEN)) {
            return param0.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        } else {
            switch((Direction)param0.getValue(FACING)) {
                case NORTH:
                default:
                    return NORTH_OPEN_AABB;
                case SOUTH:
                    return SOUTH_OPEN_AABB;
                case WEST:
                    return WEST_OPEN_AABB;
                case EAST:
                    return EAST_OPEN_AABB;
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return param0.getValue(OPEN);
            case WATER:
                return param0.getValue(WATERLOGGED);
            case AIR:
                return param0.getValue(OPEN);
            default:
                return false;
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (!this.type.canOpenByHand()) {
            return InteractionResult.PASS;
        } else {
            this.toggle(param0, param1, param2, param3);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    @Override
    public void onExplosionHit(BlockState param0, Level param1, BlockPos param2, Explosion param3, BiConsumer<ItemStack, BlockPos> param4) {
        if (param3.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK
            && !param1.isClientSide()
            && this.type.canOpenByWindCharge()
            && !param0.getValue(POWERED)) {
            this.toggle(param0, param1, param2, null);
        }

        super.onExplosionHit(param0, param1, param2, param3, param4);
    }

    private void toggle(BlockState param0, Level param1, BlockPos param2, @Nullable Player param3) {
        BlockState var0 = param0.cycle(OPEN);
        param1.setBlock(param2, var0, 2);
        if (var0.getValue(WATERLOGGED)) {
            param1.scheduleTick(param2, Fluids.WATER, Fluids.WATER.getTickDelay(param1));
        }

        this.playSound(param3, param1, param2, var0.getValue(OPEN));
    }

    protected void playSound(@Nullable Player param0, Level param1, BlockPos param2, boolean param3) {
        param1.playSound(
            param0,
            param2,
            param3 ? this.type.trapdoorOpen() : this.type.trapdoorClose(),
            SoundSource.BLOCKS,
            1.0F,
            param1.getRandom().nextFloat() * 0.1F + 0.9F
        );
        param1.gameEvent(param0, param3 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, param2);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            boolean var0 = param1.hasNeighborSignal(param2);
            if (var0 != param0.getValue(POWERED)) {
                if (param0.getValue(OPEN) != var0) {
                    param0 = param0.setValue(OPEN, Boolean.valueOf(var0));
                    this.playSound(null, param1, param2, var0);
                }

                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)), 2);
                if (param0.getValue(WATERLOGGED)) {
                    param1.scheduleTick(param2, Fluids.WATER, Fluids.WATER.getTickDelay(param1));
                }
            }

        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = this.defaultBlockState();
        FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
        Direction var2 = param0.getClickedFace();
        if (!param0.replacingClickedOnBlock() && var2.getAxis().isHorizontal()) {
            var0 = var0.setValue(FACING, var2)
                .setValue(HALF, param0.getClickLocation().y - (double)param0.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
        } else {
            var0 = var0.setValue(FACING, param0.getHorizontalDirection().getOpposite()).setValue(HALF, var2 == Direction.UP ? Half.BOTTOM : Half.TOP);
        }

        if (param0.getLevel().hasNeighborSignal(param0.getClickedPos())) {
            var0 = var0.setValue(OPEN, Boolean.valueOf(true)).setValue(POWERED, Boolean.valueOf(true));
        }

        return var0.setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    protected BlockSetType getType() {
        return this.type;
    }
}
