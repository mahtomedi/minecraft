package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final int ATTACK_DURATION = 200;
    private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_IDLING = 8;
    private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_RETREATING = 15;
    private static final int ATTACK_INTERVAL = 40;
    private static final int BABY_ATTACK_INTERVAL = 15;
    private static final int REPELLENT_PACIFY_TIME = 200;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING_REPELLENT = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.3F;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 0.6F;

    protected static Brain<?> makeBrain(Brain<Hoglin> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        initFightActivity(param0);
        initRetreatActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Hoglin> param0) {
        param0.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<Hoglin> param0) {
        param0.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
                new AnimalMakeLove(EntityType.HOGLIN, 0.6F),
                SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true),
                StartAttacking.create(HoglinAi::findNearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)),
                SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
                BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 0.6F),
                createIdleMovementBehaviors()
            )
        );
    }

    private static void initFightActivity(Brain<Hoglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
                new AnimalMakeLove(EntityType.HOGLIN, 0.6F),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                BehaviorBuilder.triggerIf(Hoglin::isAdult, MeleeAttack.create(40)),
                BehaviorBuilder.triggerIf(AgeableMob::isBaby, MeleeAttack.create(15)),
                StopAttackingIfTargetInvalid.create(),
                EraseMemoryIf.create(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initRetreatActivity(Brain<Hoglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false),
                createIdleMovementBehaviors(),
                SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
                EraseMemoryIf.<PathfinderMob>create(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors() {
        return new RunOne<>(
            ImmutableList.of(Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
        );
    }

    protected static void updateActivity(Hoglin param0) {
        Brain<Hoglin> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        var0.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity var2 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != var2) {
            getSoundForCurrentActivity(param0).ifPresent(param0::playSoundEvent);
        }

        param0.setAggressive(var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin param0, LivingEntity param1) {
        if (!param0.isBaby()) {
            if (param1.getType() == EntityType.PIGLIN && piglinsOutnumberHoglins(param0)) {
                setAvoidTarget(param0, param1);
                broadcastRetreat(param0, param1);
            } else {
                broadcastAttackTarget(param0, param1);
            }
        }
    }

    private static void broadcastRetreat(Hoglin param0, LivingEntity param1) {
        getVisibleAdultHoglins(param0).forEach(param1x -> retreatFromNearestTarget(param1x, param1));
    }

    private static void retreatFromNearestTarget(Hoglin param0, LivingEntity param1) {
        Brain<Hoglin> var1 = param0.getBrain();
        LivingEntity var0 = BehaviorUtils.getNearestTarget(param0, var1.getMemory(MemoryModuleType.AVOID_TARGET), param1);
        var0 = BehaviorUtils.getNearestTarget(param0, var1.getMemory(MemoryModuleType.ATTACK_TARGET), var0);
        setAvoidTarget(param0, var0);
    }

    private static void setAvoidTarget(Hoglin param0, LivingEntity param1) {
        param0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, (long)RETREAT_DURATION.sample(param0.level().random));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin param0x) {
        return !isPacified(param0x) && !isBreeding(param0x)
            ? param0x.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
            : Optional.empty();
    }

    static boolean isPosNearNearestRepellent(Hoglin param0, BlockPos param1) {
        Optional<BlockPos> var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
        return var0.isPresent() && var0.get().closerThan(param1, 8.0);
    }

    private static boolean wantsToStopFleeing(Hoglin param0x) {
        return param0x.isAdult() && !piglinsOutnumberHoglins(param0x);
    }

    private static boolean piglinsOutnumberHoglins(Hoglin param0) {
        if (param0.isBaby()) {
            return false;
        } else {
            int var0 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
            int var1 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
            return var0 > var1;
        }
    }

    protected static void wasHurtBy(Hoglin param0, LivingEntity param1) {
        Brain<Hoglin> var0 = param0.getBrain();
        var0.eraseMemory(MemoryModuleType.PACIFIED);
        var0.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (param0.isBaby()) {
            retreatFromNearestTarget(param0, param1);
        } else {
            maybeRetaliate(param0, param1);
        }
    }

    private static void maybeRetaliate(Hoglin param0, LivingEntity param1) {
        if (!param0.getBrain().isActive(Activity.AVOID) || param1.getType() != EntityType.PIGLIN) {
            if (param1.getType() != EntityType.HOGLIN) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(param0, param1, 4.0)) {
                    if (Sensor.isEntityAttackable(param0, param1)) {
                        setAttackTarget(param0, param1);
                        broadcastAttackTarget(param0, param1);
                    }
                }
            }
        }
    }

    private static void setAttackTarget(Hoglin param0, LivingEntity param1) {
        Brain<Hoglin> var0 = param0.getBrain();
        var0.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        var0.eraseMemory(MemoryModuleType.BREED_TARGET);
        var0.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, param1, 200L);
    }

    private static void broadcastAttackTarget(Hoglin param0, LivingEntity param1) {
        getVisibleAdultHoglins(param0).forEach(param1x -> setAttackTargetIfCloserThanCurrent(param1x, param1));
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin param0, LivingEntity param1) {
        if (!isPacified(param0)) {
            Optional<LivingEntity> var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
            LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0, param1);
            setAttackTarget(param0, var1);
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin param0) {
        return param0.getBrain().getActiveNonCoreActivity().map(param1 -> getSoundForActivity(param0, param1));
    }

    private static SoundEvent getSoundForActivity(Hoglin param0, Activity param1) {
        if (param1 == Activity.AVOID || param0.isConverting()) {
            return SoundEvents.HOGLIN_RETREAT;
        } else if (param1 == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
        } else {
            return isNearRepellent(param0) ? SoundEvents.HOGLIN_RETREAT : SoundEvents.HOGLIN_AMBIENT;
        }
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    private static boolean isNearRepellent(Hoglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean isBreeding(Hoglin param0x) {
        return param0x.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }
}
