package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class LookAtPlayerGoal extends Goal {
    protected final Mob mob;
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    private final float probability;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2) {
        this(param0, param1, param2, 0.02F);
    }

    public LookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2, float param3) {
        this.mob = param0;
        this.lookAtType = param1;
        this.lookDistance = param2;
        this.probability = param3;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        if (param1 == Player.class) {
            this.lookAtContext = new TargetingConditions()
                .range((double)param2)
                .allowSameTeam()
                .allowInvulnerable()
                .allowNonAttackable()
                .selector(param1x -> EntitySelector.notRiding(param0).test(param1x));
        } else {
            this.lookAtContext = new TargetingConditions().range((double)param2).allowSameTeam().allowInvulnerable().allowNonAttackable();
        }

    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.lookAt = this.mob.getTarget();
            }

            if (this.lookAtType == Player.class) {
                this.lookAt = this.mob
                    .level
                    .getNearestPlayer(this.lookAtContext, this.mob, this.mob.x, this.mob.y + (double)this.mob.getEyeHeight(), this.mob.z);
            } else {
                this.lookAt = this.mob
                    .level
                    .getNearestLoadedEntity(
                        this.lookAtType,
                        this.lookAtContext,
                        this.mob,
                        this.mob.x,
                        this.mob.y + (double)this.mob.getEyeHeight(),
                        this.mob.z,
                        this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance)
                    );
            }

            return this.lookAt != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        } else if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    @Override
    public void start() {
        this.lookTime = 40 + this.mob.getRandom().nextInt(40);
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.lookAt.x, this.lookAt.y + (double)this.lookAt.getEyeHeight(), this.lookAt.z);
        --this.lookTime;
    }
}
