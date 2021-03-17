package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentChannelingEnchantment extends Enchantment {
    public TridentChannelingEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.TRIDENT, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 25;
    }

    @Override
    public int getMaxCost(int param0) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
