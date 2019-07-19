package net.minecraft.world.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnchantedGoldenAppleItem extends Item {
    public EnchantedGoldenAppleItem(Item.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
