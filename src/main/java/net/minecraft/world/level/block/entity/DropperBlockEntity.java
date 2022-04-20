package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlockEntity extends DispenserBlockEntity {
    public DropperBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.DROPPER, param0, param1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.dropper");
    }
}
