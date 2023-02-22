package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Axolotl extends Animal implements LerpingModel, VariantHolder<Axolotl.Variant>, Bucketable {
    public static final int TOTAL_PLAYDEAD_TIME = 200;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.AXOLOTL_TEMPTATIONS
    );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.PLAY_DEAD_TICKS,
        MemoryModuleType.NEAREST_ATTACKABLE,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.HAS_HUNTING_COOLDOWN,
        MemoryModuleType.IS_PANICKING
    );
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    public static final double PLAYER_REGEN_DETECTION_RANGE = 20.0;
    public static final int RARE_VARIANT_CHANCE = 1200;
    private static final int AXOLOTL_TOTAL_AIR_SUPPLY = 6000;
    public static final String VARIANT_TAG = "Variant";
    private static final int REHYDRATE_AIR_SUPPLY = 1800;
    private static final int REGEN_BUFF_MAX_DURATION = 2400;
    private final Map<String, Vector3f> modelRotationValues = Maps.newHashMap();
    private static final int REGEN_BUFF_BASE_DURATION = 100;

    public Axolotl(EntityType<? extends Axolotl> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.moveControl = new Axolotl.AxolotlMoveControl(this);
        this.lookControl = new Axolotl.AxolotlLookControl(this, 20);
        this.setMaxUpStep(1.0F);
    }

    @Override
    public Map<String, Vector3f> getModelRotationValues() {
        return this.modelRotationValues;
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return 0.0F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_PLAYING_DEAD, false);
        this.entityData.define(FROM_BUCKET, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant().getId());
        param0.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(Axolotl.Variant.byId(param0.getInt("Variant")));
        this.setFromBucket(param0.getBoolean("FromBucket"));
    }

    @Override
    public void playAmbientSound() {
        if (!this.isPlayingDead()) {
            super.playAmbientSound();
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        boolean var0 = false;
        if (param2 == MobSpawnType.BUCKET) {
            return param3;
        } else {
            RandomSource var1 = param0.getRandom();
            if (param3 instanceof Axolotl.AxolotlGroupData) {
                if (((Axolotl.AxolotlGroupData)param3).getGroupSize() >= 2) {
                    var0 = true;
                }
            } else {
                param3 = new Axolotl.AxolotlGroupData(Axolotl.Variant.getCommonSpawnVariant(var1), Axolotl.Variant.getCommonSpawnVariant(var1));
            }

            this.setVariant(((Axolotl.AxolotlGroupData)param3).getVariant(var1));
            if (var0) {
                this.setAge(-24000);
            }

            return super.finalizeSpawn(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public void baseTick() {
        int var0 = this.getAirSupply();
        super.baseTick();
        if (!this.isNoAi()) {
            this.handleAirSupply(var0);
        }

    }

    protected void handleAirSupply(int param0) {
        if (this.isAlive() && !this.isInWaterRainOrBubble()) {
            this.setAirSupply(param0 - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(this.damageSources().dryOut(), 2.0F);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }

    }

    public void rehydrate() {
        int var0 = this.getAirSupply() + 1800;
        this.setAirSupply(Math.min(var0, this.getMaxAirSupply()));
    }

    @Override
    public int getMaxAirSupply() {
        return 6000;
    }

    public Axolotl.Variant getVariant() {
        return Axolotl.Variant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(Axolotl.Variant param0) {
        this.entityData.set(DATA_VARIANT, param0.getId());
    }

    private static boolean useRareVariant(RandomSource param0) {
        return param0.nextInt(1200) == 0;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    public void setPlayingDead(boolean param0) {
        this.entityData.set(DATA_PLAYING_DEAD, param0);
    }

    public boolean isPlayingDead() {
        return this.entityData.get(DATA_PLAYING_DEAD);
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean param0) {
        this.entityData.set(FROM_BUCKET, param0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Axolotl var0 = EntityType.AXOLOTL.create(param0);
        if (var0 != null) {
            Axolotl.Variant var1;
            if (useRareVariant(this.random)) {
                var1 = Axolotl.Variant.getRareSpawnVariant(this.random);
            } else {
                var1 = this.random.nextBoolean() ? this.getVariant() : ((Axolotl)param1).getVariant();
            }

            var0.setVariant(var1);
            var0.setPersistenceRequired();
        }

        return var0;
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity param0) {
        return 1.5 + (double)param0.getBbWidth() * 2.0;
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.is(ItemTags.AXOLOTL_TEMPT_ITEMS);
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("axolotlBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("axolotlActivityUpdate");
        AxolotlAi.updateActivity(this);
        this.level.getProfiler().pop();
        if (!this.isNoAi()) {
            Optional<Integer> var0 = this.getBrain().getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            this.setPlayingDead(var0.isPresent() && var0.get() > 0);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new AmphibiousPathNavigation(this, param0);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = param0.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (var0) {
            this.doEnchantDamageEffects(this, param0);
            this.playSound(SoundEvents.AXOLOTL_ATTACK, 1.0F, 1.0F);
        }

        return var0;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        float var0 = this.getHealth();
        if (!this.level.isClientSide
            && !this.isNoAi()
            && this.level.random.nextInt(3) == 0
            && ((float)this.level.random.nextInt(3) < param1 || var0 / this.getMaxHealth() < 0.5F)
            && param1 < var0
            && this.isInWater()
            && (param0.getEntity() != null || param0.getDirectEntity() != null)
            && !this.isPlayingDead()) {
            this.brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, 200);
        }

        return super.hurt(param0, param1);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.655F;
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        return Bucketable.bucketMobPickup(param0, param1, this).orElse(super.mobInteract(param0, param1));
    }

    @Override
    public void saveToBucketTag(ItemStack param0) {
        Bucketable.saveDefaultDataToBucketTag(this, param0);
        CompoundTag var0 = param0.getOrCreateTag();
        var0.putInt("Variant", this.getVariant().getId());
        var0.putInt("Age", this.getAge());
        Brain<?> var1 = this.getBrain();
        if (var1.hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
            var0.putLong("HuntingCooldown", var1.getTimeUntilExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
        }

    }

    @Override
    public void loadFromBucketTag(CompoundTag param0) {
        Bucketable.loadDefaultDataFromBucketTag(this, param0);
        this.setVariant(Axolotl.Variant.byId(param0.getInt("Variant")));
        if (param0.contains("Age")) {
            this.setAge(param0.getInt("Age"));
        }

        if (param0.contains("HuntingCooldown")) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, param0.getLong("HuntingCooldown"));
        }

    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.AXOLOTL_BUCKET);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !this.isPlayingDead() && super.canBeSeenAsEnemy();
    }

    public static void onStopAttacking(Axolotl param0, LivingEntity param1) {
        Level var0 = param0.level;
        if (param1.isDeadOrDying()) {
            DamageSource var1 = param1.getLastDamageSource();
            if (var1 != null) {
                Entity var2 = var1.getEntity();
                if (var2 != null && var2.getType() == EntityType.PLAYER) {
                    Player var3 = (Player)var2;
                    List<Player> var4 = var0.getEntitiesOfClass(Player.class, param0.getBoundingBox().inflate(20.0));
                    if (var4.contains(var3)) {
                        param0.applySupportingEffects(var3);
                    }
                }
            }
        }

    }

    public void applySupportingEffects(Player param0) {
        MobEffectInstance var0 = param0.getEffect(MobEffects.REGENERATION);
        if (var0 == null || var0.endsWithin(2399)) {
            int var1 = var0 != null ? var0.getDuration() : 0;
            int var2 = Math.min(2400, 100 + var1);
            param0.addEffect(new MobEffectInstance(MobEffects.REGENERATION, var2, 0), this);
        }

        param0.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.AXOLOTL_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    @Override
    protected Brain.Provider<Axolotl> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return AxolotlAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Axolotl> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isControlledByLocalInstance() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(param0);
        }

    }

    @Override
    protected void usePlayerItem(Player param0, InteractionHand param1, ItemStack param2) {
        if (param2.is(Items.TROPICAL_FISH_BUCKET)) {
            param0.setItemInHand(param1, new ItemStack(Items.WATER_BUCKET));
        } else {
            super.usePlayerItem(param0, param1, param2);
        }

    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    public static boolean checkAxolotlSpawnRules(
        EntityType<? extends LivingEntity> param0, ServerLevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4
    ) {
        return param1.getBlockState(param3.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON);
    }

    public static class AxolotlGroupData extends AgeableMob.AgeableMobGroupData {
        public final Axolotl.Variant[] types;

        public AxolotlGroupData(Axolotl.Variant... param0) {
            super(false);
            this.types = param0;
        }

        public Axolotl.Variant getVariant(RandomSource param0) {
            return this.types[param0.nextInt(this.types.length)];
        }
    }

    class AxolotlLookControl extends SmoothSwimmingLookControl {
        public AxolotlLookControl(Axolotl param0, int param1) {
            super(param0, param1);
        }

        @Override
        public void tick() {
            if (!Axolotl.this.isPlayingDead()) {
                super.tick();
            }

        }
    }

    static class AxolotlMoveControl extends SmoothSwimmingMoveControl {
        private final Axolotl axolotl;

        public AxolotlMoveControl(Axolotl param0) {
            super(param0, 85, 10, 0.1F, 0.5F, false);
            this.axolotl = param0;
        }

        @Override
        public void tick() {
            if (!this.axolotl.isPlayingDead()) {
                super.tick();
            }

        }
    }

    public static enum Variant implements StringRepresentable {
        LUCY(0, "lucy", true),
        WILD(1, "wild", true),
        GOLD(2, "gold", true),
        CYAN(3, "cyan", true),
        BLUE(4, "blue", false);

        private static final IntFunction<Axolotl.Variant> BY_ID = ByIdMap.continuous(Axolotl.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Axolotl.Variant> CODEC = StringRepresentable.fromEnum(Axolotl.Variant::values);
        private final int id;
        private final String name;
        private final boolean common;

        private Variant(int param0, String param1, boolean param2) {
            this.id = param0;
            this.name = param1;
            this.common = param2;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Axolotl.Variant byId(int param0) {
            return BY_ID.apply(param0);
        }

        public static Axolotl.Variant getCommonSpawnVariant(RandomSource param0) {
            return getSpawnVariant(param0, true);
        }

        public static Axolotl.Variant getRareSpawnVariant(RandomSource param0) {
            return getSpawnVariant(param0, false);
        }

        private static Axolotl.Variant getSpawnVariant(RandomSource param0, boolean param1) {
            Axolotl.Variant[] var0 = Arrays.stream(values()).filter(param1x -> param1x.common == param1).toArray(param0x -> new Axolotl.Variant[param0x]);
            return Util.getRandom(var0, param0);
        }
    }
}
