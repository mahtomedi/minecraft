package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
            CrossbowItem.performShooting(param0.level(), param0, var0, var1, param1, (float)(14 - param0.level().getDifficulty().getId() * 4));
        }

        this.onCrossbowAttackPerformed();
    }

    default void shootCrossbowProjectile(LivingEntity param0, LivingEntity param1, Projectile param2, float param3, float param4) {
        double var0 = param1.getX() - param0.getX();
        double var1 = param1.getZ() - param0.getZ();
        double var2 = Math.sqrt(var0 * var0 + var1 * var1);
        double var3 = param1.getY(0.3333333333333333) - param2.getY() + var2 * 0.2F;
        Vector3f var4 = this.getProjectileShotVector(param0, new Vec3(var0, var3, var1), param3);
        param2.shoot((double)var4.x(), (double)var4.y(), (double)var4.z(), param4, (float)(14 - param0.level().getDifficulty().getId() * 4));
        param0.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (param0.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    default Vector3f getProjectileShotVector(LivingEntity param0, Vec3 param1, float param2) {
        Vector3f var0 = param1.toVector3f().normalize();
        Vector3f var1 = new Vector3f((Vector3fc)var0).cross(new Vector3f(0.0F, 1.0F, 0.0F));
        if ((double)var1.lengthSquared() <= 1.0E-7) {
            Vec3 var2 = param0.getUpVector(1.0F);
            var1 = new Vector3f((Vector3fc)var0).cross(var2.toVector3f());
        }

        Vector3f var3 = new Vector3f((Vector3fc)var0).rotateAxis((float) (Math.PI / 2), var1.x, var1.y, var1.z);
        return new Vector3f((Vector3fc)var0).rotateAxis(param2 * (float) (Math.PI / 180.0), var3.x, var3.y, var3.z);
    }
}
