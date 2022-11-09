package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.Croak;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToPreferredBlock;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindLand;
import net.minecraft.world.entity.ai.behavior.TryFindLandNearWater;
import net.minecraft.world.entity.ai.behavior.TryLaySpawnOnWaterNearLand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FrogAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_ON_LAND = 1.0F;
    private static final float SPEED_MULTIPLIER_IN_WATER = 0.75F;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(100, 140);
    private static final int MAX_LONG_JUMP_HEIGHT = 2;
    private static final int MAX_LONG_JUMP_WIDTH = 4;
    private static final float MAX_JUMP_VELOCITY = 1.5F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

    protected static void initMemories(Frog param0, RandomSource param1) {
        param0.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample(param1));
    }

    protected static Brain<?> makeBrain(Brain<Frog> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        initSwimActivity(param0);
        initLaySpawnActivity(param0);
        initTongueActivity(param0);
        initJumpActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Frog> param0) {
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

    private static void initIdleActivity(Brain<Frog> param0) {
        param0.addActivityWithConditions(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                Pair.of(0, new AnimalMakeLove(EntityType.FROG, 1.0F)),
                Pair.of(1, new FollowTemptation(param0x -> 1.25F)),
                Pair.of(2, StartAttacking.create(FrogAi::canAttack, param0x -> param0x.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
                Pair.of(3, TryFindLand.create(6, 1.0F)),
                Pair.of(
                    4,
                    new RunOne<>(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        ImmutableList.of(
                            Pair.of(RandomStroll.stroll(1.0F), 1),
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                            Pair.of(new Croak(), 3),
                            Pair.of(BehaviorBuilder.triggerIf(Entity::isOnGround), 2)
                        )
                    )
                )
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)
            )
        );
    }

    private static void initSwimActivity(Brain<Frog> param0) {
        param0.addActivityWithConditions(
            Activity.SWIM,
            ImmutableList.of(
                Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                Pair.of(1, new FollowTemptation(param0x -> 1.25F)),
                Pair.of(2, StartAttacking.create(FrogAi::canAttack, param0x -> param0x.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
                Pair.of(3, TryFindLand.create(8, 1.5F)),
                Pair.of(
                    5,
                    new GateBehavior<>(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        ImmutableSet.of(),
                        GateBehavior.OrderPolicy.ORDERED,
                        GateBehavior.RunningPolicy.TRY_ALL,
                        ImmutableList.of(
                            Pair.of(RandomStroll.swim(0.75F), 1),
                            Pair.of(RandomStroll.stroll(1.0F, true), 1),
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                            Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), 5)
                        )
                    )
                )
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initLaySpawnActivity(Brain<Frog> param0) {
        param0.addActivityWithConditions(
            Activity.LAY_SPAWN,
            ImmutableList.of(
                Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                Pair.of(1, StartAttacking.create(FrogAi::canAttack, param0x -> param0x.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
                Pair.of(2, TryFindLandNearWater.create(8, 1.0F)),
                Pair.of(3, TryLaySpawnOnWaterNearLand.create(Blocks.FROGSPAWN)),
                Pair.of(
                    4,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(RandomStroll.stroll(1.0F), 2),
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                            Pair.of(new Croak(), 2),
                            Pair.of(BehaviorBuilder.triggerIf(Entity::isOnGround), 1)
                        )
                    )
                )
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initJumpActivity(Brain<Frog> param0) {
        param0.addActivityWithConditions(
            Activity.LONG_JUMP,
            ImmutableList.of(
                Pair.of(0, new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.FROG_STEP)),
                Pair.of(
                    1,
                    new LongJumpToPreferredBlock<>(
                        TIME_BETWEEN_LONG_JUMPS,
                        2,
                        4,
                        1.5F,
                        param0x -> SoundEvents.FROG_LONG_JUMP,
                        BlockTags.FROG_PREFER_JUMP_TO,
                        0.5F,
                        FrogAi::isAcceptableLandingSpot
                    )
                )
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)
            )
        );
    }

    private static void initTongueActivity(Brain<Frog> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.TONGUE,
            0,
            ImmutableList.of(StopAttackingIfTargetInvalid.create(), new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT)),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static <E extends Mob> boolean isAcceptableLandingSpot(E param0x, BlockPos param1) {
        Level var0 = param0x.level;
        BlockPos var1 = param1.below();
        if (var0.getFluidState(param1).isEmpty() && var0.getFluidState(var1).isEmpty() && var0.getFluidState(param1.above()).isEmpty()) {
            BlockState var2 = var0.getBlockState(param1);
            BlockState var3 = var0.getBlockState(var1);
            if (!var2.is(BlockTags.FROG_PREFER_JUMP_TO) && !var3.is(BlockTags.FROG_PREFER_JUMP_TO)) {
                BlockPathTypes var4 = WalkNodeEvaluator.getBlockPathTypeStatic(var0, param1.mutable());
                BlockPathTypes var5 = WalkNodeEvaluator.getBlockPathTypeStatic(var0, var1.mutable());
                return var4 != BlockPathTypes.TRAPDOOR && (!var2.isAir() || var5 != BlockPathTypes.TRAPDOOR)
                    ? LongJumpToRandomPos.defaultAcceptableLandingSpot(param0x, param1)
                    : true;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean canAttack(Frog param0x) {
        return !BehaviorUtils.isBreeding(param0x);
    }

    public static void updateActivity(Frog param0) {
        param0.getBrain()
            .setActiveActivityToFirstValid(ImmutableList.of(Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM, Activity.IDLE));
    }

    public static Ingredient getTemptations() {
        return Frog.TEMPTATION_ITEM;
    }
}
