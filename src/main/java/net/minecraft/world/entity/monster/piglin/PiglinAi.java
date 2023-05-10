package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final int PLAYER_ANGER_RANGE = 16;
    private static final int ANGER_DURATION = 600;
    private static final int ADMIRE_DURATION = 120;
    private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
    private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
    private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
    private static final int CELEBRATION_TIME = 300;
    protected static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
    private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
    private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
    private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final int EAT_COOLDOWN = 200;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
    private static final int INTERACTION_RANGE = 8;
    private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
    private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

    protected static Brain<?> makeBrain(Piglin param0, Brain<Piglin> param1) {
        initCoreActivity(param1);
        initIdleActivity(param1);
        initAdmireItemActivity(param1);
        initFightActivity(param0, param1);
        initCelebrateActivity(param1);
        initRetreatActivity(param1);
        initRideHoglinActivity(param1);
        param1.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param1.setDefaultActivity(Activity.IDLE);
        param1.useDefaultActivity();
        return param1;
    }

    protected static void initMemories(Piglin param0, RandomSource param1) {
        int var0 = TIME_BETWEEN_HUNTS.sample(param1);
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)var0);
    }

    private static void initCoreActivity(Brain<Piglin> param0) {
        param0.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                babyAvoidNemesis(),
                avoidZombified(),
                StopHoldingItemIfNoLongerAdmiring.create(),
                StartAdmiringItemIfSeen.create(120),
                StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
                StopBeingAngryIfTargetDead.create()
            )
        );
    }

    private static void initIdleActivity(Brain<Piglin> param0) {
        param0.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.<Piglin>create(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()),
                avoidRepellent(),
                babySometimesRideBabyHoglin(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                SetLookAndInteract.create(EntityType.PLAYER, 4)
            )
        );
    }

    private static void initFightActivity(Piglin param0, Brain<Piglin> param1) {
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                StopAttackingIfTargetInvalid.create(param1x -> !isNearestValidAttackTarget(param0, param1x)),
                BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75F)),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                MeleeAttack.create(20),
                new CrossbowAttack(),
                RememberIfHoglinWasKilled.create(),
                EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initCelebrateActivity(Brain<Piglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.CELEBRATE,
            10,
            ImmutableList.of(
                avoidRepellent(),
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.<Piglin>create(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                BehaviorBuilder.triggerIf(param0x -> !param0x.isDancing(), GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)),
                BehaviorBuilder.triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)),
                new RunOne<LivingEntity>(
                    ImmutableList.of(
                        Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1),
                        Pair.of(RandomStroll.stroll(0.6F, 2, 1), 1),
                        Pair.of(new DoNothing(10, 20), 1)
                    )
                )
            ),
            MemoryModuleType.CELEBRATE_LOCATION
        );
    }

    private static void initAdmireItemActivity(Brain<Piglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.ADMIRE_ITEM,
            10,
            ImmutableList.of(
                GoToWantedItem.create(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9),
                StopAdmiringIfItemTooFarAway.create(9),
                StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)
            ),
            MemoryModuleType.ADMIRING_ITEM
        );
    }

    private static void initRetreatActivity(Brain<Piglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                EraseMemoryIf.<PathfinderMob>create(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    private static void initRideHoglinActivity(Brain<Piglin> param0) {
        param0.addActivityAndRemoveMemoryWhenStopped(
            Activity.RIDE,
            10,
            ImmutableList.of(
                Mount.create(0.8F),
                SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0F),
                BehaviorBuilder.sequence(
                    BehaviorBuilder.triggerIf(Entity::isPassenger),
                    TriggerGate.triggerOneShuffled(
                        ImmutableList.<Pair<? extends Trigger<? super LivingEntity>, Integer>>builder()
                            .addAll(createLookBehaviors())
                            .add(Pair.of(BehaviorBuilder.triggerIf(param0x -> true), 1))
                            .build()
                    )
                ),
                DismountOrSkipMounting.<LivingEntity>create(8, PiglinAi::wantsToStopRiding)
            ),
            MemoryModuleType.RIDE_TARGET
        );
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of(
            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
            Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1),
            Pair.of(SetEntityLookTarget.create(8.0F), 1)
        );
    }

    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(
            ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder()
                .addAll(createLookBehaviors())
                .add(Pair.of(new DoNothing(30, 60), 1))
                .build()
        );
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<>(
            ImmutableList.of(
                Pair.of(RandomStroll.stroll(0.6F), 2),
                Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    private static BehaviorControl<PathfinderMob> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }

    private static BehaviorControl<Piglin> babyAvoidNemesis() {
        return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static BehaviorControl<Piglin> avoidZombified() {
        return CopyMemoryWithExpiry.create(
            PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION
        );
    }

    protected static void updateActivity(Piglin param0) {
        Brain<Piglin> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        var0.setActiveActivityToFirstValid(
            ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE)
        );
        Activity var2 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != var2) {
            getSoundForCurrentActivity(param0).ifPresent(param0::playSoundEvent);
        }

        param0.setAggressive(var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!var0.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(param0)) {
            param0.stopRiding();
        }

        if (!var0.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            var0.eraseMemory(MemoryModuleType.DANCING);
        }

        param0.setDancing(var0.hasMemoryValue(MemoryModuleType.DANCING));
    }

    private static boolean isBabyRidingBaby(Piglin param0) {
        if (!param0.isBaby()) {
            return false;
        } else {
            Entity var0 = param0.getVehicle();
            return var0 instanceof Piglin && ((Piglin)var0).isBaby() || var0 instanceof Hoglin && ((Hoglin)var0).isBaby();
        }
    }

    protected static void pickUpItem(Piglin param0, ItemEntity param1) {
        stopWalking(param0);
        ItemStack var0;
        if (param1.getItem().is(Items.GOLD_NUGGET)) {
            param0.take(param1, param1.getItem().getCount());
            var0 = param1.getItem();
            param1.discard();
        } else {
            param0.take(param1, 1);
            var0 = removeOneItemFromItemEntity(param1);
        }

        if (isLovedItem(var0)) {
            param0.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(param0, var0);
            admireGoldItem(param0);
        } else if (isFood(var0) && !hasEatenRecently(param0)) {
            eat(param0);
        } else {
            boolean var2 = !param0.equipItemIfPossible(var0).equals(ItemStack.EMPTY);
            if (!var2) {
                putInInventory(param0, var0);
            }
        }
    }

    private static void holdInOffhand(Piglin param0, ItemStack param1) {
        if (isHoldingItemInOffHand(param0)) {
            param0.spawnAtLocation(param0.getItemInHand(InteractionHand.OFF_HAND));
        }

        param0.holdInOffHand(param1);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        ItemStack var1 = var0.split(1);
        if (var0.isEmpty()) {
            param0.discard();
        } else {
            param0.setItem(var0);
        }

        return var1;
    }

    protected static void stopHoldingOffHandItem(Piglin param0, boolean param1) {
        ItemStack var0 = param0.getItemInHand(InteractionHand.OFF_HAND);
        param0.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (param0.isAdult()) {
            boolean var1 = isBarterCurrency(var0);
            if (param1 && var1) {
                throwItems(param0, getBarterResponseItems(param0));
            } else if (!var1) {
                boolean var2 = !param0.equipItemIfPossible(var0).isEmpty();
                if (!var2) {
                    putInInventory(param0, var0);
                }
            }
        } else {
            boolean var3 = !param0.equipItemIfPossible(var0).isEmpty();
            if (!var3) {
                ItemStack var4 = param0.getMainHandItem();
                if (isLovedItem(var4)) {
                    putInInventory(param0, var4);
                } else {
                    throwItems(param0, Collections.singletonList(var4));
                }

                param0.holdInMainHand(var0);
            }
        }

    }

    protected static void cancelAdmiring(Piglin param0) {
        if (isAdmiringItem(param0) && !param0.getOffhandItem().isEmpty()) {
            param0.spawnAtLocation(param0.getOffhandItem());
            param0.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }

    }

    private static void putInInventory(Piglin param0, ItemStack param1) {
        ItemStack var0 = param0.addToInventory(param1);
        throwItemsTowardRandomPos(param0, Collections.singletonList(var0));
    }

    private static void throwItems(Piglin param0, List<ItemStack> param1) {
        Optional<Player> var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (var0.isPresent()) {
            throwItemsTowardPlayer(param0, var0.get(), param1);
        } else {
            throwItemsTowardRandomPos(param0, param1);
        }

    }

    private static void throwItemsTowardRandomPos(Piglin param0, List<ItemStack> param1) {
        throwItemsTowardPos(param0, param1, getRandomNearbyPos(param0));
    }

    private static void throwItemsTowardPlayer(Piglin param0, Player param1, List<ItemStack> param2) {
        throwItemsTowardPos(param0, param2, param1.position());
    }

    private static void throwItemsTowardPos(Piglin param0, List<ItemStack> param1, Vec3 param2) {
        if (!param1.isEmpty()) {
            param0.swing(InteractionHand.OFF_HAND);

            for(ItemStack var0 : param1) {
                BehaviorUtils.throwItem(param0, var0, param2.add(0.0, 1.0, 0.0));
            }
        }

    }

    private static List<ItemStack> getBarterResponseItems(Piglin param0) {
        LootTable var0 = param0.level().getServer().getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> var1 = var0.getRandomItems(
            new LootParams.Builder((ServerLevel)param0.level()).withParameter(LootContextParams.THIS_ENTITY, param0).create(LootContextParamSets.PIGLIN_BARTER)
        );
        return var1;
    }

    private static boolean wantsToDance(LivingEntity param0x, LivingEntity param1) {
        if (param1.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return RandomSource.create(param0x.level().getGameTime()).nextFloat() < 0.1F;
        }
    }

    protected static boolean wantsToPickup(Piglin param0, ItemStack param1) {
        if (param0.isBaby() && param1.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        } else if (param1.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        } else if (isAdmiringDisabled(param0) && param0.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (isBarterCurrency(param1)) {
            return isNotHoldingLovedItemInOffHand(param0);
        } else {
            boolean var0 = param0.canAddToInventory(param1);
            if (param1.is(Items.GOLD_NUGGET)) {
                return var0;
            } else if (isFood(param1)) {
                return !hasEatenRecently(param0) && var0;
            } else if (!isLovedItem(param1)) {
                return param0.canReplaceCurrentItem(param1);
            } else {
                return isNotHoldingLovedItemInOffHand(param0) && var0;
            }
        }
    }

    protected static boolean isLovedItem(ItemStack param0) {
        return param0.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(Piglin param0x, Entity param1) {
        if (!(param1 instanceof Mob)) {
            return false;
        } else {
            Mob var0 = (Mob)param1;
            return !var0.isBaby()
                || !var0.isAlive()
                || wasHurtRecently(param0x)
                || wasHurtRecently(var0)
                || var0 instanceof Piglin && var0.getVehicle() == null;
        }
    }

    private static boolean isNearestValidAttackTarget(Piglin param0, LivingEntity param1) {
        return findNearestValidAttackTarget(param0).filter(param1x -> param1x == param1).isPresent();
    }

    private static boolean isNearZombified(Piglin param0x) {
        Brain<Piglin> var0 = param0x.getBrain();
        if (var0.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity var1 = var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return param0x.closerThan(var1, 6.0);
        } else {
            return false;
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin param0x) {
        Brain<Piglin> var0 = param0x.getBrain();
        if (isNearZombified(param0x)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> var1 = BehaviorUtils.getLivingEntityFromUUIDMemory(param0x, MemoryModuleType.ANGRY_AT);
            if (var1.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(param0x, var1.get())) {
                return var1;
            } else {
                if (var0.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    Optional<Player> var2 = var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                    if (var2.isPresent()) {
                        return var2;
                    }
                }

                Optional<Mob> var3 = var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
                if (var3.isPresent()) {
                    return var3;
                } else {
                    Optional<Player> var4 = var0.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                    return var4.isPresent() && Sensor.isEntityAttackable(param0x, var4.get()) ? var4 : Optional.empty();
                }
            }
        }
    }

    public static void angerNearbyPiglins(Player param0, boolean param1) {
        List<Piglin> var0 = param0.level().getEntitiesOfClass(Piglin.class, param0.getBoundingBox().inflate(16.0));
        var0.stream().filter(PiglinAi::isIdle).filter(param2 -> !param1 || BehaviorUtils.canSee(param2, param0)).forEach(param1x -> {
            if (param1x.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                setAngerTargetToNearestTargetablePlayerIfFound(param1x, param0);
            } else {
                setAngerTarget(param1x, param0);
            }

        });
    }

    public static InteractionResult mobInteract(Piglin param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (canAdmire(param0, var0)) {
            ItemStack var1 = var0.split(1);
            holdInOffhand(param0, var1);
            admireGoldItem(param0);
            stopWalking(param0);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    protected static boolean canAdmire(Piglin param0, ItemStack param1) {
        return !isAdmiringDisabled(param0) && !isAdmiringItem(param0) && param0.isAdult() && isBarterCurrency(param1);
    }

    protected static void wasHurtBy(Piglin param0, LivingEntity param1) {
        if (!(param1 instanceof Piglin)) {
            if (isHoldingItemInOffHand(param0)) {
                stopHoldingOffHandItem(param0, false);
            }

            Brain<Piglin> var0 = param0.getBrain();
            var0.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            var0.eraseMemory(MemoryModuleType.DANCING);
            var0.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            if (param1 instanceof Player) {
                var0.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
            }

            getAvoidTarget(param0).ifPresent(param2 -> {
                if (param2.getType() != param1.getType()) {
                    var0.eraseMemory(MemoryModuleType.AVOID_TARGET);
                }

            });
            if (param0.isBaby()) {
                var0.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, 100L);
                if (Sensor.isEntityAttackableIgnoringLineOfSight(param0, param1)) {
                    broadcastAngerTarget(param0, param1);
                }

            } else if (param1.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(param0)) {
                setAvoidTargetAndDontHuntForAWhile(param0, param1);
                broadcastRetreat(param0, param1);
            } else {
                maybeRetaliate(param0, param1);
            }
        }
    }

    protected static void maybeRetaliate(AbstractPiglin param0, LivingEntity param1) {
        if (!param0.getBrain().isActive(Activity.AVOID)) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(param0, param1)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(param0, param1, 4.0)) {
                    if (param1.getType() == EntityType.PLAYER && param0.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                        setAngerTargetToNearestTargetablePlayerIfFound(param0, param1);
                        broadcastUniversalAnger(param0);
                    } else {
                        setAngerTarget(param0, param1);
                        broadcastAngerTarget(param0, param1);
                    }

                }
            }
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin param0) {
        return param0.getBrain().getActiveNonCoreActivity().map(param1 -> getSoundForActivity(param0, param1));
    }

    private static SoundEvent getSoundForActivity(Piglin param0, Activity param1) {
        if (param1 == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        } else if (param0.isConverting()) {
            return SoundEvents.PIGLIN_RETREAT;
        } else if (param1 == Activity.AVOID && isNearAvoidTarget(param0)) {
            return SoundEvents.PIGLIN_RETREAT;
        } else if (param1 == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        } else if (param1 == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        } else if (seesPlayerHoldingLovedItem(param0)) {
            return SoundEvents.PIGLIN_JEALOUS;
        } else {
            return isNearRepellent(param0) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
        }
    }

    private static boolean isNearAvoidTarget(Piglin param0) {
        Brain<Piglin> var0 = param0.getBrain();
        return !var0.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : var0.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(param0, 12.0);
    }

    protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    public static boolean isWearingGold(LivingEntity param0) {
        for(ItemStack var1 : param0.getArmorSlots()) {
            Item var2 = var1.getItem();
            if (var2 instanceof ArmorItem && ((ArmorItem)var2).getMaterial() == ArmorMaterials.GOLD) {
                return true;
            }
        }

        return false;
    }

    private static void stopWalking(Piglin param0) {
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param0.getNavigation().stop();
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker var0 = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create(
            param1 -> param1.isBaby() && var0.tickDownAndCheck(param1.level().random),
            MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
            MemoryModuleType.RIDE_TARGET,
            RIDE_DURATION
        );
    }

    protected static void broadcastAngerTarget(AbstractPiglin param0, LivingEntity param1) {
        getAdultPiglins(param0).forEach(param1x -> {
            if (param1.getType() != EntityType.HOGLIN || param1x.canHunt() && ((Hoglin)param1).canBeHunted()) {
                setAngerTargetIfCloserThanCurrent(param1x, param1);
            }
        });
    }

    protected static void broadcastUniversalAnger(AbstractPiglin param0) {
        getAdultPiglins(param0).forEach(param0x -> getNearestVisibleTargetablePlayer(param0x).ifPresent(param1 -> setAngerTarget(param0x, param1)));
    }

    protected static void setAngerTarget(AbstractPiglin param0, LivingEntity param1) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(param0, param1)) {
            param0.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, param1.getUUID(), 600L);
            if (param1.getType() == EntityType.HOGLIN && param0.canHunt()) {
                dontKillAnyMoreHoglinsForAWhile(param0);
            }

            if (param1.getType() == EntityType.PLAYER && param0.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                param0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
            }

        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(AbstractPiglin param0, LivingEntity param1) {
        Optional<Player> var0 = getNearestVisibleTargetablePlayer(param0);
        if (var0.isPresent()) {
            setAngerTarget(param0, var0.get());
        } else {
            setAngerTarget(param0, param1);
        }

    }

    private static void setAngerTargetIfCloserThanCurrent(AbstractPiglin param0, LivingEntity param1) {
        Optional<LivingEntity> var0 = getAngerTarget(param0);
        LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0, param1);
        if (!var0.isPresent() || var0.get() != var1) {
            setAngerTarget(param0, var1);
        }
    }

    private static Optional<LivingEntity> getAngerTarget(AbstractPiglin param0) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(param0, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? param0.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
            ? param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
            : Optional.empty();
    }

    private static void broadcastRetreat(Piglin param0, LivingEntity param1) {
        getVisibleAdultPiglins(param0)
            .stream()
            .filter(param0x -> param0x instanceof Piglin)
            .forEach(param1x -> retreatFromNearestTarget((Piglin)param1x, param1));
    }

    private static void retreatFromNearestTarget(Piglin param0, LivingEntity param1) {
        Brain<Piglin> var0 = param0.getBrain();
        LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0.getMemory(MemoryModuleType.AVOID_TARGET), param1);
        var1 = BehaviorUtils.getNearestTarget(param0, var0.getMemory(MemoryModuleType.ATTACK_TARGET), var1);
        setAvoidTargetAndDontHuntForAWhile(param0, var1);
    }

    private static boolean wantsToStopFleeing(Piglin param0x) {
        Brain<Piglin> var0 = param0x.getBrain();
        if (!var0.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity var1 = var0.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> var2 = var1.getType();
            if (var2 == EntityType.HOGLIN) {
                return piglinsEqualOrOutnumberHoglins(param0x);
            } else if (isZombified(var2)) {
                return !var0.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, var1);
            } else {
                return false;
            }
        }
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin param0) {
        return !hoglinsOutnumberPiglins(param0);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin param0) {
        int var0 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int var1 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return var1 > var0;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin param0, LivingEntity param1) {
        param0.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        param0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, (long)RETREAT_DURATION.sample(param0.level().random));
        dontKillAnyMoreHoglinsForAWhile(param0);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.sample(param0.level().random));
    }

    private static void eat(Piglin param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin param0) {
        Vec3 var0 = LandRandomPos.getPos(param0, 4, 2);
        return var0 == null ? param0.position() : var0;
    }

    private static boolean hasEatenRecently(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(AbstractPiglin param0x) {
        return param0x.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity param0x) {
        return param0x.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean isAdmiringItem(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(ItemStack param0) {
        return param0.is(BARTERING_ITEM);
    }

    private static boolean isFood(ItemStack param0) {
        return param0.is(ItemTags.PIGLIN_FOOD);
    }

    private static boolean isNearRepellent(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity param0) {
        return !seesPlayerHoldingLovedItem(param0);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity param0x) {
        return param0x.getType() == EntityType.PLAYER && param0x.isHolding(PiglinAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(Piglin param0) {
        return !param0.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin param0x) {
        return param0x.getOffhandItem().isEmpty() || !isLovedItem(param0x.getOffhandItem());
    }

    public static boolean isZombified(EntityType<?> param0) {
        return param0 == EntityType.ZOMBIFIED_PIGLIN || param0 == EntityType.ZOGLIN;
    }
}
