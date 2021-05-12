package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

public final class EntitySelector {
    public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
    public static final Predicate<Entity> LIVING_ENTITY_STILL_ALIVE = param0 -> param0.isAlive() && param0 instanceof LivingEntity;
    public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = param0 -> param0.isAlive() && !param0.isVehicle() && !param0.isPassenger();
    public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = param0 -> param0 instanceof Container && param0.isAlive();
    public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = param0 -> !(param0 instanceof Player)
            || !param0.isSpectator() && !((Player)param0).isCreative();
    public static final Predicate<Entity> ATTACK_ALLOWED = param0 -> (
                !(param0 instanceof Player) || !param0.isSpectator() && !((Player)param0).isCreative() && param0.level.getDifficulty() != Difficulty.PEACEFUL
            )
            && (!(param0 instanceof Axolotl) || !((Axolotl)param0).isPlayingDead());
    public static final Predicate<Entity> NO_SPECTATORS = param0 -> !param0.isSpectator();

    private EntitySelector() {
    }

    public static Predicate<Entity> withinDistance(double param0, double param1, double param2, double param3) {
        double var0 = param3 * param3;
        return param4 -> param4 != null && param4.distanceToSqr(param0, param1, param2) <= var0;
    }

    public static Predicate<Entity> pushableBy(Entity param0) {
        Team var0 = param0.getTeam();
        Team.CollisionRule var1 = var0 == null ? Team.CollisionRule.ALWAYS : var0.getCollisionRule();
        return (Predicate<Entity>)(var1 == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : NO_SPECTATORS.and(param3 -> {
            if (!param3.isPushable()) {
                return false;
            } else if (!param0.level.isClientSide || param3 instanceof Player && ((Player)param3).isLocalPlayer()) {
                Team var0x = param3.getTeam();
                Team.CollisionRule var1x = var0x == null ? Team.CollisionRule.ALWAYS : var0x.getCollisionRule();
                if (var1x == Team.CollisionRule.NEVER) {
                    return false;
                } else {
                    boolean var2x = var0 != null && var0.isAlliedTo(var0x);
                    if ((var1 == Team.CollisionRule.PUSH_OWN_TEAM || var1x == Team.CollisionRule.PUSH_OWN_TEAM) && var2x) {
                        return false;
                    } else {
                        return var1 != Team.CollisionRule.PUSH_OTHER_TEAMS && var1x != Team.CollisionRule.PUSH_OTHER_TEAMS || var2x;
                    }
                }
            } else {
                return false;
            }
        }));
    }

    public static Predicate<Entity> notRiding(Entity param0) {
        return param1 -> {
            while(param1.isPassenger()) {
                param1 = param1.getVehicle();
                if (param1 == param0) {
                    return false;
                }
            }

            return true;
        };
    }

    public static class MobCanWearArmorEntitySelector implements Predicate<Entity> {
        private final ItemStack itemStack;

        public MobCanWearArmorEntitySelector(ItemStack param0) {
            this.itemStack = param0;
        }

        public boolean test(@Nullable Entity param0) {
            if (!param0.isAlive()) {
                return false;
            } else if (!(param0 instanceof LivingEntity)) {
                return false;
            } else {
                LivingEntity var0 = (LivingEntity)param0;
                return var0.canTakeItem(this.itemStack);
            }
        }
    }
}
