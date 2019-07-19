package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentRiptideEnchantment extends Enchantment {
    public TridentRiptideEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.TRIDENT, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 10 + param0 * 7;
    }

    @Override
    public int getMaxCost(int param0) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.LOYALTY && param0 != Enchantments.CHANNELING;
    }
}
