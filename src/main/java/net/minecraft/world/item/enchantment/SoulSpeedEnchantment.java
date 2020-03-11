package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SoulSpeedEnchantment extends Enchantment {
    public SoulSpeedEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.ARMOR_FEET, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return param0 * 10;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 15;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
