package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

public abstract class TargetGoal extends Goal {
    protected final Mob mob;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    protected LivingEntity targetMob;
    protected int unseenMemoryTicks = 60;

    public TargetGoal(Mob param0, boolean param1) {
        this(param0, param1, false);
    }

    public TargetGoal(Mob param0, boolean param1, boolean param2) {
        this.mob = param0;
        this.mustSee = param1;
        this.mustReach = param2;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 == null) {
            var0 = this.targetMob;
        }

        if (var0 == null) {
            return false;
        } else if (!var0.isAlive()) {
            return false;
        } else {
            Team var1 = this.mob.getTeam();
            Team var2 = var0.getTeam();
            if (var1 != null && var2 == var1) {
                return false;
            } else {
                double var3 = this.getFollowDistance();
                if (this.mob.distanceToSqr(var0) > var3 * var3) {
                    return false;
                } else {
                    if (this.mustSee) {
                        if (this.mob.getSensing().canSee(var0)) {
                            this.unseenTicks = 0;
                        } else if (++this.unseenTicks > this.unseenMemoryTicks) {
                            return false;
                        }
                    }

                    if (var0 instanceof Player && ((Player)var0).abilities.invulnerable) {
                        return false;
                    } else {
                        this.mob.setTarget(var0);
                        return true;
                    }
                }
            }
        }
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        this.reachCache = 0;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
        this.targetMob = null;
    }

    protected boolean canAttack(@Nullable LivingEntity param0, TargetingConditions param1) {
        if (param0 == null) {
            return false;
        } else if (!param1.test(this.mob, param0)) {
            return false;
        } else if (!this.mob.isWithinRestriction(param0.blockPosition())) {
            return false;
        } else {
            if (this.mustReach) {
                if (--this.reachCacheTime <= 0) {
                    this.reachCache = 0;
                }

                if (this.reachCache == 0) {
                    this.reachCache = this.canReach(param0) ? 1 : 2;
                }

                if (this.reachCache == 2) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean canReach(LivingEntity param0) {
        this.reachCacheTime = 10 + this.mob.getRandom().nextInt(5);
        Path var0 = this.mob.getNavigation().createPath(param0, 0);
        if (var0 == null) {
            return false;
        } else {
            Node var1 = var0.getEndNode();
            if (var1 == null) {
                return false;
            } else {
                int var2 = var1.x - Mth.floor(param0.getX());
                int var3 = var1.z - Mth.floor(param0.getZ());
                return (double)(var2 * var2 + var3 * var3) <= 2.25;
            }
        }
    }

    public TargetGoal setUnseenMemoryTicks(int param0) {
        this.unseenMemoryTicks = param0;
        return this;
    }
}
