package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape COLLIDER = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final int SHRIEKING_TICKS = 90;
    public static final double TOP_Y = COLLIDER.max(Direction.Axis.Y);

    public SculkShriekerBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHRIEKING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(SHRIEKING);
        param0.add(WATERLOGGED);
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, BlockState param2, Entity param3) {
        if (param3 instanceof Player && param0 instanceof ServerLevel var0) {
            shriek(var0, param2, param1);
        }

        super.stepOn(param0, param1, param2, param3);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(SHRIEKING)) {
            param1.setBlock(param2, param0.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
            getWardenSpawnTracker(param1, param2).ifPresent(param2x -> param2x.triggerWarningEvent(param1, param2));
        }

    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param1 instanceof ServerLevel var0 && param1.hasNeighborSignal(param2)) {
            shriek(var0, param0, param2);
        }

    }

    public static boolean canShriek(ServerLevel param0, BlockPos param1, BlockState param2) {
        return !param2.getValue(SHRIEKING)
            && getWardenSpawnTracker(param0, param1).map(param2x -> param2x.canPrepareWarningEvent(param0, param1)).orElse(false);
    }

    public static void shriek(ServerLevel param0, BlockState param1, BlockPos param2) {
        if (canShriek(param0, param2, param1)) {
            getWardenSpawnTracker(param0, param2).filter(param2x -> param2x.prepareWarningEvent(param0, param2)).ifPresent(param3 -> {
                param0.setBlock(param2, param1.setValue(SHRIEKING, Boolean.valueOf(true)), 2);
                param0.scheduleTick(param2, param1.getBlock(), 90);
                param0.levelEvent(3007, param2, 0);
            });
        }
    }

    private static Optional<WardenSpawnTracker> getWardenSpawnTracker(ServerLevel param0, BlockPos param1) {
        Player var0 = param0.getNearestPlayer(
            (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), 16.0, EntitySelector.NO_SPECTATORS.and(Entity::isAlive)
        );
        return var0 == null ? Optional.empty() : Optional.of(var0.getWardenSpawnTracker());
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return COLLIDER;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return COLLIDER;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new SculkShriekerBlockEntity(param0, param1);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        this.tryDropExperience(param1, param2, param3, ConstantInt.of(5));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel param0, T param1) {
        return param1 instanceof SculkShriekerBlockEntity var0 ? var0.getListener() : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return !param0.isClientSide
            ? BaseEntityBlock.createTickerHelper(
                param2, BlockEntityType.SCULK_SHRIEKER, (param0x, param1x, param2x, param3) -> param3.getListener().tick(param0x)
            )
            : null;
    }
}
