package net.minecraft.world.entity.animal.sniffer;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.slf4j.Logger;

public class SnifferAi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LOOK_DISTANCE = 6;
    static final List<SensorType<? extends Sensor<? super Sniffer>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS
    );
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.SNIFFER_SNIFFING_TARGET,
        MemoryModuleType.SNIFFER_DIGGING,
        MemoryModuleType.SNIFFER_HAPPY,
        MemoryModuleType.SNIFF_COOLDOWN,
        MemoryModuleType.SNIFFER_EXPLORED_POSITIONS,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.BREED_TARGET
    );
    private static final int SNIFFING_COOLDOWN_TICKS = 9600;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_SNIFFING = 1.25F;

    protected static Brain<?> makeBrain(Brain<Sniffer> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        initSniffingActivity(param0);
        initDigActivity(param0);
        param0.setCoreActivities(Set.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Sniffer> param0) {
        param0.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new AnimalPanic(2.0F) {
            @Override
            protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
                param1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
                param1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
                ((Sniffer)param1).transitionTo(Sniffer.State.IDLING);
                super.start(param0, param1, param2);
            }
        }, new MoveToTargetSink(10000, 15000)));
    }

    private static void initSniffingActivity(Brain<Sniffer> param0) {
        param0.addActivityWithConditions(
            Activity.SNIFF,
            ImmutableList.of(Pair.of(0, new SnifferAi.Searching())),
            Set.of(
                Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initDigActivity(Brain<Sniffer> param0) {
        param0.addActivityWithConditions(
            Activity.DIG,
            ImmutableList.of(Pair.of(0, new SnifferAi.Digging(160, 180)), Pair.of(0, new SnifferAi.FinishedDigging(40))),
            Set.of(
                Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initIdleActivity(Brain<Sniffer> param0) {
        param0.addActivityWithConditions(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new AnimalMakeLove(EntityType.SNIFFER, 1.0F)),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new SnifferAi.FeelingHappy(40, 100)),
                Pair.of(
                    0,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                            Pair.of(new SnifferAi.Scenting(40, 80), 1),
                            Pair.of(new SnifferAi.Sniffing(40, 80), 1),
                            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 6.0F), 1),
                            Pair.of(RandomStroll.stroll(1.0F), 1),
                            Pair.of(new DoNothing(5, 20), 2)
                        )
                    )
                )
            ),
            Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT))
        );
    }

    static void updateActivity(Sniffer param0) {
        param0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
    }

    static class Digging extends Behavior<Sniffer> {
        Digging(int param0, int param1) {
            super(
                Map.of(
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_ABSENT
                ),
                param0,
                param1
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel param0, Sniffer param1) {
            return !param1.isPanicking() && !param1.isInWater();
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            return param1.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && !param1.isPanicking();
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.DIGGING);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
        }
    }

    static class FeelingHappy extends Behavior<Sniffer> {
        FeelingHappy(int param0, int param1) {
            super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_PRESENT), param0, param1);
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            return true;
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.FEELING_HAPPY);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.IDLING);
            param1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_HAPPY);
        }
    }

    static class FinishedDigging extends Behavior<Sniffer> {
        FinishedDigging(int param0) {
            super(
                Map.of(
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_PRESENT
                ),
                param0,
                param0
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel param0, Sniffer param1) {
            return true;
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            return param1.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.RISING);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            boolean var0 = this.timedOut(param2);
            param1.transitionTo(Sniffer.State.IDLING).onDiggingComplete(var0);
            param1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
            param1.getBrain().setMemory(MemoryModuleType.SNIFFER_HAPPY, true);
        }
    }

    static class Scenting extends Behavior<Sniffer> {
        Scenting(int param0, int param1) {
            super(
                Map.of(
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_HAPPY,
                    MemoryStatus.VALUE_ABSENT
                ),
                param0,
                param1
            );
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            return true;
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.SCENTING);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.IDLING);
        }
    }

    static class Searching extends Behavior<Sniffer> {
        Searching() {
            super(
                Map.of(
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_PRESENT
                ),
                600
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel param0, Sniffer param1) {
            return !param1.isPanicking() && !param1.isInWater();
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            if (param1.isPanicking() && !param1.isInWater()) {
                return false;
            } else {
                Optional<BlockPos> var0 = param1.getBrain()
                    .getMemory(MemoryModuleType.WALK_TARGET)
                    .map(WalkTarget::getTarget)
                    .map(PositionTracker::currentBlockPosition);
                Optional<BlockPos> var1 = param1.getBrain().getMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
                return !var0.isEmpty() && !var1.isEmpty() ? var1.get().equals(var0.get()) : false;
            }
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.SEARCHING);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            if (param1.canDig()) {
                param1.getBrain().setMemory(MemoryModuleType.SNIFFER_DIGGING, true);
            }

            param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            param1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
        }
    }

    static class Sniffing extends Behavior<Sniffer> {
        Sniffing(int param0, int param1) {
            super(
                Map.of(
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_ABSENT
                ),
                param0,
                param1
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel param0, Sniffer param1) {
            return !param1.isBaby() && !param1.isInWater();
        }

        protected boolean canStillUse(ServerLevel param0, Sniffer param1, long param2) {
            return !param1.isPanicking();
        }

        protected void start(ServerLevel param0, Sniffer param1, long param2) {
            param1.transitionTo(Sniffer.State.SNIFFING);
        }

        protected void stop(ServerLevel param0, Sniffer param1, long param2) {
            boolean var0 = this.timedOut(param2);
            param1.transitionTo(Sniffer.State.IDLING);
            if (var0) {
                param1.calculateDigPosition().ifPresent(param1x -> {
                    param1.getBrain().setMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET, param1x);
                    param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(param1x, 1.25F, 0));
                });
            }

        }
    }
}
