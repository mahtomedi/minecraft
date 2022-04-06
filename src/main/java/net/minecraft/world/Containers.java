package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers {
    public static void dropContents(Level param0, BlockPos param1, Container param2) {
        dropContents(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), param2);
    }

    public static void dropContents(Level param0, Entity param1, Container param2) {
        dropContents(param0, param1.getX(), param1.getY(), param1.getZ(), param2);
    }

    private static void dropContents(Level param0, double param1, double param2, double param3, Container param4) {
        for(int var0 = 0; var0 < param4.getContainerSize(); ++var0) {
            dropItemStack(param0, param1, param2, param3, param4.getItem(var0));
        }

    }

    public static void dropContents(Level param0, BlockPos param1, NonNullList<ItemStack> param2) {
        param2.forEach(param2x -> dropItemStack(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), param2x));
    }

    public static void dropItemStack(Level param0, double param1, double param2, double param3, ItemStack param4) {
        double var0 = (double)EntityType.ITEM.getWidth();
        double var1 = 1.0 - var0;
        double var2 = var0 / 2.0;
        double var3 = Math.floor(param1) + param0.random.nextDouble() * var1 + var2;
        double var4 = Math.floor(param2) + param0.random.nextDouble() * var1;
        double var5 = Math.floor(param3) + param0.random.nextDouble() * var1 + var2;

        while(!param4.isEmpty()) {
            ItemEntity var6 = new ItemEntity(param0, var3, var4, var5, param4.split(param0.random.nextInt(21) + 10));
            float var7 = 0.05F;
            var6.setDeltaMovement(param0.random.nextGaussian() * 0.05F, param0.random.nextGaussian() * 0.05F + 0.2F, param0.random.nextGaussian() * 0.05F);
            param0.addFreshEntity(var6);
        }

    }
}
