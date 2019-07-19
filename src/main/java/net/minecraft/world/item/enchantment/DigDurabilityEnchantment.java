package net.minecraft.world.item.enchantment;

import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
    protected DigDurabilityEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.BREAKABLE, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return 5 + (param0 - 1) * 8;
    }

    @Override
    public int getMaxCost(int param0) {
        return super.getMinCost(param0) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack param0) {
        return param0.isDamageableItem() ? true : super.canEnchant(param0);
    }

    public static boolean shouldIgnoreDurabilityDrop(ItemStack param0, int param1, Random param2) {
        if (param0.getItem() instanceof ArmorItem && param2.nextFloat() < 0.6F) {
            return false;
        } else {
            return param2.nextInt(param1 + 1) > 0;
        }
    }
}
