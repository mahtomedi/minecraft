package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Boat.Type type;

    public BoatItem(Boat.Type param0, Item.Properties param1) {
        super(param1);
        this.type = param0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        HitResult var1 = getPlayerPOVHitResult(param0, param1, ClipContext.Fluid.ANY);
        if (var1.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(var0);
        } else {
            Vec3 var2 = param1.getViewVector(1.0F);
            double var3 = 5.0;
            List<Entity> var4 = param0.getEntities(param1, param1.getBoundingBox().expandTowards(var2.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
            if (!var4.isEmpty()) {
                Vec3 var5 = param1.getEyePosition(1.0F);

                for(Entity var6 : var4) {
                    AABB var7 = var6.getBoundingBox().inflate((double)var6.getPickRadius());
                    if (var7.contains(var5)) {
                        return InteractionResultHolder.pass(var0);
                    }
                }
            }

            if (var1.getType() == HitResult.Type.BLOCK) {
                Boat var8 = new Boat(param0, var1.getLocation().x, var1.getLocation().y, var1.getLocation().z);
                var8.setType(this.type);
                var8.yRot = param1.yRot;
                if (!param0.noCollision(var8, var8.getBoundingBox().inflate(-0.1))) {
                    return InteractionResultHolder.fail(var0);
                } else {
                    if (!param0.isClientSide) {
                        param0.addFreshEntity(var8);
                    }

                    if (!param1.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.success(var0);
                }
            } else {
                return InteractionResultHolder.pass(var0);
            }
        }
    }
}
