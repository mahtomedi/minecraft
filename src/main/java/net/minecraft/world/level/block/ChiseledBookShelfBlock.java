package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
    public static final MapCodec<ChiseledBookShelfBlock> CODEC = simpleCodec(ChiseledBookShelfBlock::new);
    private static final int MAX_BOOKS_IN_STORAGE = 6;
    public static final int BOOKS_PER_ROW = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED
    );

    @Override
    public MapCodec<ChiseledBookShelfBlock> codec() {
        return CODEC;
    }

    public ChiseledBookShelfBlock(BlockBehaviour.Properties param0) {
        super(param0);
        BlockState var0 = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

        for(BooleanProperty var1 : SLOT_OCCUPIED_PROPERTIES) {
            var0 = var0.setValue(var1, Boolean.valueOf(false));
        }

        this.registerDefaultState(var0);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var2 = param1.getBlockEntity(param2);
        if (var2 instanceof ChiseledBookShelfBlockEntity var0) {
            Optional<Vec2> var2x = getRelativeHitCoordinatesForBlockFace(param5, param0.getValue(HorizontalDirectionalBlock.FACING));
            if (var2x.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                int var3 = getHitSlot(var2x.get());
                if (param0.getValue(SLOT_OCCUPIED_PROPERTIES.get(var3))) {
                    removeBook(param1, param2, param3, var0, var3);
                    return InteractionResult.sidedSuccess(param1.isClientSide);
                } else {
                    ItemStack var4 = param3.getItemInHand(param4);
                    if (var4.is(ItemTags.BOOKSHELF_BOOKS)) {
                        addBook(param1, param2, param3, var0, var4, var3);
                        return InteractionResult.sidedSuccess(param1.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult param0, Direction param1) {
        Direction var0 = param0.getDirection();
        if (param1 != var0) {
            return Optional.empty();
        } else {
            BlockPos var1 = param0.getBlockPos().relative(var0);
            Vec3 var2 = param0.getLocation().subtract((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
            double var3 = var2.x();
            double var4 = var2.y();
            double var5 = var2.z();

            return switch(var0) {
                case NORTH -> Optional.of(new Vec2((float)(1.0 - var3), (float)var4));
                case SOUTH -> Optional.of(new Vec2((float)var3, (float)var4));
                case WEST -> Optional.of(new Vec2((float)var5, (float)var4));
                case EAST -> Optional.of(new Vec2((float)(1.0 - var5), (float)var4));
                case DOWN, UP -> Optional.empty();
            };
        }
    }

    private static int getHitSlot(Vec2 param0) {
        int var0 = param0.y >= 0.5F ? 0 : 1;
        int var1 = getSection(param0.x);
        return var1 + var0 * 3;
    }

    private static int getSection(float param0) {
        float var0 = 0.0625F;
        float var1 = 0.375F;
        if (param0 < 0.375F) {
            return 0;
        } else {
            float var2 = 0.6875F;
            return param0 < 0.6875F ? 1 : 2;
        }
    }

    private static void addBook(Level param0, BlockPos param1, Player param2, ChiseledBookShelfBlockEntity param3, ItemStack param4, int param5) {
        if (!param0.isClientSide) {
            param2.awardStat(Stats.ITEM_USED.get(param4.getItem()));
            SoundEvent var0 = param4.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            param3.setItem(param5, param4.split(1));
            param0.playSound(null, param1, var0, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (param2.isCreative()) {
                param4.grow(1);
            }

        }
    }

    private static void removeBook(Level param0, BlockPos param1, Player param2, ChiseledBookShelfBlockEntity param3, int param4) {
        if (!param0.isClientSide) {
            ItemStack var0 = param3.removeItem(param4, 1);
            SoundEvent var1 = var0.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
            param0.playSound(null, param1, var1, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!param2.getInventory().add(var0)) {
                param2.drop(var0, false);
            }

            param0.gameEvent(param2, GameEvent.BLOCK_CHANGE, param1);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new ChiseledBookShelfBlockEntity(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HorizontalDirectionalBlock.FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(param1 -> param0.add(param1));
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof ChiseledBookShelfBlockEntity var1 && !var1.isEmpty()) {
                for(int var2 = 0; var2 < 6; ++var2) {
                    ItemStack var3 = var1.getItem(var2);
                    if (!var3.isEmpty()) {
                        Containers.dropItemStack(param1, (double)param2.getX(), (double)param2.getY(), (double)param2.getZ(), var3);
                    }
                }

                var1.clearContent();
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
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(HorizontalDirectionalBlock.FACING, param1.rotate(param0.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        if (param1.isClientSide()) {
            return 0;
        } else {
            BlockEntity var5 = param1.getBlockEntity(param2);
            return var5 instanceof ChiseledBookShelfBlockEntity var0 ? var0.getLastInteractedSlot() + 1 : 0;
        }
    }
}
