package net.minecraft.world.entity.ai.targeting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetingConditions {
    public static final TargetingConditions DEFAULT = new TargetingConditions();
    private double range = -1.0;
    private boolean allowInvulnerable;
    private boolean allowSameTeam;
    private boolean allowUnseeable;
    private boolean allowNonAttackable;
    private boolean testInvisible = true;
    private Predicate<LivingEntity> selector;

    public TargetingConditions copy() {
        TargetingConditions var0 = new TargetingConditions();
        var0.range = this.range;
        var0.allowInvulnerable = this.allowInvulnerable;
        var0.allowSameTeam = this.allowSameTeam;
        var0.allowUnseeable = this.allowUnseeable;
        var0.allowNonAttackable = this.allowNonAttackable;
        var0.testInvisible = this.testInvisible;
        var0.selector = this.selector;
        return var0;
    }

    public TargetingConditions range(double param0) {
        this.range = param0;
        return this;
    }

    public TargetingConditions allowInvulnerable() {
        this.allowInvulnerable = true;
        return this;
    }

    public TargetingConditions allowSameTeam() {
        this.allowSameTeam = true;
        return this;
    }

    public TargetingConditions allowUnseeable() {
        this.allowUnseeable = true;
        return this;
    }

    public TargetingConditions allowNonAttackable() {
        this.allowNonAttackable = true;
        return this;
    }

    public TargetingConditions ignoreInvisibilityTesting() {
        this.testInvisible = false;
        return this;
    }

    public TargetingConditions selector(@Nullable Predicate<LivingEntity> param0) {
        this.selector = param0;
        return this;
    }

    public boolean test(@Nullable LivingEntity param0, LivingEntity param1) {
        if (param0 == param1) {
            return false;
        } else if (param1.isSpectator()) {
            return false;
        } else if (!param1.isAlive()) {
            return false;
        } else if (!this.allowInvulnerable && param1.isInvulnerable()) {
            return false;
        } else if (this.selector != null && !this.selector.test(param1)) {
            return false;
        } else {
            if (param0 != null) {
                if (!this.allowNonAttackable) {
                    if (!param0.canAttack(param1)) {
                        return false;
                    }

                    if (!param0.canAttackType(param1.getType())) {
                        return false;
                    }
                }

                if (!this.allowSameTeam && param0.isAlliedTo(param1)) {
                    return false;
                }

                if (this.range > 0.0) {
                    double var0 = this.testInvisible ? param1.getVisibilityPercent(param0) : 1.0;
                    double var1 = Math.max(this.range * var0, 2.0);
                    double var2 = param0.distanceToSqr(param1.getX(), param1.getY(), param1.getZ());
                    if (var2 > var1 * var1) {
                        return false;
                    }
                }

                if (!this.allowUnseeable && param0 instanceof Mob && !((Mob)param0).getSensing().canSee(param1)) {
                    return false;
                }
            }

            return true;
        }
    }
}
