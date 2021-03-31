package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class GoatAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final UniformInt TIMES_BETWEEN_LONG_JUMPS = UniformInt.of(600, 1200);

    protected static void initMemories(Goat param0) {
        param0.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIMES_BETWEEN_LONG_JUMPS.sample(param0.level.random));
    }

    protected static Brain<?> makeBrain(Brain<Goat> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        initLongJumpActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Goat> param0) {
        param0.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new AnimalPanic(2.0F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)
            )
        );
    }

    private static void initIdleActivity(Brain<Goat> param0) {
        param0.addActivity(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), UniformInt.of(30, 60))),
                Pair.of(0, new AnimalMakeLove(EntityType.GOAT, 1.0F)),
                Pair.of(1, new FollowTemptation(param0x -> 1.25F)),
                Pair.of(2, new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, 1.25F)),
                Pair.of(
                    3,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(new RandomStroll(1.0F), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2), Pair.of(new DoNothing(30, 60), 1)
                        )
                    )
                )
            )
        );
    }

    private static void initLongJumpActivity(Brain<Goat> param0) {
        param0.addActivityWithConditions(
            Activity.LONG_JUMP,
            ImmutableList.of(
                Pair.of(0, new LongJumpMidJump(TIMES_BETWEEN_LONG_JUMPS)), Pair.of(1, new LongJumpToRandomPos(TIMES_BETWEEN_LONG_JUMPS, 5, 5, 1.5F))
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)
            )
        );
    }

    public static void updateActivity(Goat param0) {
        param0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.LONG_JUMP, Activity.IDLE));
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(Items.WHEAT);
    }
}
