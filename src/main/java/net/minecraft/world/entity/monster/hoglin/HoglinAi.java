package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);

    protected static Brain<?> makeBrain(Hoglin param0, Dynamic<?> param1) {
        Brain<Hoglin> var0 = new Brain<>(Hoglin.MEMORY_TYPES, Hoglin.SENSOR_TYPES, param1);
        initCoreActivity(param0, var0);
        initIdleActivity(param0, var0);
        initFightActivity(param0, var0);
        initRetreatActivity(param0, var0);
        var0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        var0.setDefaultActivity(Activity.IDLE);
        var0.useDefaultActivity();
        return var0;
    }

    private static void initCoreActivity(Hoglin param0, Brain<Hoglin> param1) {
        param1.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200)));
    }

    private static void initIdleActivity(Hoglin param0, Brain<Hoglin> param1) {
        float var0 = getMovementSpeed(param0);
        param1.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_WARPED_FUNGUS, 200),
                new AnimalMakeLove(EntityType.HOGLIN),
                SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_WARPED_FUNGUS, var0 * 1.8F, 8, true),
                new StartAttacking<Hoglin>(HoglinAi::findNearestValidAttackTarget),
                new RunIf<Hoglin>(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, var0, 8, false)),
                new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
                createIdleMovementBehaviors(var0)
            )
        );
    }

    private static void initFightActivity(Hoglin param0, Brain<Hoglin> param1) {
        float var0 = getMovementSpeed(param0);
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_WARPED_FUNGUS, 200),
                new AnimalMakeLove(EntityType.HOGLIN),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(var0 * 1.8F),
                new RunIf<>(Hoglin::isAdult, new MeleeAttack(1.5, 40)),
                new RunIf<>(AgableMob::isBaby, new MeleeAttack(1.5, 15)),
                new StopAttackingIfTargetInvalid()
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initRetreatActivity(Hoglin param0, Brain<Hoglin> param1) {
        float var0 = getMovementSpeed(param0) * 2.0F;
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, var0, 15, false),
                createIdleMovementBehaviors(getMovementSpeed(param0)),
                new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
                new EraseMemoryIf<Hoglin>(HoglinAi::hoglinsOutnumberPiglins, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors(float param0) {
        return new RunOne<>(
            ImmutableList.of(Pair.of(new RandomStroll(param0), 2), Pair.of(new SetWalkTargetFromLookTarget(param0, 3), 2), Pair.of(new DoNothing(30, 60), 1))
        );
    }

    protected static void updateActivity(Hoglin param0) {
        Brain<Hoglin> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        var0.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity var2 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != var2) {
            playActivitySound(param0);
        }

        param0.setAggressive(var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin param0, LivingEntity param1) {
        if (!param0.isBaby()) {
            if (param1.getType() == EntityType.PIGLIN && !hoglinsOutnumberPiglins(param0)) {
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
        param0.getBrain()
            .setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, param0.level.getGameTime(), (long)RETREAT_DURATION.randomValue(param0.level.random));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin param0x) {
        return !isPacified(param0x) && !isBreeding(param0x)
            ? param0x.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)
            : Optional.empty();
    }

    static boolean isPosNearNearestWarpedFungus(Hoglin param0, BlockPos param1) {
        Optional<BlockPos> var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_WARPED_FUNGUS);
        return var0.isPresent() && var0.get().closerThan(param1, 8.0);
    }

    private static boolean hoglinsOutnumberPiglins(Hoglin param0x) {
        if (param0x.isBaby()) {
            return false;
        } else {
            int var0x = param0x.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
            int var1 = param0x.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
            return var1 > var0x;
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
            if (EntitySelector.ATTACK_ALLOWED.test(param1)) {
                if (param1.getType() != EntityType.HOGLIN) {
                    if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(param0, param1, 4.0)) {
                        setAttackTarget(param0, param1);
                        broadcastAttackTarget(param0, param1);
                    }
                }
            }
        }
    }

    private static void setAttackTarget(Hoglin param0, LivingEntity param1) {
        param0.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, param1, param0.level.getGameTime(), 200L);
    }

    private static void broadcastAttackTarget(Hoglin param0, LivingEntity param1) {
        getVisibleAdultHoglins(param0).forEach(param1x -> setAttackTargetIfCloserThanCurrent(param1x, param1));
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin param0, LivingEntity param1) {
        Optional<LivingEntity> var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0, param1);
        setAttackTarget(param0, var1);
    }

    private static void playActivitySound(Hoglin param0) {
        param0.getBrain().getActiveNonCoreActivity().ifPresent(param1 -> {
            if (param1 == Activity.AVOID) {
                param0.playRetreatSound();
            } else if (param1 == Activity.FIGHT) {
                param0.playAngrySound();
            }

        });
    }

    protected static void maybePlayActivitySound(Hoglin param0) {
        if ((double)param0.level.random.nextFloat() < 0.0125) {
            playActivitySound(param0);
        }

    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin param0) {
        return (List<Hoglin>)(param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS)
            ? param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).get()
            : Lists.newArrayList());
    }

    public static float getMovementSpeed(Hoglin param0) {
        return (float)param0.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
    }

    private static boolean isBreeding(Hoglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }

    protected static boolean isIdle(Hoglin param0) {
        return param0.getBrain().isActive(Activity.IDLE);
    }
}
