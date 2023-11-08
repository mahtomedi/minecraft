package net.minecraft.world.entity.monster.breeze;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class BreezeAi {
    public static final float SPEED_MULTIPLIER_WHEN_SLIDING = 0.6F;
    public static final float JUMP_CIRCLE_INNER_RADIUS = 4.0F;
    public static final float JUMP_CIRCLE_MIDDLE_RADIUS = 8.0F;
    public static final float JUMP_CIRCLE_OUTER_RADIUS = 20.0F;
    static final List<SensorType<? extends Sensor<? super Breeze>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.BREEZE_ATTACK_ENTITY_SENSOR
    );
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_ATTACKABLE,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.BREEZE_JUMP_COOLDOWN,
        MemoryModuleType.BREEZE_JUMP_INHALING,
        MemoryModuleType.BREEZE_SHOOT,
        MemoryModuleType.BREEZE_SHOOT_CHARGING,
        MemoryModuleType.BREEZE_SHOOT_RECOVERING,
        MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
        MemoryModuleType.BREEZE_JUMP_TARGET,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.PATH
    );

    protected static Brain<?> makeBrain(Brain<Breeze> param0) {
        initCoreActivity(param0);
        initFightActivity(param0);
        param0.setCoreActivities(Set.of(Activity.CORE));
        param0.setDefaultActivity(Activity.FIGHT);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Breeze> param0) {
        param0.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new LookAtTargetSink(45, 90), new BreezeAi.SlideToTargetSink(20, 100)));
    }

    private static void initFightActivity(Brain<Breeze> param0) {
        param0.addActivityWithConditions(
            Activity.FIGHT,
            ImmutableList.of(
                Pair.of(0, StartAttacking.create(param0x -> param0x.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
                Pair.of(1, StopAttackingIfTargetInvalid.create()),
                Pair.of(2, new Shoot()),
                Pair.of(3, new LongJump()),
                Pair.of(4, new Slide()),
                Pair.of(4, new RunOne<>(ImmutableList.of(Pair.of(new DoNothing(20, 100), 1), Pair.of(RandomStroll.stroll(0.6F), 2))))
            ),
            Set.of()
        );
    }

    static class SlideToTargetSink extends MoveToTargetSink {
        SlideToTargetSink(int param0, int param1) {
            super(param0, param1);
        }

        @Override
        protected void start(ServerLevel param0, Mob param1, long param2) {
            super.start(param0, param1, param2);
            param1.playSound(SoundEvents.BREEZE_SLIDE);
            param1.setPose(Pose.SLIDING);
        }

        @Override
        protected void stop(ServerLevel param0, Mob param1, long param2) {
            super.stop(param0, param1, param2);
            param1.setPose(Pose.STANDING);
            if (param1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                param1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
            }

        }
    }
}
