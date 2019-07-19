package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class BindingCurseEnchantment extends Enchantment {
    public BindingCurseEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.WEARABLE, param1);
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

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }
}
