package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public HurtByTargetGoal(PathfinderMob param0, Class<?>... param1) {
        super(param0, true);
        this.toIgnoreDamage = param1;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int var0 = this.mob.getLastHurtByMobTimestamp();
        LivingEntity var1 = this.mob.getLastHurtByMob();
        if (var0 != this.timestamp && var1 != null) {
            if (var1.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                for(Class<?> var2 : this.toIgnoreDamage) {
                    if (var2.isAssignableFrom(var1.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(var1, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public HurtByTargetGoal setAlertOthers(Class<?>... param0) {
        this.alertSameType = true;
        this.toIgnoreAlert = param0;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double var0 = this.getFollowDistance();
        AABB var1 = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(var0, 10.0, var0);
        List<? extends Mob> var2 = this.mob.level.getEntitiesOfClass(this.mob.getClass(), var1, EntitySelector.NO_SPECTATORS);
        Iterator var5 = var2.iterator();

        while(true) {
            Mob var3;
            while(true) {
                if (!var5.hasNext()) {
                    return;
                }

                var3 = (Mob)var5.next();
                if (this.mob != var3
                    && var3.getTarget() == null
                    && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)var3).getOwner())
                    && !var3.isAlliedTo(this.mob.getLastHurtByMob())) {
                    if (this.toIgnoreAlert == null) {
                        break;
                    }

                    boolean var4 = false;

                    for(Class<?> var5 : this.toIgnoreAlert) {
                        if (var3.getClass() == var5) {
                            var4 = true;
                            break;
                        }
                    }

                    if (!var4) {
                        break;
                    }
                }
            }

            this.alertOther(var3, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob param0, LivingEntity param1) {
        param0.setTarget(param1);
    }
}
