package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AdmireHeldItem;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToCelebrateLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StartHuntingHoglin;
import net.minecraft.world.entity.ai.behavior.StopAdmiringIfItemTooFarAway;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
    private static final IntRange TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final IntRange RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final IntRange RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final Set FOOD_ITEMS = ImmutableSet.of(Items.PORKCHOP, Items.COOKED_PORKCHOP);
    private static final Set<Item> LOVED_ITEMS_IN_ADDITION_TO_GOLD_TIER_AND_GOLD_MATERIAL = ImmutableSet.of(
        Items.GOLD_INGOT,
        Items.GOLDEN_APPLE,
        Items.GOLDEN_HORSE_ARMOR,
        Items.GOLDEN_CARROT,
        Items.GOLD_BLOCK,
        Items.GOLD_ORE,
        Items.ENCHANTED_GOLDEN_APPLE,
        Items.GOLDEN_HORSE_ARMOR,
        Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Items.BELL,
        Items.GLISTERING_MELON_SLICE,
        Items.CLOCK
    );

    protected static Brain<?> makeBrain(Piglin param0, Dynamic<?> param1) {
        Brain<Piglin> var0 = new Brain<>(Piglin.MEMORY_TYPES, Piglin.SENSOR_TYPES, param1);
        initCoreActivity(param0, var0);
        initIdleActivity(param0, var0);
        initAdmireItemActivity(param0, var0);
        initFightActivity(param0, var0);
        initCelebrateActivity(param0, var0);
        initRetreatActivity(param0, var0);
        initRidePiglinActivity(param0, var0);
        var0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        var0.setDefaultActivity(Activity.IDLE);
        var0.useDefaultActivity();
        initMemories(param0.level, var0);
        return var0;
    }

    private static void initMemories(Level param0, Brain<Piglin> param1) {
        int var0 = TIME_BETWEEN_HUNTS.randomValue(param0.random);
        param1.setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, param0.getGameTime(), (long)var0);
    }

    private static void initCoreActivity(Piglin param0, Brain<Piglin> param1) {
        param1.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(200),
                new InteractWithDoor(),
                new StopHoldingItemIfNoLongerAdmiring<>(),
                new StartAdmiringItemIfSeen(120),
                new StartCelebratingIfTargetDead(300),
                new StopBeingAngryIfTargetDead()
            )
        );
    }

    private static void initIdleActivity(Piglin param0, Brain<Piglin> param1) {
        float var0 = param0.getMovementSpeed();
        param1.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                new StartAttacking<>(Piglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                new StartHuntingHoglin(),
                avoidZombifiedPiglin(var0),
                avoidSoulFire(var0),
                babySometimesRideBabyHoglin(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(var0),
                new SetLookAndInteract(EntityType.PLAYER, 4)
            )
        );
    }

    private static void initFightActivity(Piglin param0, Brain<Piglin> param1) {
        float var0 = param0.getMovementSpeed();
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                new StopAttackingIfTargetInvalid<>(param1x -> !isNearestValidAttackTarget(param0, param1x)),
                new RunIf(PiglinAi::hasCrossbow, new BackUpIfTooClose<>(5, 0.75F)),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(var0 * 1.2F),
                new MeleeAttack(1.5, 20),
                new CrossbowAttack(),
                new RememberIfHoglinWasKilled()
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initCelebrateActivity(Piglin param0, Brain<Piglin> param1) {
        float var0 = param0.getMovementSpeed();
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.CELEBRATE,
            10,
            ImmutableList.of(
                avoidZombifiedPiglin(var0),
                avoidSoulFire(var0),
                new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                new StartAttacking<Piglin>(Piglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                new GoToCelebrateLocation(2),
                new RunOne(
                    ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1),
                        Pair.of(new RandomStroll(var0, 2, 1), 1),
                        Pair.of(new DoNothing(10, 20), 1)
                    )
                )
            ),
            MemoryModuleType.CELEBRATE_LOCATION
        );
    }

    private static void initAdmireItemActivity(Piglin param0, Brain<Piglin> param1) {
        float var0 = param0.getMovementSpeed();
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.ADMIRE_ITEM,
            10,
            ImmutableList.of(
                new GoToWantedItem<>(PiglinAi::isNotHoldingLovedItemInOffHand, 9, true), new AdmireHeldItem(var0 * 0.5F), new StopAdmiringIfItemTooFarAway(9)
            ),
            MemoryModuleType.ADMIRING_ITEM
        );
    }

    private static void initRetreatActivity(Piglin param0, Brain<Piglin> param1) {
        float var0 = param0.getMovementSpeed() * 1.3F;
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, var0, 6, false),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(param0.getMovementSpeed()),
                new EraseMemoryIf<Piglin>(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    private static void initRidePiglinActivity(Piglin param0, Brain<Piglin> param1) {
        param1.addActivityAndRemoveMemoryWhenStopped(
            Activity.RIDE,
            10,
            ImmutableList.of(
                new Mount<>(),
                new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 8.0F),
                new RunIf(Piglin::isRiding, createIdleLookBehaviors()),
                new DismountOrSkipMounting(8, PiglinAi::wantsToStopRiding)
            ),
            MemoryModuleType.RIDE_TARGET
        );
    }

    private static RunOne<Piglin> createIdleLookBehaviors() {
        return new RunOne<>(
            ImmutableList.of(
                Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1),
                Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1),
                Pair.of(new SetEntityLookTarget(8.0F), 1),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    private static RunOne<Piglin> createIdleMovementBehaviors(float param0) {
        return new RunOne<>(
            ImmutableList.of(
                Pair.of(new RandomStroll(param0), 2),
                Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, param0, 2), 2),
                Pair.of(new RunIf<>(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, new SetWalkTargetFromLookTarget(param0, 3)), 2),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    private static SetWalkTargetAwayFrom<BlockPos> avoidSoulFire(float param0) {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_SOUL_FIRE, param0 * 1.5F, 8, false);
    }

    private static SetWalkTargetAwayFrom<?> avoidZombifiedPiglin(float param0) {
        return SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN, param0 * 1.5F, 10, false);
    }

    protected static void updateActivity(Piglin param0) {
        Brain<Piglin> var0 = param0.getBrain();
        Activity var1 = var0.getActiveNonCoreActivity().orElse(null);
        var0.setActiveActivityToFirstValid(
            ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE)
        );
        Activity var2 = var0.getActiveNonCoreActivity().orElse(null);
        if (var1 != var2) {
            playActivitySound(param0);
        }

        param0.setAggressive(var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!var0.hasMemoryValue(MemoryModuleType.RIDE_TARGET)) {
            param0.stopRiding();
        }

        if (param0.isRiding() && seesPlayerHoldingWantedItem(param0)) {
            param0.stopRiding();
            param0.getBrain().eraseMemory(MemoryModuleType.RIDE_TARGET);
        }

    }

    protected static void pickUpItem(Piglin param0, ItemEntity param1) {
        stopWalking(param0);
        param0.take(param1, 1);
        ItemStack var0 = removeOneItemFromItemEntity(param1);
        Item var1 = var0.getItem();
        if (isLovedItem(var1)) {
            if (!param0.getOffhandItem().isEmpty()) {
                param0.spawnAtLocation(param0.getItemInHand(InteractionHand.OFF_HAND));
            }

            param0.holdInOffHand(var0);
            admireGoldItem(param0);
        } else if (isFood(var1) && !hasEatenRecently(param0)) {
            eat(param0);
        } else {
            boolean var2 = param0.equipItemIfPossible(var0);
            if (!var2) {
                putInInventory(param0, var0);
            }
        }
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        ItemStack var1 = var0.split(1);
        if (var0.isEmpty()) {
            param0.remove();
        } else {
            param0.setItem(var0);
        }

        return var1;
    }

    public static void stopHoldingOffHandItem(Piglin param0, boolean param1) {
        ItemStack var0 = param0.getItemInHand(InteractionHand.OFF_HAND);
        param0.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (param0.isAdult()) {
            if (param1 && isBarterCurrency(var0.getItem())) {
                throwItem(param0, getBarterResponseItem(param0));
            } else {
                boolean var1 = param0.equipItemIfPossible(var0);
                if (!var1) {
                    putInInventory(param0, var0);
                }
            }
        } else {
            boolean var2 = param0.equipItemIfPossible(var0);
            if (!var2) {
                ItemStack var3 = param0.getMainHandItem();
                if (isLovedItem(var3.getItem())) {
                    putInInventory(param0, var3);
                } else {
                    throwItem(param0, var3);
                }

                param0.holdInMainHand(var0);
            }
        }

    }

    private static void putInInventory(Piglin param0, ItemStack param1) {
        ItemStack var0 = param0.addToInventory(param1);
        throwItemTowardRandomPos(param0, var0);
    }

    private static void throwItem(Piglin param0, ItemStack param1) {
        Optional<Player> var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (var0.isPresent()) {
            throwItemTowardPlayer(param0, var0.get(), param1);
        } else {
            throwItemTowardRandomPos(param0, param1);
        }

    }

    private static void throwItemTowardRandomPos(Piglin param0, ItemStack param1) {
        throwItemTowardPos(param0, param1, getRandomNearbyPos(param0));
    }

    private static void throwItemTowardPlayer(Piglin param0, Player param1, ItemStack param2) {
        throwItemTowardPos(param0, param2, param1.position());
    }

    private static void throwItemTowardPos(Piglin param0, ItemStack param1, Vec3 param2) {
        if (!param1.isEmpty()) {
            param0.swing(InteractionHand.OFF_HAND);
            BehaviorUtils.throwItem(param0, param1, param2.add(0.0, 1.0, 0.0));
        }

    }

    private static ItemStack getBarterResponseItem(Piglin param0) {
        LootTable var0 = param0.level.getServer().getLootTables().get(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> var1 = var0.getRandomItems(
            new LootContext.Builder((ServerLevel)param0.level)
                .withParameter(LootContextParams.THIS_ENTITY, param0)
                .withRandom(param0.level.random)
                .create(LootContextParamSets.PIGLIN_BARTER)
        );
        return var1.isEmpty() ? ItemStack.EMPTY : var1.get(0);
    }

    protected static boolean wantsToPickup(Piglin param0, ItemStack param1) {
        Item var0 = param1.getItem();
        if (var0 == Items.GOLD_NUGGET) {
            return true;
        } else if (wasHitByPlayer(param0) && param0.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (isFood(var0)) {
            return !hasEatenRecently(param0);
        } else {
            return isLovedItem(var0) ? isNotHoldingLovedItemInOffHand(param0) : param0.canReplaceCurrentItem(param1);
        }
    }

    public static boolean isLovedItem(Item param0) {
        return LOVED_ITEMS_IN_ADDITION_TO_GOLD_TIER_AND_GOLD_MATERIAL.contains(param0)
            || param0 instanceof TieredItem && ((TieredItem)param0).getTier() == Tiers.GOLD
            || param0 instanceof ArmorItem && ((ArmorItem)param0).getMaterial() == ArmorMaterials.GOLD;
    }

    private static boolean wantsToStopRiding(Piglin param0x, Entity param1x) {
        if (!(param1x instanceof Mob)) {
            return false;
        } else {
            Mob var0 = (Mob)param1x;
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

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin param0x) {
        Brain<Piglin> var0x = param0x.getBrain();
        Optional<LivingEntity> var1 = BehaviorUtils.getLivingEntityFromUUIDMemory(param0x, MemoryModuleType.ANGRY_AT);
        if (var1.isPresent() && isAttackAllowed(var1.get())) {
            return var1;
        } else {
            Optional<WitherSkeleton> var2 = var0x.getMemory(MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON);
            if (var2.isPresent()) {
                return var2;
            } else {
                Optional<Player> var3 = var0x.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                return var3.isPresent() && isAttackAllowed(var3.get()) ? var3 : Optional.empty();
            }
        }
    }

    public static void angerNearbyPiglinsThatSee(Player param0) {
        if (isAttackAllowed(param0)) {
            List<Piglin> var0 = param0.level.getEntitiesOfClass(Piglin.class, param0.getBoundingBox().inflate(16.0));
            var0.stream().filter(PiglinAi::isIdle).filter(param1 -> BehaviorUtils.canSee(param1, param0)).forEach(param1 -> setAngerTarget(param1, param0));
        }
    }

    public static boolean mobInteract(Piglin param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        Item var1 = var0.getItem();
        if (!isAdmiringItem(param0) && param0.isAdult() && isBarterCurrency(var1) && !wasHitByPlayer(param0)) {
            var0.shrink(1);
            param0.holdInOffHand(new ItemStack(var1, 1));
            admireGoldItem(param0);
            return true;
        } else {
            return false;
        }
    }

    protected static void wasHurtBy(Piglin param0, LivingEntity param1) {
        if (!(param1 instanceof Piglin)) {
            if (!param0.getOffhandItem().isEmpty()) {
                stopHoldingOffHandItem(param0, false);
            }

            Brain<Piglin> var0 = param0.getBrain();
            var0.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            var0.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            if (param1 instanceof Player) {
                var0.setMemoryWithExpiry(MemoryModuleType.WAS_HIT_BY_PLAYER, (Player)param1, param0.level.getGameTime(), 400L);
            }

            if (param0.isBaby()) {
                var0.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, param0.level.getGameTime(), 100L);
            } else if (param1.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(param0)) {
                setAvoidTargetAndDontHuntForAWhile(param0, param1);
                broadcastRetreat(param0, param1);
            } else {
                maybeRetaliate(param0, param1);
            }
        }
    }

    private static void maybeRetaliate(Piglin param0, LivingEntity param1) {
        if (!param0.getBrain().isActive(Activity.AVOID) || param1.getType() != EntityType.HOGLIN) {
            if (isAttackAllowed(param1)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(param0, param1, 4.0)) {
                    setAngerTarget(param0, param1);
                    broadcastAngerTarget(param0, param1);
                }
            }
        }
    }

    private static void playActivitySound(Piglin param0) {
        param0.getBrain().getActiveNonCoreActivity().ifPresent(param1 -> {
            if (param1 == Activity.FIGHT) {
                param0.playAngrySound();
            } else if (param1 == Activity.AVOID || param0.isConverting()) {
                param0.playRetreatSound();
            } else if (param1 == Activity.ADMIRE_ITEM) {
                param0.playAdmiringSound();
            } else if (param1 == Activity.CELEBRATE) {
                param0.playCelebrateSound();
            } else if (seesPlayerHoldingLovedItem(param0)) {
                param0.playJealousSound();
            } else if (seesZombifiedPiglin(param0) || seesSoulFire(param0)) {
                param0.playRetreatSound();
            }

        });
    }

    protected static void maybePlayActivitySound(Piglin param0) {
        if ((double)param0.level.random.nextFloat() < 0.0125) {
            playActivitySound(param0);
        }

    }

    public static boolean hasAnyoneNearbyHuntedRecently(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY)
            || getVisibleAdultPiglins(param0).stream().anyMatch(param0x -> param0x.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
    }

    private static List<Piglin> getVisibleAdultPiglins(Piglin param0) {
        return (List<Piglin>)(param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)
            ? param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).get()
            : Lists.newArrayList());
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

    private static RunSometimes<Piglin> babySometimesRideBabyHoglin() {
        return new RunSometimes<>(
            new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION),
            RIDE_START_INTERVAL
        );
    }

    public static void broadcastAngerTarget(Piglin param0, LivingEntity param1) {
        getVisibleAdultPiglins(param0).forEach(param1x -> setAngerTargetIfCloserThanCurrent(param1x, param1));
    }

    public static void broadcastDontKillAnyMoreHoglinsForAWhile(Piglin param0) {
        getVisibleAdultPiglins(param0).forEach(param0x -> dontKillAnyMoreHoglinsForAWhile(param0x));
    }

    public static void setAngerTarget(Piglin param0, LivingEntity param1) {
        param0.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, new SerializableUUID(param1.getUUID()), param0.level.getGameTime(), 600L);
        if (param1.getType() == EntityType.HOGLIN) {
            dontKillAnyMoreHoglinsForAWhile(param0);
        }

    }

    private static void setAngerTargetIfCloserThanCurrent(Piglin param0, LivingEntity param1) {
        Optional<LivingEntity> var0 = getAngerTarget(param0);
        LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0, param1);
        setAngerTarget(param0, var1);
    }

    private static Optional<LivingEntity> getAngerTarget(Piglin param0) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(param0, MemoryModuleType.ANGRY_AT);
    }

    private static void broadcastRetreat(Piglin param0, LivingEntity param1) {
        getVisibleAdultPiglins(param0).forEach(param1x -> retreatFromNearestTarget(param1x, param1));
    }

    private static void retreatFromNearestTarget(Piglin param0, LivingEntity param1) {
        Brain<Piglin> var0 = param0.getBrain();
        LivingEntity var1 = BehaviorUtils.getNearestTarget(param0, var0.getMemory(MemoryModuleType.AVOID_TARGET), param1);
        var1 = BehaviorUtils.getNearestTarget(param0, var0.getMemory(MemoryModuleType.ATTACK_TARGET), var1);
        setAvoidTargetAndDontHuntForAWhile(param0, var1);
    }

    private static boolean wantsToStopFleeing(Piglin param0x) {
        return param0x.isAdult() && piglinsEqualOrOutnumberHoglins(param0x);
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
        param0.getBrain()
            .setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, param1, param0.level.getGameTime(), (long)RETREAT_DURATION.randomValue(param0.level.random));
        dontKillAnyMoreHoglinsForAWhile(param0);
    }

    public static void dontKillAnyMoreHoglinsForAWhile(Piglin param0) {
        param0.getBrain()
            .setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, param0.level.getGameTime(), (long)TIME_BETWEEN_HUNTS.randomValue(param0.level.random));
    }

    private static boolean seesPlayerHoldingWantedItem(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static void eat(Piglin param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, param0.level.getGameTime(), 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin param0) {
        Vec3 var0 = RandomPos.getLandPos(param0, 4, 2);
        return var0 == null ? param0.position() : var0;
    }

    private static boolean hasEatenRecently(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    static boolean isIdle(Piglin param0x) {
        return param0x.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity param0x) {
        return param0x.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, param0.level.getGameTime(), 120L);
    }

    private static boolean isAdmiringItem(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(Item param0) {
        return param0 == Items.GOLD_INGOT;
    }

    private static boolean isFood(Item param0) {
        return FOOD_ITEMS.contains(param0);
    }

    private static boolean isAttackAllowed(LivingEntity param0) {
        return EntitySelector.ATTACK_ALLOWED.test(param0);
    }

    private static boolean seesSoulFire(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_SOUL_FIRE);
    }

    private static boolean seesZombifiedPiglin(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity param0x) {
        return !seesPlayerHoldingLovedItem(param0x);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity param0x) {
        return param0x.getType() == EntityType.PLAYER && param0x.isHolding(PiglinAi::isLovedItem);
    }

    private static boolean wasHitByPlayer(Piglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.WAS_HIT_BY_PLAYER);
    }

    private static boolean wasHurtRecently(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin param0x) {
        return param0x.getOffhandItem().isEmpty() || !isLovedItem(param0x.getOffhandItem().getItem());
    }
}
