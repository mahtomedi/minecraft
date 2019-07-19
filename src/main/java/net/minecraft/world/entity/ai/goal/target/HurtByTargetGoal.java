package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
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
            for(Class<?> var2 : this.toIgnoreDamage) {
                if (var2.isAssignableFrom(var1.getClass())) {
                    return false;
                }
            }

            return this.canAttack(var1, HURT_BY_TARGETING);
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
        List<Mob> var1 = this.mob
            .level
            .getLoadedEntitiesOfClass(
                this.mob.getClass(),
                new AABB(this.mob.x, this.mob.y, this.mob.z, this.mob.x + 1.0, this.mob.y + 1.0, this.mob.z + 1.0).inflate(var0, 10.0, var0)
            );
        Iterator var4 = var1.iterator();

        while(true) {
            Mob var2;
            while(true) {
                if (!var4.hasNext()) {
                    return;
                }

                var2 = (Mob)var4.next();
                if (this.mob != var2
                    && var2.getTarget() == null
                    && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)var2).getOwner())
                    && !var2.isAlliedTo(this.mob.getLastHurtByMob())) {
                    if (this.toIgnoreAlert == null) {
                        break;
                    }

                    boolean var3 = false;

                    for(Class<?> var4 : this.toIgnoreAlert) {
                        if (var2.getClass() == var4) {
                            var3 = true;
                            break;
                        }
                    }

                    if (!var3) {
                        break;
                    }
                }
            }

            this.alertOther(var2, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob param0, LivingEntity param1) {
        param0.setTarget(param1);
    }
}
