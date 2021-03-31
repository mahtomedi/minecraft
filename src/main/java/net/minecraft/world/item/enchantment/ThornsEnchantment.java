package net.minecraft.world.item.enchantment;

import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ThornsEnchantment extends Enchantment {
    private static final float CHANCE_PER_LEVEL = 0.15F;

    public ThornsEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.ARMOR_CHEST, param1);
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
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack param0) {
        return param0.getItem() instanceof ArmorItem ? true : super.canEnchant(param0);
    }

    @Override
    public void doPostHurt(LivingEntity param0, Entity param1, int param2) {
        Random var0 = param0.getRandom();
        Entry<EquipmentSlot, ItemStack> var1 = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, param0);
        if (shouldHit(param2, var0)) {
            if (param1 != null) {
                param1.hurt(DamageSource.thorns(param0), (float)getDamage(param2, var0));
            }

            if (var1 != null) {
                var1.getValue().hurtAndBreak(2, param0, param1x -> param1x.broadcastBreakEvent(var1.getKey()));
            }
        }

    }

    public static boolean shouldHit(int param0, Random param1) {
        if (param0 <= 0) {
            return false;
        } else {
            return param1.nextFloat() < 0.15F * (float)param0;
        }
    }

    public static int getDamage(int param0, Random param1) {
        return param0 > 10 ? param0 - 10 : 1 + param1.nextInt(4);
    }
}
