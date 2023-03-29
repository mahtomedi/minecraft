package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ProjectileUtil {
    public static HitResult getHitResultOnMoveVector(Entity param0, Predicate<Entity> param1) {
        Vec3 var0 = param0.getDeltaMovement();
        Level var1 = param0.level;
        Vec3 var2 = param0.position();
        return getHitResult(var2, param0, param1, var0, var1);
    }

    public static HitResult getHitResultOnViewVector(Entity param0, Predicate<Entity> param1, double param2) {
        Vec3 var0 = param0.getViewVector(0.0F).scale(param2);
        Level var1 = param0.level;
        Vec3 var2 = param0.getEyePosition();
        return getHitResult(var2, param0, param1, var0, var1);
    }

    private static HitResult getHitResult(Vec3 param0, Entity param1, Predicate<Entity> param2, Vec3 param3, Level param4) {
        Vec3 var0 = param0.add(param3);
        HitResult var1 = param4.clip(new ClipContext(param0, var0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param1));
        if (var1.getType() != HitResult.Type.MISS) {
            var0 = var1.getLocation();
        }

        HitResult var2 = getEntityHitResult(param4, param1, param0, var0, param1.getBoundingBox().expandTowards(param1.getDeltaMovement()).inflate(1.0), param2);
        if (var2 != null) {
            var1 = var2;
        }

        return var1;
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity param0, Vec3 param1, Vec3 param2, AABB param3, Predicate<Entity> param4, double param5) {
        Level var0 = param0.level;
        double var1 = param5;
        Entity var2 = null;
        Vec3 var3 = null;

        for(Entity var4 : var0.getEntities(param0, param3, param4)) {
            AABB var5 = var4.getBoundingBox().inflate((double)var4.getPickRadius());
            Optional<Vec3> var6 = var5.clip(param1, param2);
            if (var5.contains(param1)) {
                if (var1 >= 0.0) {
                    var2 = var4;
                    var3 = var6.orElse(param1);
                    var1 = 0.0;
                }
            } else if (var6.isPresent()) {
                Vec3 var7 = var6.get();
                double var8 = param1.distanceToSqr(var7);
                if (var8 < var1 || var1 == 0.0) {
                    if (var4.getRootVehicle() == param0.getRootVehicle()) {
                        if (var1 == 0.0) {
                            var2 = var4;
                            var3 = var7;
                        }
                    } else {
                        var2 = var4;
                        var3 = var7;
                        var1 = var8;
                    }
                }
            }
        }

        return var2 == null ? null : new EntityHitResult(var2, var3);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level param0, Entity param1, Vec3 param2, Vec3 param3, AABB param4, Predicate<Entity> param5) {
        return getEntityHitResult(param0, param1, param2, param3, param4, param5, 0.3F);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level param0, Entity param1, Vec3 param2, Vec3 param3, AABB param4, Predicate<Entity> param5, float param6) {
        double var0 = Double.MAX_VALUE;
        Entity var1 = null;

        for(Entity var2 : param0.getEntities(param1, param4, param5)) {
            AABB var3 = var2.getBoundingBox().inflate((double)param6);
            Optional<Vec3> var4 = var3.clip(param2, param3);
            if (var4.isPresent()) {
                double var5 = param2.distanceToSqr(var4.get());
                if (var5 < var0) {
                    var1 = var2;
                    var0 = var5;
                }
            }
        }

        return var1 == null ? null : new EntityHitResult(var1);
    }

    public static void rotateTowardsMovement(Entity param0, float param1) {
        Vec3 var0 = param0.getDeltaMovement();
        if (var0.lengthSqr() != 0.0) {
            double var1 = var0.horizontalDistance();
            param0.setYRot((float)(Mth.atan2(var0.z, var0.x) * 180.0F / (float)Math.PI) + 90.0F);
            param0.setXRot((float)(Mth.atan2(var1, var0.y) * 180.0F / (float)Math.PI) - 90.0F);

            while(param0.getXRot() - param0.xRotO < -180.0F) {
                param0.xRotO -= 360.0F;
            }

            while(param0.getXRot() - param0.xRotO >= 180.0F) {
                param0.xRotO += 360.0F;
            }

            while(param0.getYRot() - param0.yRotO < -180.0F) {
                param0.yRotO -= 360.0F;
            }

            while(param0.getYRot() - param0.yRotO >= 180.0F) {
                param0.yRotO += 360.0F;
            }

            param0.setXRot(Mth.lerp(param1, param0.xRotO, param0.getXRot()));
            param0.setYRot(Mth.lerp(param1, param0.yRotO, param0.getYRot()));
        }
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity param0, Item param1) {
        return param0.getMainHandItem().is(param1) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity param0, ItemStack param1, float param2) {
        ArrowItem var0 = (ArrowItem)(param1.getItem() instanceof ArrowItem ? param1.getItem() : Items.ARROW);
        AbstractArrow var1 = var0.createArrow(param0.level, param1, param0);
        var1.setEnchantmentEffectsFromEntity(param0, param2);
        if (param1.is(Items.TIPPED_ARROW) && var1 instanceof Arrow) {
            ((Arrow)var1).setEffectsFromItem(param1);
        }

        return var1;
    }
}
