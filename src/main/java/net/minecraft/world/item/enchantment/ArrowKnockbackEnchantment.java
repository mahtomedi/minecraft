package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowKnockbackEnchantment extends Enchantment {
    public ArrowKnockbackEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.BOW, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 12 + (param0 - 1) * 20;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
