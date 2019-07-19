package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowPiercingEnchantment extends Enchantment {
    public ArrowPiercingEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.CROSSBOW, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 1 + (param0 - 1) * 10;
    }

    @Override
    public int getMaxCost(int param0) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.MULTISHOT;
    }
}
