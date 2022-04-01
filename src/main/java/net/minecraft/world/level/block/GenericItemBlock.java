package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GenericItemBlock extends Block implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final IntegerProperty ITEM = BlockStateProperties.ITEM;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected GenericItemBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(ITEM, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(ITEM, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        LevelAccessor var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(var0.getFluidState(var1).getType() == Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return !param1.isEmptyBlock(param2.below());
    }

    @Nullable
    public static BlockState wrap(BlockState param0) {
        return genericBlockFromItem(param0.getBlock().asItem());
    }

    @Nullable
    public static BlockState unwrap(BlockState param0) {
        Item var0 = itemFromGenericBlock(param0);
        return var0 instanceof BlockItem var1 ? var1.getBlock().defaultBlockState() : null;
    }

    @Nullable
    public static BlockState genericBlockFromItem(Item param0) {
        if (param0 == Items.AIR) {
            return null;
        } else {
            int var0 = Registry.ITEM.getId(param0);
            return var0 != -1 ? Blocks.GENERIC_ITEM_BLOCK.defaultBlockState().setValue(ITEM, Integer.valueOf(var0)) : null;
        }
    }

    @Nullable
    public static Item itemFromGenericBlock(BlockState param0) {
        if (param0.hasProperty(ITEM)) {
            Item var0 = Registry.ITEM.byId(param0.getValue(ITEM));
            return var0 != Items.AIR ? var0 : null;
        } else {
            return null;
        }
    }

    public static final class GenericBlockItem extends BlockItem {
        public GenericBlockItem(Block param0, Item.Properties param1) {
            super(param0, param1);
        }

        @Override
        public Component getName(ItemStack param0) {
            return new TextComponent("How did we get here?");
        }
    }
}
