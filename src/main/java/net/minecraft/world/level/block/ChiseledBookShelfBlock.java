package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
    public static final IntegerProperty BOOKS_STORED = BlockStateProperties.BOOKS_STORED;
    public static final IntegerProperty LAST_INTERACTION_BOOK_SLOT = BlockStateProperties.LAST_INTERACTION_BOOK_SLOT;

    public ChiseledBookShelfBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(BOOKS_STORED, Integer.valueOf(0))
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(LAST_INTERACTION_BOOK_SLOT, Integer.valueOf(0))
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var2 = param1.getBlockEntity(param2);
        if (var2 instanceof ChiseledBookShelfBlockEntity var0) {
            if (param1.isClientSide()) {
                return InteractionResult.SUCCESS;
            } else {
                ItemStack var2x = param3.getItemInHand(param4);
                return var2x.is(ItemTags.BOOKSHELF_BOOKS) ? tryAddBook(param1, param2, param3, var0, var2x) : tryRemoveBook(param1, param2, param3, var0);
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static InteractionResult tryRemoveBook(Level param0, BlockPos param1, Player param2, ChiseledBookShelfBlockEntity param3) {
        if (!param3.isEmpty()) {
            ItemStack var0 = param3.removeBook();
            param0.playSound(null, param1, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            param0.gameEvent(param2, GameEvent.BLOCK_CHANGE, param1);
            if (!param2.getInventory().add(var0)) {
                param2.drop(var0, false);
            }
        }

        return InteractionResult.CONSUME;
    }

    private static InteractionResult tryAddBook(Level param0, BlockPos param1, Player param2, ChiseledBookShelfBlockEntity param3, ItemStack param4) {
        if (param3.addBook(param4.split(1))) {
            param0.playSound(null, param1, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (param2.isCreative()) {
                param4.grow(1);
            }

            param0.gameEvent(param2, GameEvent.BLOCK_CHANGE, param1);
        } else {
            param4.grow(1);
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new ChiseledBookShelfBlockEntity(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(BOOKS_STORED).add(LAST_INTERACTION_BOOK_SLOT).add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof ChiseledBookShelfBlockEntity var1) {
                List<ItemStack> var2 = var1.removeAllBooksWithoutBlockStateUpdate();
                var2.forEach(param2x -> Containers.dropItemStack(param1, (double)param2.getX(), (double)param2.getY(), (double)param2.getZ(), param2x));
                param1.updateNeighbourForOutputSignal(param2, this);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(LAST_INTERACTION_BOOK_SLOT);
    }
}
