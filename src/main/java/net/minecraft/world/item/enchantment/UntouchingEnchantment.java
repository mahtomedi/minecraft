package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class UntouchingEnchantment extends Enchantment {
    protected UntouchingEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.DIGGER, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 15;
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.BLOCK_FORTUNE;
    }
}
