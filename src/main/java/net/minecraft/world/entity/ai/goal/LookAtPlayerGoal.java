package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class LookAtPlayerGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final Mob mob;
    @Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2) {
        this(param0, param1, param2, 0.02F);
    }

    public LookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2, float param3) {
        this(param0, param1, param2, param3, false);
    }

    public LookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2, float param3, boolean param4) {
        this.mob = param0;
        this.lookAtType = param1;
        this.lookDistance = param2;
        this.probability = param3;
        this.onlyHorizontal = param4;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        if (param1 == Player.class) {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double)param2).selector(param1x -> EntitySelector.notRiding(param0).test(param1x));
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double)param2);
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
                this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.lookAt = this.mob
                    .level
                    .getNearestEntity(
                        this.mob
                            .level
                            .getEntitiesOfClass(
                                this.lookAtType, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance), param0 -> true
                            ),
                        this.lookAtContext,
                        this.mob,
                        this.mob.getX(),
                        this.mob.getEyeY(),
                        this.mob.getZ()
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
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.lookAt.isAlive()) {
            double var0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), var0, this.lookAt.getZ());
            --this.lookTime;
        }
    }
}
