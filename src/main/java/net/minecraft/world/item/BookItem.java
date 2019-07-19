package net.minecraft.world.item;

public class BookItem extends Item {
    public BookItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isEnchantable(ItemStack param0) {
        return param0.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
