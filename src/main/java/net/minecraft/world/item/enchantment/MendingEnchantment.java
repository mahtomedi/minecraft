package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
    public MendingEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.BREAKABLE, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return param0 * 25;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
