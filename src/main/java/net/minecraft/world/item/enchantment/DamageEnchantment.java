package net.minecraft.world.item.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class DamageEnchantment extends Enchantment {
    private static final String[] NAMES = new String[]{"all", "undead", "arthropods"};
    private static final int[] MIN_COST = new int[]{1, 5, 5};
    private static final int[] LEVEL_COST = new int[]{11, 8, 8};
    private static final int[] LEVEL_COST_SPAN = new int[]{20, 20, 20};
    public final int type;

    public DamageEnchantment(Enchantment.Rarity param0, int param1, EquipmentSlot... param2) {
        super(param0, EnchantmentCategory.WEAPON, param2);
        this.type = param1;
    }

    @Override
    public int getMinCost(int param0) {
        return MIN_COST[this.type] + (param0 - 1) * LEVEL_COST[this.type];
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + LEVEL_COST_SPAN[this.type];
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public float getDamageBonus(int param0, MobType param1) {
        if (this.type == 0) {
            return 1.0F + (float)Math.max(0, param0 - 1) * 0.5F;
        } else if (this.type == 1 && param1 == MobType.UNDEAD) {
            return (float)param0 * 2.5F;
        } else {
            return this.type == 2 && param1 == MobType.ARTHROPOD ? (float)param0 * 2.5F : 0.0F;
        }
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return !(param0 instanceof DamageEnchantment);
    }

    @Override
    public boolean canEnchant(ItemStack param0) {
        return param0.getItem() instanceof AxeItem ? true : super.canEnchant(param0);
    }

    @Override
    public void doPostAttack(LivingEntity param0, Entity param1, int param2) {
        if (param1 instanceof LivingEntity) {
            LivingEntity var0 = (LivingEntity)param1;
            if (this.type == 2 && var0.getMobType() == MobType.ARTHROPOD) {
                int var1 = 20 + param0.getRandom().nextInt(10 * param2);
                var0.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, var1, 3));
            }
        }

    }
}
