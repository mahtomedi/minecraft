package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.FlyingRandomStroll;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AllayAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 2.25F;
    private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 1.75F;
    private static final int CLOSE_ENOUGH_TO_TARGET = 4;
    private static final int TOO_FAR_FROM_TARGET = 16;
    private static final int MAX_LOOK_DISTANCE = 6;
    private static final int MIN_WAIT_DURATION = 30;
    private static final int MAX_WAIT_DURATION = 60;
    private static final int TIME_TO_FORGET_NOTEBLOCK = 600;
    private static final int DISTANCE_TO_WANDER_ITEM = 32;

    protected static Brain<?> makeBrain(Brain<Allay> param0) {
        initCoreActivity(param0);
        initIdleActivity(param0);
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.useDefaultActivity();
        return param0;
    }

    private static void initCoreActivity(Brain<Allay> param0) {
        param0.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)
            )
        );
    }

    private static void initIdleActivity(Brain<Allay> param0) {
        param0.addActivityWithConditions(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new GoToWantedItem<>(param0x -> true, 1.75F, true, 32)),
                Pair.of(1, new GoAndGiveItemsToTarget<>(AllayAi::getItemDepositPosition, 2.25F)),
                Pair.of(2, new StayCloseToTarget<>(AllayAi::getItemDepositPosition, 4, 16, 2.25F)),
                Pair.of(3, new RunSometimes<>(new SetEntityLookTarget(param0x -> true, 6.0F), UniformInt.of(30, 60))),
                Pair.of(
                    4,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(new FlyingRandomStroll(1.0F), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2), Pair.of(new DoNothing(30, 60), 1)
                        )
                    )
                )
            ),
            ImmutableSet.of()
        );
    }

    public static void updateActivity(Allay param0) {
        param0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    public static void hearNoteblock(LivingEntity param0, BlockPos param1) {
        Brain<?> var0 = param0.getBrain();
        GlobalPos var1 = GlobalPos.of(param0.getLevel().dimension(), param1);
        Optional<GlobalPos> var2 = var0.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if (var2.isEmpty()) {
            var0.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION, var1);
            var0.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        } else if (var2.get().equals(var1)) {
            var0.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        }

    }

    private static Optional<PositionTracker> getItemDepositPosition(LivingEntity param0x) {
        Brain<?> var0 = param0x.getBrain();
        Optional<GlobalPos> var1 = var0.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if (var1.isPresent()) {
            BlockPos var2 = var1.get().pos();
            if (shouldDepositItemsAtLikedNoteblock(param0x, var0, var2)) {
                return Optional.of(new BlockPosTracker(var2.above()));
            }

            var0.eraseMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        }

        return getLikedPlayerPositionTracker(param0x);
    }

    private static boolean shouldDepositItemsAtLikedNoteblock(LivingEntity param0, Brain<?> param1, BlockPos param2) {
        Optional<Integer> var0 = param1.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
        return param0.getLevel().getBlockState(param2).is(Blocks.NOTE_BLOCK) && var0.isPresent();
    }

    private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity param0) {
        return getLikedPlayer(param0).map(param0x -> new EntityTracker(param0x, true));
    }

    public static Optional<ServerPlayer> getLikedPlayer(LivingEntity param0) {
        Level var0 = param0.getLevel();
        if (!var0.isClientSide() && var0 instanceof ServerLevel var1) {
            Optional<UUID> var2 = param0.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if (var2.isPresent()) {
                Entity var3 = var1.getEntity(var2.get());
                return var3 instanceof ServerPlayer var4 ? Optional.of(var4) : Optional.empty();
            }
        }

        return Optional.empty();
    }
}
