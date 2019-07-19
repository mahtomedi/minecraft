package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ItemPropertyFunction {
    @OnlyIn(Dist.CLIENT)
    float call(ItemStack var1, @Nullable Level var2, @Nullable LivingEntity var3);
}
