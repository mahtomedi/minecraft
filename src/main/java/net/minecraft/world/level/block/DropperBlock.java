package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlock extends DispenserBlock {
    private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

    public DropperBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    protected DispenseItemBehavior getDispenseMethod(ItemStack param0) {
        return DISPENSE_BEHAVIOUR;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new DropperBlockEntity(param0, param1);
    }

    @Override
    protected void dispenseFrom(ServerLevel param0, BlockPos param1) {
        BlockSourceImpl var0 = new BlockSourceImpl(param0, param1);
        DispenserBlockEntity var1 = var0.getEntity();
        int var2 = var1.getRandomSlot(param0.random);
        if (var2 < 0) {
            param0.levelEvent(1001, param1, 0);
        } else {
            ItemStack var3 = var1.getItem(var2);
            if (!var3.isEmpty()) {
                Direction var4 = param0.getBlockState(param1).getValue(FACING);
                Container var5 = HopperBlockEntity.getContainerAt(param0, param1.relative(var4));
                ItemStack var6;
                if (var5 == null) {
                    var6 = DISPENSE_BEHAVIOUR.dispense(var0, var3);
                } else {
                    var6 = HopperBlockEntity.addItem(var1, var5, var3.copy().split(1), var4.getOpposite());
                    if (var6.isEmpty()) {
                        var6 = var3.copy();
                        var6.shrink(1);
                    } else {
                        var6 = var3.copy();
                    }
                }

                var1.setItem(var2, var6);
            }
        }
    }
}
