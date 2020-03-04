package net.minecraft.world.entity.monster;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public interface CrossbowAttackMob extends RangedAttackMob {
    void setChargingCrossbow(boolean var1);

    void shootCrossbowProjectile(LivingEntity var1, ItemStack var2, Projectile var3, float var4);

    @Nullable
    LivingEntity getTarget();

    void onCrossbowAttackPerformed();

    default void performCrossbowAttack(LivingEntity param0, float param1) {
        InteractionHand var0 = ProjectileUtil.getWeaponHoldingHand(param0, Items.CROSSBOW);
        ItemStack var1 = param0.getItemInHand(var0);
        if (param0.isHolding(Items.CROSSBOW)) {
            CrossbowItem.performShooting(param0.level, param0, var0, var1, param1, (float)(14 - param0.level.getDifficulty().getId() * 4));
        }

        this.onCrossbowAttackPerformed();
    }

    default void shootCrossbowProjectile(LivingEntity param0, LivingEntity param1, Projectile param2, float param3, float param4) {
        double var1 = param1.getX() - param0.getX();
        double var2 = param1.getZ() - param0.getZ();
        double var3 = (double)Mth.sqrt(var1 * var1 + var2 * var2);
        double var4 = param1.getY(0.3333333333333333) - param2.getY() + var3 * 0.2F;
        Vector3f var5 = this.getProjectileShotVector(param0, new Vec3(var1, var4, var2), param3);
        param2.shoot((double)var5.x(), (double)var5.y(), (double)var5.z(), param4, (float)(14 - param0.level.getDifficulty().getId() * 4));
        param0.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (param0.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    default Vector3f getProjectileShotVector(LivingEntity param0, Vec3 param1, float param2) {
        Vec3 var0 = param1.normalize();
        Vec3 var1 = var0.cross(new Vec3(0.0, 1.0, 0.0));
        if (var1.lengthSqr() <= 1.0E-7) {
            var1 = var0.cross(param0.getUpVector(1.0F));
        }

        Quaternion var2 = new Quaternion(new Vector3f(var1), 90.0F, true);
        Vector3f var3 = new Vector3f(var0);
        var3.transform(var2);
        Quaternion var4 = new Quaternion(var3, param2, true);
        Vector3f var5 = new Vector3f(var0);
        var5.transform(var4);
        return var5;
    }
}
