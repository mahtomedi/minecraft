package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBruteAi {
    protected static Brain<?> makeBrain(PiglinBrute param0, Brain<PiglinBrute> param1) {
        initCoreActivity(param0, param1);
        initIdleActivity(param0, param1);
        initFightActivity(param0, param1);
        param1.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param1.setDefaultActivity(Activity.IDLE);
        param1.useDefaultActivity();
        return param1;
    }

    protected static void initMemories(PiglinBrute param0) {
        GlobalPos var0 = GlobalPos.of(param0.level.dimension(), param0.blockPosition());
        param0.getBrain().setMemory(MemoryModuleType.HOME, var0);
    }

    private static void initCoreActivity(PiglinBrute param0, Brain<PiglinBrute> param1) {
        param1.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200), new InteractWithDoor(), new StopBeingAngryIfTargetDead<>())
        );
    }

    private static void initIdleActivity(PiglinBrute param0, Brain<PiglinBrute> param1) {
        param1.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                new StartAttacking<>(PiglinBruteAi::findNearestValidAttackTarget),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                new SetLookAndInteract(EntityType.PLAYER, 4)
            )
        );
    }

    private static void initFightActivity(PiglinBrute param0, Brain<PiglinBrute> param1) {
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                new StopAttackingIfTargetInvalid<>(param1x -> !isNearestValidAttackTarget(param0, param1x)),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
                new MeleeAttack(20)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static RunOne<PiglinBrute> createIdleLookBehaviors() {
        return new RunOne<>(
            ImmutableList.of(
                Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1),
                Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1),
                Pair.of(new SetEntityLookTarget(EntityType.PIGLIN_BRUTE, 8.0F), 1),
                Pair.of(new SetEntityLookTarget(8.0F), 1),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
        return new RunOne<>(
            ImmutableList.of(
                Pair.of(new RandomStroll(0.6F), 2),
                Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                Pair.of(new StrollToPoi(MemoryModuleType.HOME, 0.6F, 2, 100), 2),
                Pair.of(new StrollAroundPoi(MemoryModuleType.HOME, 0.6F, 5), 2),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    protected static void updateActivity(PiglinBrute param0) {
        Brain<PiglinBrute> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        var0.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        Activity var2 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != var2) {
            playActivitySound(param0);
        }

        param0.setAggressive(var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    private static boolean isNearestValidAttackTarget(AbstractPiglin param0, LivingEntity param1) {
        return findNearestValidAttackTarget(param0).filter(param1x -> param1x == param1).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin param0x) {
        Optional<LivingEntity> var0 = BehaviorUtils.getLivingEntityFromUUIDMemory(param0x, MemoryModuleType.ANGRY_AT);
        if (var0.isPresent() && isAttackAllowed(var0.get())) {
            return var0;
        } else {
            Optional<? extends LivingEntity> var1 = getTargetIfWithinRange(param0x, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
            return var1.isPresent() ? var1 : param0x.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        }
    }

    private static boolean isAttackAllowed(LivingEntity param0) {
        return EntitySelector.ATTACK_ALLOWED.test(param0);
    }

    private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin param0, MemoryModuleType<? extends LivingEntity> param1) {
        return param0.getBrain().getMemory(param1).filter(param1x -> param1x.closerThan(param0, 12.0));
    }

    protected static void wasHurtBy(PiglinBrute param0, LivingEntity param1) {
        if (!(param1 instanceof AbstractPiglin)) {
            PiglinAi.maybeRetaliate(param0, param1);
        }
    }

    protected static void maybePlayActivitySound(PiglinBrute param0) {
        if ((double)param0.level.random.nextFloat() < 0.0125) {
            playActivitySound(param0);
        }

    }

    private static void playActivitySound(PiglinBrute param0) {
        param0.getBrain().getActiveNonCoreActivity().ifPresent(param1 -> {
            if (param1 == Activity.FIGHT) {
                param0.playAngrySound();
            }

        });
    }
}
