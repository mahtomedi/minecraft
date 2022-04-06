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
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Boat.Type type;
    private final boolean hasChest;

    public BoatItem(boolean param0, Boat.Type param1, Item.Properties param2) {
        super(param2);
        this.hasChest = param0;
        this.type = param1;
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
                Vec3 var5 = param1.getEyePosition();

                for(Entity var6 : var4) {
                    AABB var7 = var6.getBoundingBox().inflate((double)var6.getPickRadius());
                    if (var7.contains(var5)) {
                        return InteractionResultHolder.pass(var0);
                    }
                }
            }

            if (var1.getType() == HitResult.Type.BLOCK) {
                Boat var8 = this.getBoat(param0, var1);
                var8.setType(this.type);
                var8.setYRot(param1.getYRot());
                if (!param0.noCollision(var8, var8.getBoundingBox())) {
                    return InteractionResultHolder.fail(var0);
                } else {
                    if (!param0.isClientSide) {
                        param0.addFreshEntity(var8);
                        param0.gameEvent(param1, GameEvent.ENTITY_PLACE, var1.getLocation());
                        if (!param1.getAbilities().instabuild) {
                            var0.shrink(1);
                        }
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(var0);
            }
        }
    }

    private Boat getBoat(Level param0, HitResult param1) {
        return (Boat)(this.hasChest
            ? new ChestBoat(param0, param1.getLocation().x, param1.getLocation().y, param1.getLocation().z)
            : new Boat(param0, param1.getLocation().x, param1.getLocation().y, param1.getLocation().z));
    }
}
