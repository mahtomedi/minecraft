package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public abstract class AbstractProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    public ItemStack execute(BlockSource param0, ItemStack param1) {
        Level var0 = param0.getLevel();
        Position var1 = DispenserBlock.getDispensePosition(param0);
        Direction var2 = param0.getBlockState().getValue(DispenserBlock.FACING);
        Projectile var3 = this.getProjectile(var0, var1, param1);
        var3.shoot((double)var2.getStepX(), (double)((float)var2.getStepY() + 0.1F), (double)var2.getStepZ(), this.getPower(), this.getUncertainty());
        var0.addFreshEntity((Entity)var3);
        param1.shrink(1);
        return param1;
    }

    @Override
    protected void playSound(BlockSource param0) {
        param0.getLevel().levelEvent(1002, param0.getPos(), 0);
    }

    protected abstract Projectile getProjectile(Level var1, Position var2, ItemStack var3);

    protected float getUncertainty() {
        return 6.0F;
    }

    protected float getPower() {
        return 1.1F;
    }
}
