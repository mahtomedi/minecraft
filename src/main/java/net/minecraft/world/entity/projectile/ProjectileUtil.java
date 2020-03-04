package net.minecraft.world.entity.projectile;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class ProjectileUtil {
    public static HitResult forwardsRaycast(Entity param0, boolean param1, boolean param2, @Nullable Entity param3, ClipContext.Block param4) {
        return forwardsRaycast(
            param0,
            param1,
            param2,
            param3,
            param4,
            true,
            param2x -> !param2x.isSpectator() && param2x.isPickable() && (param2 || !param2x.is(param3)) && !param2x.noPhysics,
            param0.getBoundingBox().expandTowards(param0.getDeltaMovement()).inflate(1.0)
        );
    }

    public static HitResult getHitResult(Entity param0, AABB param1, Predicate<Entity> param2, ClipContext.Block param3, boolean param4) {
        return forwardsRaycast(param0, param4, false, null, param3, false, param2, param1);
    }

    @Nullable
    public static EntityHitResult getHitResult(Level param0, Entity param1, Vec3 param2, Vec3 param3, AABB param4, Predicate<Entity> param5) {
        return getHitResult(param0, param1, param2, param3, param4, param5, Double.MAX_VALUE);
    }

    private static HitResult forwardsRaycast(
        Entity param0, boolean param1, boolean param2, @Nullable Entity param3, ClipContext.Block param4, boolean param5, Predicate<Entity> param6, AABB param7
    ) {
        Vec3 var0 = param0.getDeltaMovement();
        Level var1 = param0.level;
        Vec3 var2 = param0.position();
        if (param5
            && !var1.noCollision(param0, param0.getBoundingBox(), (Set<Entity>)(!param2 && param3 != null ? getIgnoredEntities(param3) : ImmutableSet.of()))) {
            return new BlockHitResult(var2, Direction.getNearest(var0.x, var0.y, var0.z), param0.blockPosition(), false);
        } else {
            Vec3 var3 = var2.add(var0);
            HitResult var4 = var1.clip(new ClipContext(var2, var3, param4, ClipContext.Fluid.NONE, param0));
            if (param1) {
                if (var4.getType() != HitResult.Type.MISS) {
                    var3 = var4.getLocation();
                }

                HitResult var5 = getHitResult(var1, param0, var2, var3, param7, param6);
                if (var5 != null) {
                    var4 = var5;
                }
            }

            return var4;
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
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
    public static EntityHitResult getHitResult(Level param0, Entity param1, Vec3 param2, Vec3 param3, AABB param4, Predicate<Entity> param5, double param6) {
        double var0 = param6;
        Entity var1 = null;

        for(Entity var2 : param0.getEntities(param1, param4, param5)) {
            AABB var3 = var2.getBoundingBox().inflate(0.3F);
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

    private static Set<Entity> getIgnoredEntities(Entity param0) {
        Entity var0 = param0.getVehicle();
        return var0 != null ? ImmutableSet.of(param0, var0) : ImmutableSet.of(param0);
    }

    public static final void rotateTowardsMovement(Entity param0, float param1) {
        Vec3 var0 = param0.getDeltaMovement();
        float var1 = Mth.sqrt(Entity.getHorizontalDistanceSqr(var0));
        param0.yRot = (float)(Mth.atan2(var0.z, var0.x) * 180.0F / (float)Math.PI) + 90.0F;
        param0.xRot = (float)(Mth.atan2((double)var1, var0.y) * 180.0F / (float)Math.PI) - 90.0F;

        while(param0.xRot - param0.xRotO < -180.0F) {
            param0.xRotO -= 360.0F;
        }

        while(param0.xRot - param0.xRotO >= 180.0F) {
            param0.xRotO += 360.0F;
        }

        while(param0.yRot - param0.yRotO < -180.0F) {
            param0.yRotO -= 360.0F;
        }

        while(param0.yRot - param0.yRotO >= 180.0F) {
            param0.yRotO += 360.0F;
        }

        param0.xRot = Mth.lerp(param1, param0.xRotO, param0.xRot);
        param0.yRot = Mth.lerp(param1, param0.yRotO, param0.yRot);
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity param0, Item param1) {
        return param0.getMainHandItem().getItem() == param1 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity param0, ItemStack param1, float param2) {
        ArrowItem var0 = (ArrowItem)(param1.getItem() instanceof ArrowItem ? param1.getItem() : Items.ARROW);
        AbstractArrow var1 = var0.createArrow(param0.level, param1, param0);
        var1.setEnchantmentEffectsFromEntity(param0, param2);
        if (param1.getItem() == Items.TIPPED_ARROW && var1 instanceof Arrow) {
            ((Arrow)var1).setEffectsFromItem(param1);
        }

        return var1;
    }
}
