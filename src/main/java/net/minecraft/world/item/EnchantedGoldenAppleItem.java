package net.minecraft.world.item;

public class EnchantedGoldenAppleItem extends Item {
    public EnchantedGoldenAppleItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
