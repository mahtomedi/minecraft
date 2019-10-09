package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
    protected final PathfinderMob mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    protected T toAvoid;
    protected final float maxDist;
    protected Path path;
    protected final PathNavigation pathNav;
    protected final Class<T> avoidClass;
    protected final Predicate<LivingEntity> avoidPredicate;
    protected final Predicate<LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;

    public AvoidEntityGoal(PathfinderMob param0, Class<T> param1, float param2, double param3, double param4) {
        this(param0, param1, param0x -> true, param2, param3, param4, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
    }

    public AvoidEntityGoal(
        PathfinderMob param0, Class<T> param1, Predicate<LivingEntity> param2, float param3, double param4, double param5, Predicate<LivingEntity> param6
    ) {
        this.mob = param0;
        this.avoidClass = param1;
        this.avoidPredicate = param2;
        this.maxDist = param3;
        this.walkSpeedModifier = param4;
        this.sprintSpeedModifier = param5;
        this.predicateOnAvoidEntity = param6;
        this.pathNav = param0.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.avoidEntityTargeting = new TargetingConditions().range((double)param3).selector(param6.and(param2));
    }

    public AvoidEntityGoal(PathfinderMob param0, Class<T> param1, float param2, double param3, double param4, Predicate<LivingEntity> param5) {
        this(param0, param1, param0x -> true, param2, param3, param4, param5);
    }

    @Override
    public boolean canUse() {
        this.toAvoid = this.mob
            .level
            .getNearestLoadedEntity(
                this.avoidClass,
                this.avoidEntityTargeting,
                this.mob,
                this.mob.getX(),
                this.mob.getY(),
                this.mob.getZ(),
                this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0, (double)this.maxDist)
            );
        if (this.toAvoid == null) {
            return false;
        } else {
            Vec3 var0 = RandomPos.getPosAvoid(this.mob, 16, 7, this.toAvoid.position());
            if (var0 == null) {
                return false;
            } else if (this.toAvoid.distanceToSqr(var0.x, var0.y, var0.z) < this.toAvoid.distanceToSqr(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(var0.x, var0.y, var0.z, 0);
                return this.path != null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    @Override
    public void start() {
        this.pathNav.moveTo(this.path, this.walkSpeedModifier);
    }

    @Override
    public void stop() {
        this.toAvoid = null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr(this.toAvoid) < 49.0) {
            this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }

    }
}
