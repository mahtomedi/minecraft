package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BindingCurseEnchantment extends Enchantment {
    public BindingCurseEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.WEARABLE, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 25;
    }

    @Override
    public int getMaxCost(int param0) {
        return 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean canEnchant(ItemStack param0) {
        return !param0.is(Items.SHIELD) && super.canEnchant(param0);
    }
}
