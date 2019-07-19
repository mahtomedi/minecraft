package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWalkerEnchantment extends Enchantment {
    public WaterWalkerEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
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
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.FROST_WALKER;
    }
}
