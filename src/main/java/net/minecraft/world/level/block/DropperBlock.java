package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class DropperBlock extends DispenserBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DropperBlock> CODEC = simpleCodec(DropperBlock::new);
    private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

    @Override
    public MapCodec<DropperBlock> codec() {
        return CODEC;
    }

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
    protected void dispenseFrom(ServerLevel param0, BlockState param1, BlockPos param2) {
        DispenserBlockEntity var0 = param0.getBlockEntity(param2, BlockEntityType.DROPPER).orElse(null);
        if (var0 == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", param2);
        } else {
            BlockSource var1 = new BlockSource(param0, param2, param1, var0);
            int var2 = var0.getRandomSlot(param0.random);
            if (var2 < 0) {
                param0.levelEvent(1001, param2, 0);
            } else {
                ItemStack var3 = var0.getItem(var2);
                if (!var3.isEmpty()) {
                    Direction var4 = param0.getBlockState(param2).getValue(FACING);
                    Container var5 = HopperBlockEntity.getContainerAt(param0, param2.relative(var4));
                    ItemStack var6;
                    if (var5 == null) {
                        var6 = DISPENSE_BEHAVIOUR.dispense(var1, var3);
                    } else {
                        var6 = HopperBlockEntity.addItem(var0, var5, var3.copy().split(1), var4.getOpposite());
                        if (var6.isEmpty()) {
                            var6 = var3.copy();
                            var6.shrink(1);
                        } else {
                            var6 = var3.copy();
                        }
                    }

                    var0.setItem(var2, var6);
                }
            }
        }
    }
}
