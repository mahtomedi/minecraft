package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
    protected BaseEntityBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        super.triggerEvent(param0, param1, param2, param3, param4);
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 == null ? false : var0.triggerEvent(param3, param4);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 instanceof MenuProvider ? (MenuProvider)var0 : null;
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> param0, BlockEntityType<E> param1, BlockEntityTicker<? super E> param2
    ) {
        return param1 == param0 ? param2 : null;
    }
}
