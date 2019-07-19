package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class LootBonusEnchantment extends Enchantment {
    protected LootBonusEnchantment(Enchantment.Rarity param0, EnchantmentCategory param1, EquipmentSlot... param2) {
        super(param0, param1, param2);
    }

    @Override
    public int getMinCost(int param0) {
        return 15 + (param0 - 1) * 9;
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.SILK_TOUCH;
    }
}
