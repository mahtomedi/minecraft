package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class QuickChargeEnchantment extends Enchantment {
    public QuickChargeEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.CROSSBOW, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 12 + (param0 - 1) * 20;
    }

    @Override
    public int getMaxCost(int param0) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
