package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.CountDownTemptationTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RandomSwim;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;

public class AxolotlAi {
    private static final IntRange ADULT_FOLLOW_RANGE = IntRange.of(5, 16);

    protected static Brain<?> makeBrain(Brain<Axolotl> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        initFightActivity(param0);
        initPlayDeadActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initPlayDeadActivity(Brain<Axolotl> param0) {
        param0.addActivityAndRemoveMemoriesWhenStopped(
            Activity.PLAY_DEAD,
            ImmutableList.of(Pair.of(0, new PlayDead()), Pair.of(1, new EraseMemoryIf<>(AxolotlAi::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))),
            ImmutableSet.of(Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT)),
            ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS)
        );
    }

    private static void initFightActivity(Brain<Axolotl> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            0,
            ImmutableList.of(
                new StopAttackingIfTargetInvalid<>(),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(AxolotlAi::getSpeedModifierChasing),
                new MeleeAttack(20),
                new EraseMemoryIf(AxolotlAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initCoreActivity(Brain<Axolotl> param0) {
        param0.addActivity(
            Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new ValidatePlayDead(), new CountDownTemptationTicks())
        );
    }

    private static void initIdleActivity(Brain<Axolotl> param0) {
        param0.addActivity(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), IntRange.of(30, 60))),
                Pair.of(
                    1,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(new AnimalMakeLove(EntityType.AXOLOTL, 0.2F), 1),
                            Pair.of(new FollowTemptation(AxolotlAi::getSpeedModifier), 1),
                            Pair.of(new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult), 1)
                        )
                    )
                ),
                Pair.of(2, new StartAttacking<>(AxolotlAi::findNearestValidAttackTarget)),
                Pair.of(2, new TryFindWater(6, 0.15F)),
                Pair.of(
                    3,
                    new GateBehavior<>(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        ImmutableSet.of(),
                        GateBehavior.OrderPolicy.ORDERED,
                        GateBehavior.RunningPolicy.TRY_ALL,
                        ImmutableList.of(
                            Pair.of(new RandomSwim(0.5F), 2),
                            Pair.of(new RandomStroll(0.15F), 2),
                            Pair.of(new SetWalkTargetFromLookTarget(AxolotlAi::getSpeedModifier, 3), 3),
                            Pair.of(new RunIf<>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5),
                            Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(200, 400)), 5)
                        )
                    )
                )
            )
        );
    }

    public static void updateActivity(Axolotl param0) {
        Brain<Axolotl> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != Activity.PLAY_DEAD) {
            var0.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, Activity.IDLE));
        }

    }

    private static float getSpeedModifierChasing(LivingEntity param0x) {
        return param0x.isInWaterOrBubble() ? 0.6F : 0.15F;
    }

    private static float getSpeedModifierFollowingAdult(LivingEntity param0x) {
        return param0x.isInWaterOrBubble() ? 0.6F : 0.15F;
    }

    private static float getSpeedModifier(LivingEntity param0x) {
        return param0x.isInWaterOrBubble() ? 0.5F : 0.15F;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Axolotl param0x) {
        return isBreeding(param0x) ? Optional.empty() : param0x.getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE);
    }

    private static boolean isBreeding(Axolotl param0x) {
        return param0x.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(ItemTags.AXOLOTL_TEMPT_ITEMS);
    }
}
