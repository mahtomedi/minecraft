package net.minecraft.world.entity.npc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Villager extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
    public static final int BREEDING_FOOD_THRESHOLD = 12;
    public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
    private static final int TRADES_PER_LEVEL = 2;
    private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(
        Items.BREAD,
        Items.POTATO,
        Items.CARROT,
        Items.WHEAT,
        Items.WHEAT_SEEDS,
        Items.BEETROOT,
        Items.BEETROOT_SEEDS,
        Items.TORCHFLOWER_SEEDS,
        Items.PITCHER_POD
    );
    private static final int MAX_GOSSIP_TOPICS = 10;
    private static final int GOSSIP_COOLDOWN = 1200;
    private static final int GOSSIP_DECAY_INTERVAL = 24000;
    private static final int REPUTATION_CHANGE_PER_EVENT = 25;
    private static final int HOW_FAR_AWAY_TO_TALK_TO_OTHER_VILLAGERS_ABOUT_GOLEMS = 10;
    private static final int HOW_MANY_VILLAGERS_NEED_TO_AGREE_TO_SPAWN_A_GOLEM = 5;
    private static final long TIME_SINCE_SLEEPING_FOR_GOLEM_SPAWNING = 24000L;
    @VisibleForTesting
    public static final float SPEED_MODIFIER = 0.5F;
    private int updateMerchantTimer;
    private boolean increaseProfessionLevelOnUpdate;
    @Nullable
    private Player lastTradedPlayer;
    private boolean chasing;
    private int foodLevel;
    private final GossipContainer gossips = new GossipContainer();
    private long lastGossipTime;
    private long lastGossipDecayTime;
    private int villagerXp;
    private long lastRestockGameTime;
    private int numberOfRestocksToday;
    private long lastRestockCheckDayTime;
    private boolean assignProfessionWhenSpawned;
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.HOME,
        MemoryModuleType.JOB_SITE,
        MemoryModuleType.POTENTIAL_JOB_SITE,
        MemoryModuleType.MEETING_POINT,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.VISIBLE_VILLAGER_BABIES,
        MemoryModuleType.NEAREST_PLAYERS,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
        MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.INTERACTION_TARGET,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.PATH,
        MemoryModuleType.DOORS_TO_CLOSE,
        MemoryModuleType.NEAREST_BED,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.NEAREST_HOSTILE,
        MemoryModuleType.SECONDARY_JOB_SITE,
        MemoryModuleType.HIDING_PLACE,
        MemoryModuleType.HEARD_BELL_TIME,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.LAST_SLEPT,
        MemoryModuleType.LAST_WOKEN,
        MemoryModuleType.LAST_WORKED_AT_POI,
        MemoryModuleType.GOLEM_DETECTED_RECENTLY
    );
    private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.NEAREST_PLAYERS,
        SensorType.NEAREST_ITEMS,
        SensorType.NEAREST_BED,
        SensorType.HURT_BY,
        SensorType.VILLAGER_HOSTILES,
        SensorType.VILLAGER_BABIES,
        SensorType.SECONDARY_POIS,
        SensorType.GOLEM_DETECTED
    );
    public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<Villager, Holder<PoiType>>> POI_MEMORIES = ImmutableMap.of(
        MemoryModuleType.HOME,
        (param0, param1) -> param1.is(PoiTypes.HOME),
        MemoryModuleType.JOB_SITE,
        (param0, param1) -> param0.getVillagerData().getProfession().heldJobSite().test(param1),
        MemoryModuleType.POTENTIAL_JOB_SITE,
        (param0, param1) -> VillagerProfession.ALL_ACQUIRABLE_JOBS.test(param1),
        MemoryModuleType.MEETING_POINT,
        (param0, param1) -> param1.is(PoiTypes.MEETING)
    );

    public Villager(EntityType<? extends Villager> param0, Level param1) {
        this(param0, param1, VillagerType.PLAINS);
    }

    public Villager(EntityType<? extends Villager> param0, Level param1, VillagerType param2) {
        super(param0, param1);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.setCanPickUpLoot(true);
        this.setVillagerData(this.getVillagerData().setType(param2).setProfession(VillagerProfession.NONE));
    }

    @Override
    public Brain<Villager> getBrain() {
        return super.getBrain();
    }

    @Override
    protected Brain.Provider<Villager> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        Brain<Villager> var0 = this.brainProvider().makeBrain(param0);
        this.registerBrainGoals(var0);
        return var0;
    }

    public void refreshBrain(ServerLevel param0) {
        Brain<Villager> var0 = this.getBrain();
        var0.stopAll(param0, this);
        this.brain = var0.copyWithoutBehaviors();
        this.registerBrainGoals(this.getBrain());
    }

    private void registerBrainGoals(Brain<Villager> param0) {
        VillagerProfession var0 = this.getVillagerData().getProfession();
        if (this.isBaby()) {
            param0.setSchedule(Schedule.VILLAGER_BABY);
            param0.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5F));
        } else {
            param0.setSchedule(Schedule.VILLAGER_DEFAULT);
            param0.addActivityWithConditions(
                Activity.WORK, VillagerGoalPackages.getWorkPackage(var0, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT))
            );
        }

        param0.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage(var0, 0.5F));
        param0.addActivityWithConditions(
            Activity.MEET,
            VillagerGoalPackages.getMeetPackage(var0, 0.5F),
            ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT))
        );
        param0.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(var0, 0.5F));
        param0.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(var0, 0.5F));
        param0.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(var0, 0.5F));
        param0.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(var0, 0.5F));
        param0.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(var0, 0.5F));
        param0.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(var0, 0.5F));
        param0.setCoreActivities(ImmutableSet.of(Activity.CORE));
        param0.setDefaultActivity(Activity.IDLE);
        param0.setActiveActivityIfPossible(Activity.IDLE);
        param0.updateActivityFromSchedule(this.level().getDayTime(), this.level().getGameTime());
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (this.level() instanceof ServerLevel) {
            this.refreshBrain((ServerLevel)this.level());
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public boolean assignProfessionWhenSpawned() {
        return this.assignProfessionWhenSpawned;
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("villagerBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        if (this.assignProfessionWhenSpawned) {
            this.assignProfessionWhenSpawned = false;
        }

        if (!this.isTrading() && this.updateMerchantTimer > 0) {
            --this.updateMerchantTimer;
            if (this.updateMerchantTimer <= 0) {
                if (this.increaseProfessionLevelOnUpdate) {
                    this.increaseMerchantCareer();
                    this.increaseProfessionLevelOnUpdate = false;
                }

                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            }
        }

        if (this.lastTradedPlayer != null && this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
            this.level().broadcastEntityEvent(this, (byte)14);
            this.lastTradedPlayer = null;
        }

        if (!this.isNoAi() && this.random.nextInt(100) == 0) {
            Raid var0 = ((ServerLevel)this.level()).getRaidAt(this.blockPosition());
            if (var0 != null && var0.isActive() && !var0.isOver()) {
                this.level().broadcastEntityEvent(this, (byte)42);
            }
        }

        if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.isTrading()) {
            this.stopTrading();
        }

        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getUnhappyCounter() > 0) {
            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
        }

        this.maybeDecayGossip();
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.is(Items.VILLAGER_SPAWN_EGG) || !this.isAlive() || this.isTrading() || this.isSleeping()) {
            return super.mobInteract(param0, param1);
        } else if (this.isBaby()) {
            this.setUnhappy();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            boolean var1 = this.getOffers().isEmpty();
            if (param1 == InteractionHand.MAIN_HAND) {
                if (var1 && !this.level().isClientSide) {
                    this.setUnhappy();
                }

                param0.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (var1) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                if (!this.level().isClientSide && !this.offers.isEmpty()) {
                    this.startTrading(param0);
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
    }

    private void setUnhappy() {
        this.setUnhappyCounter(40);
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    private void startTrading(Player param0) {
        this.updateSpecialPrices(param0);
        this.setTradingPlayer(param0);
        this.openTradingScreen(param0, this.getDisplayName(), this.getVillagerData().getLevel());
    }

    @Override
    public void setTradingPlayer(@Nullable Player param0) {
        boolean var0 = this.getTradingPlayer() != null && param0 == null;
        super.setTradingPlayer(param0);
        if (var0) {
            this.stopTrading();
        }

    }

    @Override
    protected void stopTrading() {
        super.stopTrading();
        this.resetSpecialPrices();
    }

    private void resetSpecialPrices() {
        for(MerchantOffer var0 : this.getOffers()) {
            var0.resetSpecialPriceDiff();
        }

    }

    @Override
    public boolean canRestock() {
        return true;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    public void restock() {
        this.updateDemand();

        for(MerchantOffer var0 : this.getOffers()) {
            var0.resetUses();
        }

        this.resendOffersToTradingPlayer();
        this.lastRestockGameTime = this.level().getGameTime();
        ++this.numberOfRestocksToday;
    }

    private void resendOffersToTradingPlayer() {
        MerchantOffers var0 = this.getOffers();
        Player var1 = this.getTradingPlayer();
        if (var1 != null && !var0.isEmpty()) {
            var1.sendMerchantOffers(
                var1.containerMenu.containerId, var0, this.getVillagerData().getLevel(), this.getVillagerXp(), this.showProgressBar(), this.canRestock()
            );
        }

    }

    private boolean needsToRestock() {
        for(MerchantOffer var0 : this.getOffers()) {
            if (var0.needsRestock()) {
                return true;
            }
        }

        return false;
    }

    private boolean allowedToRestock() {
        return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level().getGameTime() > this.lastRestockGameTime + 2400L;
    }

    public boolean shouldRestock() {
        long var0 = this.lastRestockGameTime + 12000L;
        long var1 = this.level().getGameTime();
        boolean var2 = var1 > var0;
        long var3 = this.level().getDayTime();
        if (this.lastRestockCheckDayTime > 0L) {
            long var4 = this.lastRestockCheckDayTime / 24000L;
            long var5 = var3 / 24000L;
            var2 |= var5 > var4;
        }

        this.lastRestockCheckDayTime = var3;
        if (var2) {
            this.lastRestockGameTime = var1;
            this.resetNumberOfRestocks();
        }

        return this.allowedToRestock() && this.needsToRestock();
    }

    private void catchUpDemand() {
        int var0 = 2 - this.numberOfRestocksToday;
        if (var0 > 0) {
            for(MerchantOffer var1 : this.getOffers()) {
                var1.resetUses();
            }
        }

        for(int var2 = 0; var2 < var0; ++var2) {
            this.updateDemand();
        }

        this.resendOffersToTradingPlayer();
    }

    private void updateDemand() {
        for(MerchantOffer var0 : this.getOffers()) {
            var0.updateDemand();
        }

    }

    private void updateSpecialPrices(Player param0) {
        int var0 = this.getPlayerReputation(param0);
        if (var0 != 0) {
            for(MerchantOffer var1 : this.getOffers()) {
                var1.addToSpecialPriceDiff(-Mth.floor((float)var0 * var1.getPriceMultiplier()));
            }
        }

        if (param0.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
            MobEffectInstance var2 = param0.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
            int var3 = var2.getAmplifier();

            for(MerchantOffer var4 : this.getOffers()) {
                double var5 = 0.3 + 0.0625 * (double)var3;
                int var6 = (int)Math.floor(var5 * (double)var4.getBaseCostA().getCount());
                var4.addToSpecialPriceDiff(-Math.max(var6, 1));
            }
        }

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        VillagerData.CODEC
            .encodeStart(NbtOps.INSTANCE, this.getVillagerData())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("VillagerData", param1));
        param0.putByte("FoodLevel", (byte)this.foodLevel);
        param0.put("Gossips", this.gossips.store(NbtOps.INSTANCE));
        param0.putInt("Xp", this.villagerXp);
        param0.putLong("LastRestock", this.lastRestockGameTime);
        param0.putLong("LastGossipDecay", this.lastGossipDecayTime);
        param0.putInt("RestocksToday", this.numberOfRestocksToday);
        if (this.assignProfessionWhenSpawned) {
            param0.putBoolean("AssignProfessionWhenSpawned", true);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("VillagerData", 10)) {
            DataResult<VillagerData> var0 = VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, param0.get("VillagerData")));
            var0.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
        }

        if (param0.contains("Offers", 10)) {
            this.offers = new MerchantOffers(param0.getCompound("Offers"));
        }

        if (param0.contains("FoodLevel", 1)) {
            this.foodLevel = param0.getByte("FoodLevel");
        }

        ListTag var1 = param0.getList("Gossips", 10);
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, var1));
        if (param0.contains("Xp", 3)) {
            this.villagerXp = param0.getInt("Xp");
        }

        this.lastRestockGameTime = param0.getLong("LastRestock");
        this.lastGossipDecayTime = param0.getLong("LastGossipDecay");
        this.setCanPickUpLoot(true);
        if (this.level() instanceof ServerLevel) {
            this.refreshBrain((ServerLevel)this.level());
        }

        this.numberOfRestocksToday = param0.getInt("RestocksToday");
        if (param0.contains("AssignProfessionWhenSpawned")) {
            this.assignProfessionWhenSpawned = param0.getBoolean("AssignProfessionWhenSpawned");
        }

    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return null;
        } else {
            return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    public void playWorkSound() {
        SoundEvent var0 = this.getVillagerData().getProfession().workSound();
        if (var0 != null) {
            this.playSound(var0, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    @Override
    public void setVillagerData(VillagerData param0x) {
        VillagerData var0x = this.getVillagerData();
        if (var0x.getProfession() != param0x.getProfession()) {
            this.offers = null;
        }

        this.entityData.set(DATA_VILLAGER_DATA, param0x);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER_DATA);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer param0) {
        int var0 = 3 + this.random.nextInt(4);
        this.villagerXp += param0.getXp();
        this.lastTradedPlayer = this.getTradingPlayer();
        if (this.shouldIncreaseLevel()) {
            this.updateMerchantTimer = 40;
            this.increaseProfessionLevelOnUpdate = true;
            var0 += 5;
        }

        if (param0.shouldRewardExp()) {
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), var0));
        }

    }

    public void setChasing(boolean param0) {
        this.chasing = param0;
    }

    public boolean isChasing() {
        return this.chasing;
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity param0) {
        if (param0 != null && this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).onReputationEvent(ReputationEventType.VILLAGER_HURT, param0, this);
            if (this.isAlive() && param0 instanceof Player) {
                this.level().broadcastEntityEvent(this, (byte)13);
            }
        }

        super.setLastHurtByMob(param0);
    }

    @Override
    public void die(DamageSource param0) {
        LOGGER.info("Villager {} died, message: '{}'", this, param0.getLocalizedDeathMessage(this).getString());
        Entity var0 = param0.getEntity();
        if (var0 != null) {
            this.tellWitnessesThatIWasMurdered(var0);
        }

        this.releaseAllPois();
        super.die(param0);
    }

    private void releaseAllPois() {
        this.releasePoi(MemoryModuleType.HOME);
        this.releasePoi(MemoryModuleType.JOB_SITE);
        this.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
        this.releasePoi(MemoryModuleType.MEETING_POINT);
    }

    private void tellWitnessesThatIWasMurdered(Entity param0) {
        Level var2 = this.level();
        if (var2 instanceof ServerLevel var0) {
            Optional<NearestVisibleLivingEntities> var2x = this.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            if (!var2x.isEmpty()) {
                var2x.get()
                    .findAll(ReputationEventHandler.class::isInstance)
                    .forEach(param2 -> var0.onReputationEvent(ReputationEventType.VILLAGER_KILLED, param0, (ReputationEventHandler)param2));
            }
        }
    }

    public void releasePoi(MemoryModuleType<GlobalPos> param0) {
        if (this.level() instanceof ServerLevel) {
            MinecraftServer var0 = ((ServerLevel)this.level()).getServer();
            this.brain.getMemory(param0).ifPresent(param2 -> {
                ServerLevel var0x = var0.getLevel(param2.dimension());
                if (var0x != null) {
                    PoiManager var1x = var0x.getPoiManager();
                    Optional<Holder<PoiType>> var2x = var1x.getType(param2.pos());
                    BiPredicate<Villager, Holder<PoiType>> var3 = POI_MEMORIES.get(param0);
                    if (var2x.isPresent() && var3.test(this, (Holder<PoiType>)var2x.get())) {
                        var1x.release(param2.pos());
                        DebugPackets.sendPoiTicketCountPacket(var0x, param2.pos());
                    }

                }
            });
        }
    }

    @Override
    public boolean canBreed() {
        return this.foodLevel + this.countFoodPointsInInventory() >= 12 && !this.isSleeping() && this.getAge() == 0;
    }

    private boolean hungry() {
        return this.foodLevel < 12;
    }

    private void eatUntilFull() {
        if (this.hungry() && this.countFoodPointsInInventory() != 0) {
            for(int var0 = 0; var0 < this.getInventory().getContainerSize(); ++var0) {
                ItemStack var1 = this.getInventory().getItem(var0);
                if (!var1.isEmpty()) {
                    Integer var2 = FOOD_POINTS.get(var1.getItem());
                    if (var2 != null) {
                        int var3 = var1.getCount();

                        for(int var4 = var3; var4 > 0; --var4) {
                            this.foodLevel += var2;
                            this.getInventory().removeItem(var0, 1);
                            if (!this.hungry()) {
                                return;
                            }
                        }
                    }
                }
            }

        }
    }

    public int getPlayerReputation(Player param0) {
        return this.gossips.getReputation(param0.getUUID(), param0x -> true);
    }

    private void digestFood(int param0) {
        this.foodLevel -= param0;
    }

    public void eatAndDigestFood() {
        this.eatUntilFull();
        this.digestFood(12);
    }

    public void setOffers(MerchantOffers param0) {
        this.offers = param0;
    }

    private boolean shouldIncreaseLevel() {
        int var0 = this.getVillagerData().getLevel();
        return VillagerData.canLevelUp(var0) && this.villagerXp >= VillagerData.getMaxXpPerLevel(var0);
    }

    private void increaseMerchantCareer() {
        this.setVillagerData(this.getVillagerData().setLevel(this.getVillagerData().getLevel() + 1));
        this.updateTrades();
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(
            this.getType().getDescriptionId() + "." + BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.getVillagerData().getProfession()).getPath()
        );
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 12) {
            this.addParticlesAroundSelf(ParticleTypes.HEART);
        } else if (param0 == 13) {
            this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
        } else if (param0 == 14) {
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else if (param0 == 42) {
            this.addParticlesAroundSelf(ParticleTypes.SPLASH);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param2 == MobSpawnType.BREEDING) {
            this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NONE));
        }

        if (param2 == MobSpawnType.COMMAND || param2 == MobSpawnType.SPAWN_EGG || MobSpawnType.isSpawner(param2) || param2 == MobSpawnType.DISPENSER) {
            this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(param0.getBiome(this.blockPosition()))));
        }

        if (param2 == MobSpawnType.STRUCTURE) {
            this.assignProfessionWhenSpawned = true;
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Nullable
    public Villager getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        double var0 = this.random.nextDouble();
        VillagerType var1;
        if (var0 < 0.5) {
            var1 = VillagerType.byBiome(param0.getBiome(this.blockPosition()));
        } else if (var0 < 0.75) {
            var1 = this.getVillagerData().getType();
        } else {
            var1 = ((Villager)param1).getVillagerData().getType();
        }

        Villager var4 = new Villager(EntityType.VILLAGER, param0, var1);
        var4.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var4.blockPosition()), MobSpawnType.BREEDING, null, null);
        return var4;
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
        if (param0.getDifficulty() != Difficulty.PEACEFUL) {
            LOGGER.info("Villager {} was struck by lightning {}.", this, param1);
            Witch var0 = EntityType.WITCH.create(param0);
            if (var0 != null) {
                var0.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                var0.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var0.blockPosition()), MobSpawnType.CONVERSION, null, null);
                var0.setNoAi(this.isNoAi());
                if (this.hasCustomName()) {
                    var0.setCustomName(this.getCustomName());
                    var0.setCustomNameVisible(this.isCustomNameVisible());
                }

                var0.setPersistenceRequired();
                param0.addFreshEntityWithPassengers(var0);
                this.releaseAllPois();
                this.discard();
            } else {
                super.thunderHit(param0, param1);
            }
        } else {
            super.thunderHit(param0, param1);
        }

    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        InventoryCarrier.pickUpItem(this, this, param0);
    }

    @Override
    public boolean wantsToPickUp(ItemStack param0) {
        Item var0 = param0.getItem();
        return (WANTED_ITEMS.contains(var0) || this.getVillagerData().getProfession().requestedItems().contains(var0))
            && this.getInventory().canAddItem(param0);
    }

    public boolean hasExcessFood() {
        return this.countFoodPointsInInventory() >= 24;
    }

    public boolean wantsMoreFood() {
        return this.countFoodPointsInInventory() < 12;
    }

    private int countFoodPointsInInventory() {
        SimpleContainer var0 = this.getInventory();
        return FOOD_POINTS.entrySet().stream().mapToInt(param1 -> var0.countItem(param1.getKey()) * param1.getValue()).sum();
    }

    public boolean hasFarmSeeds() {
        return this.getInventory().hasAnyMatching(param0 -> param0.is(ItemTags.VILLAGER_PLANTABLE_SEEDS));
    }

    @Override
    protected void updateTrades() {
        VillagerData var0 = this.getVillagerData();
        Int2ObjectMap<VillagerTrades.ItemListing[]> var2;
        if (this.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> var1 = VillagerTrades.EXPERIMENTAL_TRADES.get(var0.getProfession());
            var2 = var1 != null ? var1 : VillagerTrades.TRADES.get(var0.getProfession());
        } else {
            var2 = VillagerTrades.TRADES.get(var0.getProfession());
        }

        if (var2 != null && !var2.isEmpty()) {
            VillagerTrades.ItemListing[] var4 = var2.get(var0.getLevel());
            if (var4 != null) {
                MerchantOffers var5 = this.getOffers();
                this.addOffersFromItemListings(var5, var4, 2);
            }
        }
    }

    public void gossip(ServerLevel param0, Villager param1, long param2) {
        if ((param2 < this.lastGossipTime || param2 >= this.lastGossipTime + 1200L)
            && (param2 < param1.lastGossipTime || param2 >= param1.lastGossipTime + 1200L)) {
            this.gossips.transferFrom(param1.gossips, this.random, 10);
            this.lastGossipTime = param2;
            param1.lastGossipTime = param2;
            this.spawnGolemIfNeeded(param0, param2, 5);
        }
    }

    private void maybeDecayGossip() {
        long var0 = this.level().getGameTime();
        if (this.lastGossipDecayTime == 0L) {
            this.lastGossipDecayTime = var0;
        } else if (var0 >= this.lastGossipDecayTime + 24000L) {
            this.gossips.decay();
            this.lastGossipDecayTime = var0;
        }
    }

    public void spawnGolemIfNeeded(ServerLevel param0, long param1, int param2) {
        if (this.wantsToSpawnGolem(param1)) {
            AABB var0 = this.getBoundingBox().inflate(10.0, 10.0, 10.0);
            List<Villager> var1 = param0.getEntitiesOfClass(Villager.class, var0);
            List<Villager> var2 = var1.stream().filter(param1x -> param1x.wantsToSpawnGolem(param1)).limit(5L).collect(Collectors.toList());
            if (var2.size() >= param2) {
                if (!SpawnUtil.trySpawnMob(
                        EntityType.IRON_GOLEM, MobSpawnType.MOB_SUMMONED, param0, this.blockPosition(), 10, 8, 6, SpawnUtil.Strategy.LEGACY_IRON_GOLEM
                    )
                    .isEmpty()) {
                    var1.forEach(GolemSensor::golemDetected);
                }
            }
        }
    }

    public boolean wantsToSpawnGolem(long param0) {
        if (!this.golemSpawnConditionsMet(this.level().getGameTime())) {
            return false;
        } else {
            return !this.brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
        }
    }

    @Override
    public void onReputationEventFrom(ReputationEventType param0, Entity param1) {
        if (param0 == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
            this.gossips.add(param1.getUUID(), GossipType.MAJOR_POSITIVE, 20);
            this.gossips.add(param1.getUUID(), GossipType.MINOR_POSITIVE, 25);
        } else if (param0 == ReputationEventType.TRADE) {
            this.gossips.add(param1.getUUID(), GossipType.TRADING, 2);
        } else if (param0 == ReputationEventType.VILLAGER_HURT) {
            this.gossips.add(param1.getUUID(), GossipType.MINOR_NEGATIVE, 25);
        } else if (param0 == ReputationEventType.VILLAGER_KILLED) {
            this.gossips.add(param1.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
        }

    }

    @Override
    public int getVillagerXp() {
        return this.villagerXp;
    }

    public void setVillagerXp(int param0) {
        this.villagerXp = param0;
    }

    private void resetNumberOfRestocks() {
        this.catchUpDemand();
        this.numberOfRestocksToday = 0;
    }

    public GossipContainer getGossips() {
        return this.gossips;
    }

    public void setGossips(Tag param0) {
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, param0));
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void startSleeping(BlockPos param0) {
        super.startSleeping(param0);
        this.brain.setMemory(MemoryModuleType.LAST_SLEPT, this.level().getGameTime());
        this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    @Override
    public void stopSleeping() {
        super.stopSleeping();
        this.brain.setMemory(MemoryModuleType.LAST_WOKEN, this.level().getGameTime());
    }

    private boolean golemSpawnConditionsMet(long param0) {
        Optional<Long> var0 = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
        if (var0.isPresent()) {
            return param0 - var0.get() < 24000L;
        } else {
            return false;
        }
    }
}
