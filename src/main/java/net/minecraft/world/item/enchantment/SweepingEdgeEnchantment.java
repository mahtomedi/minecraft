package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SweepingEdgeEnchantment extends Enchantment {
    public SweepingEdgeEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.WEAPON, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 5 + (param0 - 1) * 9;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    public static float getSweepingDamageRatio(int param0) {
        return 1.0F - 1.0F / (float)(param0 + 1);
    }
}
