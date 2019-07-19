package net.minecraft.world.item.enchantment;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class ProtectionEnchantment extends Enchantment {
    public final ProtectionEnchantment.Type type;

    public ProtectionEnchantment(Enchantment.Rarity param0, ProtectionEnchantment.Type param1, EquipmentSlot... param2) {
        super(param0, EnchantmentCategory.ARMOR, param2);
        this.type = param1;
        if (param1 == ProtectionEnchantment.Type.FALL) {
            this.category = EnchantmentCategory.ARMOR_FEET;
        }

    }

    @Override
    public int getMinCost(int param0) {
        return this.type.getMinCost() + (param0 - 1) * this.type.getLevelCost();
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + this.type.getLevelCost();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getDamageProtection(int param0, DamageSource param1) {
        if (param1.isBypassInvul()) {
            return 0;
        } else if (this.type == ProtectionEnchantment.Type.ALL) {
            return param0;
        } else if (this.type == ProtectionEnchantment.Type.FIRE && param1.isFire()) {
            return param0 * 2;
        } else if (this.type == ProtectionEnchantment.Type.FALL && param1 == DamageSource.FALL) {
            return param0 * 3;
        } else if (this.type == ProtectionEnchantment.Type.EXPLOSION && param1.isExplosion()) {
            return param0 * 2;
        } else {
            return this.type == ProtectionEnchantment.Type.PROJECTILE && param1.isProjectile() ? param0 * 2 : 0;
        }
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        if (param0 instanceof ProtectionEnchantment) {
            ProtectionEnchantment var0 = (ProtectionEnchantment)param0;
            if (this.type == var0.type) {
                return false;
            } else {
                return this.type == ProtectionEnchantment.Type.FALL || var0.type == ProtectionEnchantment.Type.FALL;
            }
        } else {
            return super.checkCompatibility(param0);
        }
    }

    public static int getFireAfterDampener(LivingEntity param0, int param1) {
        int var0 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, param0);
        if (var0 > 0) {
            param1 -= Mth.floor((float)param1 * (float)var0 * 0.15F);
        }

        return param1;
    }

    public static double getExplosionKnockbackAfterDampener(LivingEntity param0, double param1) {
        int var0 = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, param0);
        if (var0 > 0) {
            param1 -= (double)Mth.floor(param1 * (double)((float)var0 * 0.15F));
        }

        return param1;
    }

    public static enum Type {
        ALL("all", 1, 11),
        FIRE("fire", 10, 8),
        FALL("fall", 5, 6),
        EXPLOSION("explosion", 5, 8),
        PROJECTILE("projectile", 3, 6);

        private final String name;
        private final int minCost;
        private final int levelCost;

        private Type(String param0, int param1, int param2) {
            this.name = param0;
            this.minCost = param1;
            this.levelCost = param2;
        }

        public int getMinCost() {
            return this.minCost;
        }

        public int getLevelCost() {
            return this.levelCost;
        }
    }
}
