package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior {
    @Override
    public final ItemStack dispense(BlockSource param0, ItemStack param1) {
        ItemStack var0 = this.execute(param0, param1);
        this.playSound(param0);
        this.playAnimation(param0, param0.getBlockState().getValue(DispenserBlock.FACING));
        return var0;
    }

    protected ItemStack execute(BlockSource param0, ItemStack param1) {
        Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
        Position var1 = DispenserBlock.getDispensePosition(param0);
        ItemStack var2 = param1.split(1);
        spawnItem(param0.getLevel(), var2, 6, var0, var1);
        return param1;
    }

    public static void spawnItem(Level param0, ItemStack param1, int param2, Direction param3, Position param4) {
        double var0 = param4.x();
        double var1 = param4.y();
        double var2 = param4.z();
        if (param3.getAxis() == Direction.Axis.Y) {
            var1 -= 0.125;
        } else {
            var1 -= 0.15625;
        }

        ItemEntity var3 = new ItemEntity(param0, var0, var1, var2, param1);
        double var4 = param0.random.nextDouble() * 0.1 + 0.2;
        var3.setDeltaMovement(
            param0.random.triangle((double)param3.getStepX() * var4, 0.0172275 * (double)param2),
            param0.random.triangle(0.2, 0.0172275 * (double)param2),
            param0.random.triangle((double)param3.getStepZ() * var4, 0.0172275 * (double)param2)
        );
        param0.addFreshEntity(var3);
    }

    protected void playSound(BlockSource param0) {
        param0.getLevel().levelEvent(1000, param0.getPos(), 0);
    }

    protected void playAnimation(BlockSource param0, Direction param1) {
        param0.getLevel().levelEvent(2000, param0.getPos(), param1.get3DDataValue());
    }
}
