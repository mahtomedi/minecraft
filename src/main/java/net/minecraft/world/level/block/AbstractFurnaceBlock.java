package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractFurnaceBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(false)));
    }

    @Override
    protected abstract MapCodec<? extends AbstractFurnaceBlock> codec();

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            this.openContainer(param1, param2, param3);
            return InteractionResult.CONSUME;
        }
    }

    protected abstract void openContainer(Level var1, BlockPos var2, Player var3);

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof AbstractFurnaceBlockEntity) {
                ((AbstractFurnaceBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof AbstractFurnaceBlockEntity) {
                if (param1 instanceof ServerLevel) {
                    Containers.dropContents(param1, param2, (AbstractFurnaceBlockEntity)var0);
                    ((AbstractFurnaceBlockEntity)var0).getRecipesToAwardAndPopExperience((ServerLevel)param1, Vec3.atCenterOf(param2));
                }

                super.onRemove(param0, param1, param2, param3, param4);
                param1.updateNeighbourForOutputSignal(param2, this);
            } else {
                super.onRemove(param0, param1, param2, param3, param4);
            }

        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(param1.getBlockEntity(param2));
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
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
        param0.add(FACING, LIT);
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(
        Level param0, BlockEntityType<T> param1, BlockEntityType<? extends AbstractFurnaceBlockEntity> param2
    ) {
        return param0.isClientSide ? null : createTickerHelper(param1, param2, AbstractFurnaceBlockEntity::serverTick);
    }
}
