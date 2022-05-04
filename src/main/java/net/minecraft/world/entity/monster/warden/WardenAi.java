package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.warden.Digging;
import net.minecraft.world.entity.ai.behavior.warden.Emerging;
import net.minecraft.world.entity.ai.behavior.warden.Roar;
import net.minecraft.world.entity.ai.behavior.warden.SetRoarTarget;
import net.minecraft.world.entity.ai.behavior.warden.SetWardenLookTarget;
import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
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
    private static final List<SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.NEAREST_ATTACKABLE,
        MemoryModuleType.ROAR_TARGET,
        MemoryModuleType.DISTURBANCE_LOCATION,
        MemoryModuleType.RECENT_PROJECTILE,
        MemoryModuleType.IS_SNIFFING,
        MemoryModuleType.IS_EMERGING,
        MemoryModuleType.ROAR_SOUND_DELAY,
        MemoryModuleType.DIG_COOLDOWN,
        MemoryModuleType.ROAR_SOUND_COOLDOWN,
        MemoryModuleType.SNIFF_COOLDOWN,
        MemoryModuleType.TOUCH_COOLDOWN,
        MemoryModuleType.VIBRATION_COOLDOWN,
        MemoryModuleType.SONIC_BOOM_COOLDOWN,
        MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
        MemoryModuleType.SONIC_BOOM_SOUND_DELAY
    );
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

    protected static Brain<?> makeBrain(Warden param0, Dynamic<?> param1) {
        Brain.Provider<Warden> var0 = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
        Brain<Warden> var1 = var0.makeBrain(param1);
        initCoreActivity(var1);
        initEmergeActivity(var1);
        initDiggingActivity(var1);
        initIdleActivity(var1);
        initRoarActivity(var1);
        initFightActivity(param0, var1);
        initInvestigateActivity(var1);
        initSniffingActivity(var1);
        var1.setCoreActivities(ImmutableSet.of(Activity.CORE));
        var1.setDefaultActivity(Activity.IDLE);
        var1.useDefaultActivity();
        return var1;
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
        param0.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                new SetRoarTarget<>(Warden::getEntityAngryAt),
                new TryToSniff(),
                new RunOne(
                    ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryStatus.VALUE_ABSENT),
                    ImmutableList.of(Pair.of(new RandomStroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1))
                )
            )
        );
    }

    private static void initInvestigateActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.INVESTIGATE,
            5,
            ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new GoToTargetLocation(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)),
            MemoryModuleType.DISTURBANCE_LOCATION
        );
    }

    private static void initSniffingActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.SNIFF, 5, ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING
        );
    }

    private static void initRoarActivity(Brain<Warden> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
    }

    private static void initFightActivity(Warden param0, Brain<Warden> param1) {
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                DIG_COOLDOWN_SETTER,
                new StopAttackingIfTargetInvalid<>(
                    param1x -> !param0.getAngerLevel().isAngry() || !param0.canTargetEntity(param1x), WardenAi::onTargetInvalid, false
                ),
                new SetEntityLookTarget(param1x -> isTarget(param0, param1x), (float)param0.getAttributeValue(Attributes.FOLLOW_RANGE)),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F),
                new SonicBoom(),
                new MeleeAttack(18)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static boolean isTarget(Warden param0, LivingEntity param1) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(param1x -> param1x == param1).isPresent();
    }

    private static void onTargetInvalid(Warden param0x, LivingEntity param1x) {
        if (!param0x.canTargetEntity(param1x)) {
            param0x.clearAnger(param1x);
        }

        setDigCooldown(param0x);
    }

    public static void setDigCooldown(LivingEntity param0) {
        if (param0.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }

    }

    public static void setDisturbanceLocation(Warden param0, BlockPos param1) {
        if (param0.level.getWorldBorder().isWithinBounds(param1)
            && !param0.getEntityAngryAt().isPresent()
            && !param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            setDigCooldown(param0);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1), 100L);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, param1, 100L);
            param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
