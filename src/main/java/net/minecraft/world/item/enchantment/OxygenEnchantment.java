package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class OxygenEnchantment extends Enchantment {
    public OxygenEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.ARMOR_HEAD, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 10 * param0;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
