package net.minecraft.world.entity.ai.targeting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetingConditions {
    public static final TargetingConditions DEFAULT = forCombat();
    private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
    private final boolean isCombat;
    private double range = -1.0;
    private boolean checkLineOfSight = true;
    private boolean testInvisible = true;
    @Nullable
    private Predicate<LivingEntity> selector;

    private TargetingConditions(boolean param0) {
        this.isCombat = param0;
    }

    public static TargetingConditions forCombat() {
        return new TargetingConditions(true);
    }

    public static TargetingConditions forNonCombat() {
        return new TargetingConditions(false);
    }

    public TargetingConditions copy() {
        TargetingConditions var0 = this.isCombat ? forCombat() : forNonCombat();
        var0.range = this.range;
        var0.checkLineOfSight = this.checkLineOfSight;
        var0.testInvisible = this.testInvisible;
        var0.selector = this.selector;
        return var0;
    }

    public TargetingConditions range(double param0) {
        this.range = param0;
        return this;
    }

    public TargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;
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
        } else if (!param1.canBeSeenByAnyone()) {
            return false;
        } else if (this.selector != null && !this.selector.test(param1)) {
            return false;
        } else {
            if (param0 == null) {
                if (this.isCombat && (!param1.canBeSeenAsEnemy() || param1.level().getDifficulty() == Difficulty.PEACEFUL)) {
                    return false;
                }
            } else {
                if (this.isCombat && (!param0.canAttack(param1) || !param0.canAttackType(param1.getType()) || param0.isAlliedTo(param1))) {
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

                if (this.checkLineOfSight && param0 instanceof Mob var3 && !var3.getSensing().hasLineOfSight(param1)) {
                    return false;
                }
            }

            return true;
        }
    }
}
