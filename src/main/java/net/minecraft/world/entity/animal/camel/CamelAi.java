package net.minecraft.world.entity.animal.camel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;

public class CamelAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 4.0F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 2.5F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 2.5F;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super Camel>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.CAMEL_TEMPTATIONS, SensorType.NEAREST_ADULT
    );
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.GAZE_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_ADULT
    );

    protected static void initMemories(Camel param0, RandomSource param1) {
    }

    public static Brain.Provider<Camel> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Camel> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Camel> param0) {
        param0.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new Swim(0.8F),
                new CamelAi.CamelPanic(4.0F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS)
            )
        );
    }

    private static void initIdleActivity(Brain<Camel> param0) {
        param0.addActivity(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                Pair.of(1, new AnimalMakeLove(EntityType.CAMEL, 1.0F)),
                Pair.of(2, new FollowTemptation(param0x -> 2.5F)),
                Pair.of(3, BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 2.5F))),
                Pair.of(4, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                Pair.of(
                    5,
                    new RunOne<>(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        ImmutableList.of(
                            Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), RandomStroll.stroll(2.0F)), 1),
                            Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), SetWalkTargetFromLookTarget.create(2.0F, 3)), 1),
                            Pair.of(new CamelAi.RandomSitting(20), 1),
                            Pair.of(new DoNothing(30, 60), 1)
                        )
                    )
                )
            )
        );
    }

    public static void updateActivity(Camel param0) {
        param0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    public static Ingredient getTemptations() {
        return Camel.TEMPTATION_ITEM;
    }

    public static class CamelPanic extends AnimalPanic {
        public CamelPanic(float param0) {
            super(param0);
        }

        @Override
        protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
            if (param1 instanceof Camel var0) {
                var0.standUpInstantly();
            }

            super.start(param0, param1, param2);
        }
    }

    public static class RandomSitting extends Behavior<Camel> {
        private final int minimalPoseTicks;

        public RandomSitting(int param0) {
            super(ImmutableMap.of());
            this.minimalPoseTicks = param0 * 20;
        }

        protected boolean checkExtraStartConditions(ServerLevel param0, Camel param1) {
            return !param1.isInWater()
                && param1.getPoseTime() >= (long)this.minimalPoseTicks
                && !param1.isLeashed()
                && param1.onGround()
                && !param1.hasControllingPassenger();
        }

        protected void start(ServerLevel param0, Camel param1, long param2) {
            if (param1.isCamelSitting()) {
                param1.standUp();
            } else if (!param1.isPanicking()) {
                param1.sitDown();
            }

        }
    }
}
