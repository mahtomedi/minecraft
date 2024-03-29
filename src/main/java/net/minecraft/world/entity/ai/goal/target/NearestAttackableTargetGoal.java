package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public NearestAttackableTargetGoal(Mob param0, Class<T> param1, boolean param2) {
        this(param0, param1, 10, param2, false, null);
    }

    public NearestAttackableTargetGoal(Mob param0, Class<T> param1, boolean param2, Predicate<LivingEntity> param3) {
        this(param0, param1, 10, param2, false, param3);
    }

    public NearestAttackableTargetGoal(Mob param0, Class<T> param1, boolean param2, boolean param3) {
        this(param0, param1, 10, param2, param3, null);
    }

    public NearestAttackableTargetGoal(Mob param0, Class<T> param1, int param2, boolean param3, boolean param4, @Nullable Predicate<LivingEntity> param5) {
        super(param0, param3, param4);
        this.targetType = param1;
        this.randomInterval = reducedTickDelay(param2);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(param5);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    protected AABB getTargetSearchArea(double param0) {
        return this.mob.getBoundingBox().inflate(param0, 4.0, param0);
    }

    protected void findTarget() {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob
                .level()
                .getNearestEntity(
                    this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), param0 -> true),
                    this.targetConditions,
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
                );
        } else {
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity param0) {
        this.target = param0;
    }
}
