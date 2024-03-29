package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LeapAtTargetGoal extends Goal {
    private final Mob mob;
    private LivingEntity target;
    private final float yd;

    public LeapAtTargetGoal(Mob param0, float param1) {
        this.mob = param0;
        this.yd = param1;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.hasControllingPassenger()) {
            return false;
        } else {
            this.target = this.mob.getTarget();
            if (this.target == null) {
                return false;
            } else {
                double var0 = this.mob.distanceToSqr(this.target);
                if (var0 < 4.0 || var0 > 16.0) {
                    return false;
                } else if (!this.mob.onGround()) {
                    return false;
                } else {
                    return this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.onGround();
    }

    @Override
    public void start() {
        Vec3 var0 = this.mob.getDeltaMovement();
        Vec3 var1 = new Vec3(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
        if (var1.lengthSqr() > 1.0E-7) {
            var1 = var1.normalize().scale(0.4).add(var0.scale(0.2));
        }

        this.mob.setDeltaMovement(var1.x, (double)this.yd, var1.z);
    }
}
