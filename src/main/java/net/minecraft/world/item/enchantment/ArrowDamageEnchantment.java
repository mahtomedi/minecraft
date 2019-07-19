package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowDamageEnchantment extends Enchantment {
    public ArrowDamageEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.BOW, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 1 + (param0 - 1) * 10;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}
