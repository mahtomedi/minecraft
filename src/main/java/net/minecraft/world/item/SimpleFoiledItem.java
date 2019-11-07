package net.minecraft.world.item;

public class SimpleFoiledItem extends Item {
    public SimpleFoiledItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
