package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowInfiniteEnchantment extends Enchantment {
    public ArrowInfiniteEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.BOW, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 20;
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
    public boolean checkCompatibility(Enchantment param0) {
        return param0 instanceof MendingEnchantment ? false : super.checkCompatibility(param0);
    }
}
