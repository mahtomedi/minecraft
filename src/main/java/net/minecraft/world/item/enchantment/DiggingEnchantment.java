package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiggingEnchantment extends Enchantment {
    protected DiggingEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.DIGGER, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 1 + 10 * (param0 - 1);
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canEnchant(ItemStack param0) {
        return param0.is(Items.SHEARS) ? true : super.canEnchant(param0);
    }
}
