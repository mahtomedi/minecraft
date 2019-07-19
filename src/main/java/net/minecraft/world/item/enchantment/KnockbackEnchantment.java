package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class KnockbackEnchantment extends Enchantment {
    protected KnockbackEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.WEAPON, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 5 + 20 * (param0 - 1);
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
