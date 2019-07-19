package net.minecraft.world.item;

public class TieredItem extends Item {
    private final Tier tier;

    public TieredItem(Tier param0, Item.Properties param1) {
        super(param1.defaultDurability(param0.getUses()));
        this.tier = param0;
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack param0, ItemStack param1) {
        return this.tier.getRepairIngredient().test(param1) || super.isValidRepairItem(param0, param1);
    }
}
