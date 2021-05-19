package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClampedItemPropertyFunction extends ItemPropertyFunction {
    @Deprecated
    @Override
    default float call(ItemStack param0, @Nullable ClientLevel param1, @Nullable LivingEntity param2, int param3) {
        return Mth.clamp(this.unclampedCall(param0, param1, param2, param3), 0.0F, 1.0F);
    }

    float unclampedCall(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4);
}
