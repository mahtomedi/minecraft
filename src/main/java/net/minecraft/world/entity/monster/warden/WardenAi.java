package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.Digging;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.Emerging;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.Sniffing;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
    private static final int MAX_LOOK_DIST = 8;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
    private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
    private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
    private static final int MELEE_ATTACK_COOLDOWN = 18;
    private static final int DIGGING_DURATION = Mth.ceil(100.0F);
    public static final int EMERGE_DURATION = Mth.ceil(133.59999F);
    public static final int ROAR_DURATION = Mth.ceil(84.0F);
    private static final int SNIFFING_DURATION = Mth.ceil(83.2F);
    public static final int DIGGING_COOLDOWN = 1200;
    private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
    private static final Behavior<Warden> DIG_COOLDOWN_SETTER = new Behavior<Warden>(ImmutableMap.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.REGISTERED)) {
        protected void start(ServerLevel param0, Warden param1, long param2) {
            WardenAi.setDigCooldown(param1);
        }
    };

    public static void updateActivity(Warden param0) {
        param0.getBrain()
            .setActiveActivityToFirstValid(
                ImmutableList.of(Activity.EMERGE, Activity.DIG, Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.SNIFF, Activity.IDLE)
            );
    }

    protected static Brain<?> makeBrain(Warden param0, Brain<Warden> param1) {
        initCoreActivity(param1);
        initEmergeActivity(param1);
        initDiggingActivity(param1);
        initIdleActivity(param1);
        initRoarActivity(param1);
        initFightActivity(param0, param1);
        initInvestigateActivity(param1);
        initSniffingActivity(param1);
        param1.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param1.setDefaultActivity(Activity.IDLE);
        param1.useDefaultActivity();
        return param1;
    }

    private static void initCoreActivity(Brain<Warden> param0) {
        param0.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new SetWardenLookTarget(), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static void initEmergeActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, ImmutableList.of(new Emerging<>(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
    }

    private static void initDiggingActivity(Brain<Warden> param0) {
        param0.addActivityWithConditions(
            Activity.DIG,
            ImmutableList.of(Pair.of(0, new Digging<>(DIGGING_DURATION))),
            ImmutableSet.of(Pair.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.VALUE_ABSENT))
        );
    }

    private static void initIdleActivity(Brain<Warden> param0) {
        param0.addActivity(Activity.IDLE, 10, ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new TryToSniff(), createIdleMovementBehaviors()));
    }

    private static void initInvestigateActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.INVESTIGATE,
            5,
            ImmutableList.of(
                new SetRoarTarget<>(Warden::getEntityAngryAt), new GoToTargetLocation(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F), new DoNothing(10, 20)
            ),
            MemoryModuleType.DISTURBANCE_LOCATION
        );
    }

    private static void initSniffingActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.SNIFF, 5, ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING
        );
    }

    private static void initRoarActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.ROAR,
            10,
            ImmutableList.of(new Roar(), new StartAttackingAfterTimeOut(param0x -> true, Warden::getEntityAngryAt, ROAR_DURATION)),
            MemoryModuleType.ROAR_TARGET
        );
    }

    private static void initFightActivity(Warden param0, Brain<Warden> param1) {
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                DIG_COOLDOWN_SETTER,
                new StopAttackingIfTargetInvalid<>(
                    param1x -> param0.getAngerLevel() != AngerLevel.ANGRY || !isValidAttackTarget(param0, param1x), WardenAi::onTargetInvalid, false
                ),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F),
                new SetEntityLookTarget(param1x -> isTarget(param0, param1x), 8.0F),
                new MeleeAttack(18)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static boolean isTarget(Warden param0, LivingEntity param1) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(param1x -> param1x == param1).isPresent();
    }

    private static void onTargetInvalid(Warden param0x, LivingEntity param1x) {
        if (param1x.isDeadOrDying()) {
            param0x.clearAnger(param1x);
        }

        setDigCooldown(param0x);
    }

    private static RunOne<Warden> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    public static void setDigCooldown(LivingEntity param0) {
        if (param0.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }

    }

    public static void setDisturbanceLocation(Warden param0, BlockPos param1) {
        if (shouldInvestigate(param0)) {
            setDigCooldown(param0);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1), 100L);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, param1, 100L);
            param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }

    }

    private static boolean isValidAttackTarget(Warden param0, LivingEntity param1) {
        return param0.getEntityAngryAt().filter(param1x -> param1x == param1).isPresent();
    }

    private static boolean shouldInvestigate(Warden param0) {
        return param0.getEntityAngryAt().isEmpty() && param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty();
    }
}