package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class FireAspectEnchantment extends Enchantment {
    protected FireAspectEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.WEAPON, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 10 + 20 * (param0 - 1);
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
