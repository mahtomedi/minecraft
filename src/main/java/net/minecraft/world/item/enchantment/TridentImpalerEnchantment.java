package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;

public class TridentImpalerEnchantment extends Enchantment {
    public TridentImpalerEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.TRIDENT, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 1 + (param0 - 1) * 8;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public float getDamageBonus(int param0, MobType param1) {
        return param1 == MobType.WATER ? (float)param0 * 2.5F : 0.0F;
    }
}
