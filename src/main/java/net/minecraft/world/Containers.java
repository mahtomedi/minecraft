package net.minecraft.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers {
    private static final Random RANDOM = new Random();

    public static void dropContents(Level param0, BlockPos param1, Container param2) {
        dropContents(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), param2);
    }

    public static void dropContents(Level param0, Entity param1, Container param2) {
        dropContents(param0, param1.x, param1.y, param1.z, param2);
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
        double var3 = Math.floor(param1) + RANDOM.nextDouble() * var1 + var2;
        double var4 = Math.floor(param2) + RANDOM.nextDouble() * var1;
        double var5 = Math.floor(param3) + RANDOM.nextDouble() * var1 + var2;

        while(!param4.isEmpty()) {
            ItemEntity var6 = new ItemEntity(param0, var3, var4, var5, param4.split(RANDOM.nextInt(21) + 10));
            float var7 = 0.05F;
            var6.setDeltaMovement(RANDOM.nextGaussian() * 0.05F, RANDOM.nextGaussian() * 0.05F + 0.2F, RANDOM.nextGaussian() * 0.05F);
            param0.addFreshEntity(var6);
        }

    }
}
