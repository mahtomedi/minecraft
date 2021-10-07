package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Fox extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
    private static final int FLAG_SITTING = 1;
    public static final int FLAG_CROUCHING = 4;
    public static final int FLAG_INTERESTED = 8;
    public static final int FLAG_POUNCING = 16;
    private static final int FLAG_SLEEPING = 32;
    private static final int FLAG_FACEPLANTED = 64;
    private static final int FLAG_DEFENDING = 128;
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = param0 -> !param0.hasPickUpDelay() && param0.isAlive();
    private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = param0 -> {
        if (!(param0 instanceof LivingEntity)) {
            return false;
        } else {
            LivingEntity var0 = (LivingEntity)param0;
            return var0.getLastHurtMob() != null && var0.getLastHurtMobTimestamp() < var0.tickCount + 600;
        }
    };
    static final Predicate<Entity> STALKABLE_PREY = param0 -> param0 instanceof Chicken || param0 instanceof Rabbit;
    private static final Predicate<Entity> AVOID_PLAYERS = param0 -> !param0.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(param0);
    private static final int MIN_TICKS_BEFORE_EAT = 600;
    private Goal landTargetGoal;
    private Goal turtleEggTargetGoal;
    private Goal fishTargetGoal;
    private float interestedAngle;
    private float interestedAngleO;
    float crouchAmount;
    float crouchAmountO;
    private int ticksSinceEaten;

    public Fox(EntityType<? extends Fox> param0, Level param1) {
        super(param0, param1);
        this.lookControl = new Fox.FoxLookControl();
        this.moveControl = new Fox.FoxMoveControl();
        this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTED_ID_0, Optional.empty());
        this.entityData.define(DATA_TRUSTED_ID_1, Optional.empty());
        this.entityData.define(DATA_TYPE_ID, 0);
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void registerGoals() {
        this.landTargetGoal = new NearestAttackableTargetGoal<>(
            this, Animal.class, 10, false, false, param0 -> param0 instanceof Chicken || param0 instanceof Rabbit
        );
        this.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
        this.fishTargetGoal = new NearestAttackableTargetGoal<>(this, AbstractFish.class, 20, false, false, param0 -> param0 instanceof AbstractSchoolingFish);
        this.goalSelector.addGoal(0, new Fox.FoxFloatGoal());
        this.goalSelector.addGoal(1, new Fox.FaceplantGoal());
        this.goalSelector.addGoal(2, new Fox.FoxPanicGoal(2.2));
        this.goalSelector.addGoal(3, new Fox.FoxBreedGoal(1.0));
        this.goalSelector
            .addGoal(
                4,
                new AvoidEntityGoal<>(
                    this, Player.class, 16.0F, 1.6, 1.4, param0 -> AVOID_PLAYERS.test(param0) && !this.trusts(param0.getUUID()) && !this.isDefending()
                )
            );
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Wolf.class, 8.0F, 1.6, 1.4, param0 -> !((Wolf)param0).isTame() && !this.isDefending()));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 8.0F, 1.6, 1.4, param0 -> !this.isDefending()));
        this.goalSelector.addGoal(5, new Fox.StalkPreyGoal());
        this.goalSelector.addGoal(6, new Fox.FoxPounceGoal());
        this.goalSelector.addGoal(6, new Fox.SeekShelterGoal(1.25));
        this.goalSelector.addGoal(7, new Fox.FoxMeleeAttackGoal(1.2F, true));
        this.goalSelector.addGoal(7, new Fox.SleepGoal());
        this.goalSelector.addGoal(8, new Fox.FoxFollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(9, new Fox.FoxStrollThroughVillageGoal(32, 200));
        this.goalSelector.addGoal(10, new Fox.FoxEatBerriesGoal(1.2F, 12, 1));
        this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new Fox.FoxSearchForItemsGoal());
        this.goalSelector.addGoal(12, new Fox.FoxLookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(13, new Fox.PerchAndSearchGoal());
        this.targetSelector
            .addGoal(
                3,
                new Fox.DefendTrustedTargetGoal(
                    LivingEntity.class, false, false, param0 -> TRUSTED_TARGET_SELECTOR.test(param0) && !this.trusts(param0.getUUID())
                )
            );
    }

    @Override
    public SoundEvent getEatingSound(ItemStack param0) {
        return SoundEvents.FOX_EAT;
    }

    @Override
    public void aiStep() {
        if (!this.level.isClientSide && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack var0 = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.canEat(var0)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack var1 = var0.finishUsingItem(this.level, this);
                    if (!var1.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, var1);
                    }

                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatingSound(var0), 1.0F, 1.0F);
                    this.level.broadcastEntityEvent(this, (byte)45);
                }
            }

            LivingEntity var2 = this.getTarget();
            if (var2 == null || !var2.isAlive()) {
                this.setIsCrouching(false);
                this.setIsInterested(false);
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05F) {
            this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
        }

    }

    @Override
    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    private boolean canEat(ItemStack param0) {
        return param0.getItem().isEdible() && this.getTarget() == null && this.onGround && !this.isSleeping();
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        if (this.random.nextFloat() < 0.2F) {
            float var0 = this.random.nextFloat();
            ItemStack var1;
            if (var0 < 0.05F) {
                var1 = new ItemStack(Items.EMERALD);
            } else if (var0 < 0.2F) {
                var1 = new ItemStack(Items.EGG);
            } else if (var0 < 0.4F) {
                var1 = this.random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else if (var0 < 0.6F) {
                var1 = new ItemStack(Items.WHEAT);
            } else if (var0 < 0.8F) {
                var1 = new ItemStack(Items.LEATHER);
            } else {
                var1 = new ItemStack(Items.FEATHER);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, var1);
        }

    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 45) {
            ItemStack var0 = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!var0.isEmpty()) {
                for(int var1 = 0; var1 < 8; ++var1) {
                    Vec3 var2 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
                        .xRot(-this.getXRot() * (float) (Math.PI / 180.0))
                        .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
                    this.level
                        .addParticle(
                            new ItemParticleOption(ParticleTypes.ITEM, var0),
                            this.getX() + this.getLookAngle().x / 2.0,
                            this.getY(),
                            this.getZ() + this.getLookAngle().z / 2.0,
                            var2.x,
                            var2.y + 0.05,
                            var2.z
                        );
                }
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    public Fox getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Fox var0 = EntityType.FOX.create(param0);
        var0.setFoxType(this.random.nextBoolean() ? this.getFoxType() : ((Fox)param1).getFoxType());
        return var0;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        Optional<ResourceKey<Biome>> var0 = param0.getBiomeName(this.blockPosition());
        Fox.Type var1 = Fox.Type.byBiome(var0);
        boolean var2 = false;
        if (param3 instanceof Fox.FoxGroupData) {
            var1 = ((Fox.FoxGroupData)param3).type;
            if (((Fox.FoxGroupData)param3).getGroupSize() >= 2) {
                var2 = true;
            }
        } else {
            param3 = new Fox.FoxGroupData(var1);
        }

        this.setFoxType(var1);
        if (var2) {
            this.setAge(-24000);
        }

        if (param0 instanceof ServerLevel) {
            this.setTargetGoals();
        }

        this.populateDefaultEquipmentSlots(param1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    private void setTargetGoals() {
        if (this.getFoxType() == Fox.Type.RED) {
            this.targetSelector.addGoal(4, this.landTargetGoal);
            this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
            this.targetSelector.addGoal(6, this.fishTargetGoal);
        } else {
            this.targetSelector.addGoal(4, this.fishTargetGoal);
            this.targetSelector.addGoal(6, this.landTargetGoal);
            this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
        }

    }

    @Override
    protected void usePlayerItem(Player param0, InteractionHand param1, ItemStack param2) {
        if (this.isFood(param2)) {
            this.playSound(this.getEatingSound(param2), 1.0F, 1.0F);
        }

        super.usePlayerItem(param0, param1, param2);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? param1.height * 0.85F : 0.4F;
    }

    public Fox.Type getFoxType() {
        return Fox.Type.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setFoxType(Fox.Type param0) {
        this.entityData.set(DATA_TYPE_ID, param0.getId());
    }

    List<UUID> getTrustedUUIDs() {
        List<UUID> var0 = Lists.newArrayList();
        var0.add(this.entityData.get(DATA_TRUSTED_ID_0).orElse(null));
        var0.add(this.entityData.get(DATA_TRUSTED_ID_1).orElse(null));
        return var0;
    }

    void addTrustedUUID(@Nullable UUID param0) {
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(param0));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(param0));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        List<UUID> var0 = this.getTrustedUUIDs();
        ListTag var1 = new ListTag();

        for(UUID var2 : var0) {
            if (var2 != null) {
                var1.add(NbtUtils.createUUID(var2));
            }
        }

        param0.put("Trusted", var1);
        param0.putBoolean("Sleeping", this.isSleeping());
        param0.putString("Type", this.getFoxType().getName());
        param0.putBoolean("Sitting", this.isSitting());
        param0.putBoolean("Crouching", this.isCrouching());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        ListTag var0 = param0.getList("Trusted", 11);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            this.addTrustedUUID(NbtUtils.loadUUID(var0.get(var1)));
        }

        this.setSleeping(param0.getBoolean("Sleeping"));
        this.setFoxType(Fox.Type.byName(param0.getString("Type")));
        this.setSitting(param0.getBoolean("Sitting"));
        this.setIsCrouching(param0.getBoolean("Crouching"));
        if (this.level instanceof ServerLevel) {
            this.setTargetGoals();
        }

    }

    public boolean isSitting() {
        return this.getFlag(1);
    }

    public void setSitting(boolean param0) {
        this.setFlag(1, param0);
    }

    public boolean isFaceplanted() {
        return this.getFlag(64);
    }

    void setFaceplanted(boolean param0) {
        this.setFlag(64, param0);
    }

    boolean isDefending() {
        return this.getFlag(128);
    }

    void setDefending(boolean param0) {
        this.setFlag(128, param0);
    }

    @Override
    public boolean isSleeping() {
        return this.getFlag(32);
    }

    void setSleeping(boolean param0) {
        this.setFlag(32, param0);
    }

    private void setFlag(int param0, boolean param1) {
        if (param1) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | param0));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~param0));
        }

    }

    private boolean getFlag(int param0) {
        return (this.entityData.get(DATA_FLAGS_ID) & param0) != 0;
    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        if (!this.getItemBySlot(var0).isEmpty()) {
            return false;
        } else {
            return var0 == EquipmentSlot.MAINHAND && super.canTakeItem(param0);
        }
    }

    @Override
    public boolean canHoldItem(ItemStack param0) {
        Item var0 = param0.getItem();
        ItemStack var1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return var1.isEmpty() || this.ticksSinceEaten > 0 && var0.isEdible() && !var1.getItem().isEdible();
    }

    private void spitOutItem(ItemStack param0) {
        if (!param0.isEmpty() && !this.level.isClientSide) {
            ItemEntity var0 = new ItemEntity(this.level, this.getX() + this.getLookAngle().x, this.getY() + 1.0, this.getZ() + this.getLookAngle().z, param0);
            var0.setPickUpDelay(40);
            var0.setThrower(this.getUUID());
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level.addFreshEntity(var0);
        }
    }

    private void dropItemStack(ItemStack param0) {
        ItemEntity var0 = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), param0);
        this.level.addFreshEntity(var0);
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        if (this.canHoldItem(var0)) {
            int var1 = var0.getCount();
            if (var1 > 1) {
                this.dropItemStack(var0.split(var1 - 1));
            }

            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.onItemPickup(param0);
            this.setItemSlot(EquipmentSlot.MAINHAND, var0.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(param0, var0.getCount());
            param0.discard();
            this.ticksSinceEaten = 0;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isEffectiveAi()) {
            boolean var0 = this.isInWater();
            if (var0 || this.getTarget() != null || this.level.isThundering()) {
                this.wakeUp();
            }

            if (var0 || this.isSleeping()) {
                this.setSitting(false);
            }

            if (this.isFaceplanted() && this.level.random.nextFloat() < 0.2F) {
                BlockPos var1 = this.blockPosition();
                BlockState var2 = this.level.getBlockState(var1);
                this.level.levelEvent(2001, var1, Block.getId(var2));
            }
        }

        this.interestedAngleO = this.interestedAngle;
        if (this.isInterested()) {
            this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
        } else {
            this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
        }

        this.crouchAmountO = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2F;
            if (this.crouchAmount > 3.0F) {
                this.crouchAmount = 3.0F;
            }
        } else {
            this.crouchAmount = 0.0F;
        }

    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.is(ItemTags.FOX_FOOD);
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player param0, Mob param1) {
        ((Fox)param1).addTrustedUUID(param0.getUUID());
    }

    public boolean isPouncing() {
        return this.getFlag(16);
    }

    public void setIsPouncing(boolean param0) {
        this.setFlag(16, param0);
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0F;
    }

    public void setIsCrouching(boolean param0) {
        this.setFlag(4, param0);
    }

    @Override
    public boolean isCrouching() {
        return this.getFlag(4);
    }

    public void setIsInterested(boolean param0) {
        this.setFlag(8, param0);
    }

    public boolean isInterested() {
        return this.getFlag(8);
    }

    public float getHeadRollAngle(float param0) {
        return Mth.lerp(param0, this.interestedAngleO, this.interestedAngle) * 0.11F * (float) Math.PI;
    }

    public float getCrouchAmount(float param0) {
        return Mth.lerp(param0, this.crouchAmountO, this.crouchAmount);
    }

    @Override
    public void setTarget(@Nullable LivingEntity param0) {
        if (this.isDefending() && param0 == null) {
            this.setDefending(false);
        }

        super.setTarget(param0);
    }

    @Override
    protected int calculateFallDamage(float param0, float param1) {
        return Mth.ceil((param0 - 5.0F) * param1);
    }

    void wakeUp() {
        this.setSleeping(false);
    }

    void clearStates() {
        this.setIsInterested(false);
        this.setIsCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setDefending(false);
        this.setFaceplanted(false);
    }

    boolean canMove() {
        return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent var0 = this.getAmbientSound();
        if (var0 == SoundEvents.FOX_SCREECH) {
            this.playSound(var0, 2.0F, this.getVoicePitch());
        } else {
            super.playAmbientSound();
        }

    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return SoundEvents.FOX_SLEEP;
        } else {
            if (!this.level.isDay() && this.random.nextFloat() < 0.1F) {
                List<Player> var0 = this.level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0, 16.0, 16.0), EntitySelector.NO_SPECTATORS);
                if (var0.isEmpty()) {
                    return SoundEvents.FOX_SCREECH;
                }
            }

            return SoundEvents.FOX_AMBIENT;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.FOX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    boolean trusts(UUID param0) {
        return this.getTrustedUUIDs().contains(param0);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource param0) {
        ItemStack var0 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!var0.isEmpty()) {
            this.spawnAtLocation(var0);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        super.dropAllDeathLoot(param0);
    }

    public static boolean isPathClear(Fox param0, LivingEntity param1) {
        double var0 = param1.getZ() - param0.getZ();
        double var1 = param1.getX() - param0.getX();
        double var2 = var0 / var1;
        int var3 = 6;

        for(int var4 = 0; var4 < 6; ++var4) {
            double var5 = var2 == 0.0 ? 0.0 : var0 * (double)((float)var4 / 6.0F);
            double var6 = var2 == 0.0 ? var1 * (double)((float)var4 / 6.0F) : var5 / var2;

            for(int var7 = 1; var7 < 4; ++var7) {
                if (!param0.level
                    .getBlockState(new BlockPos(param0.getX() + var6, param0.getY() + (double)var7, param0.getZ() + var5))
                    .getMaterial()
                    .isReplaceable()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    class DefendTrustedTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
        @Nullable
        private LivingEntity trustedLastHurtBy;
        @Nullable
        private LivingEntity trustedLastHurt;
        private int timestamp;

        public DefendTrustedTargetGoal(Class<LivingEntity> param0, boolean param1, @Nullable boolean param2, Predicate<LivingEntity> param3) {
            super(Fox.this, param0, 10, param1, param2, param3);
        }

        @Override
        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                for(UUID var0 : Fox.this.getTrustedUUIDs()) {
                    if (var0 != null && Fox.this.level instanceof ServerLevel) {
                        Entity var1 = ((ServerLevel)Fox.this.level).getEntity(var0);
                        if (var1 instanceof LivingEntity var2) {
                            this.trustedLastHurt = var2;
                            this.trustedLastHurtBy = var2.getLastHurtByMob();
                            int var3 = var2.getLastHurtByMobTimestamp();
                            return var3 != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
                        }
                    }
                }

                return false;
            }
        }

        @Override
        public void start() {
            this.setTarget(this.trustedLastHurtBy);
            this.target = this.trustedLastHurtBy;
            if (this.trustedLastHurt != null) {
                this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
            }

            Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
            Fox.this.setDefending(true);
            Fox.this.wakeUp();
            super.start();
        }
    }

    class FaceplantGoal extends Goal {
        int countdown;

        public FaceplantGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Fox.this.isFaceplanted();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.countdown > 0;
        }

        @Override
        public void start() {
            this.countdown = this.adjustedTickDelay(40);
        }

        @Override
        public void stop() {
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void tick() {
            --this.countdown;
        }
    }

    public class FoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
        public boolean test(LivingEntity param0) {
            if (param0 instanceof Fox) {
                return false;
            } else if (param0 instanceof Chicken || param0 instanceof Rabbit || param0 instanceof Monster) {
                return true;
            } else if (param0 instanceof TamableAnimal) {
                return !((TamableAnimal)param0).isTame();
            } else if (!(param0 instanceof Player) || !param0.isSpectator() && !((Player)param0).isCreative()) {
                if (Fox.this.trusts(param0.getUUID())) {
                    return false;
                } else {
                    return !param0.isSleeping() && !param0.isDiscrete();
                }
            } else {
                return false;
            }
        }
    }

    abstract class FoxBehaviorGoal extends Goal {
        private final TargetingConditions alertableTargeting = TargetingConditions.forCombat()
            .range(12.0)
            .ignoreLineOfSight()
            .selector(Fox.this.new FoxAlertableEntitiesSelector());

        protected boolean hasShelter() {
            BlockPos var0 = new BlockPos(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
            return !Fox.this.level.canSeeSky(var0) && Fox.this.getWalkTargetValue(var0) >= 0.0F;
        }

        protected boolean alertable() {
            return !Fox.this.level
                .getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0, 6.0, 12.0))
                .isEmpty();
        }
    }

    class FoxBreedGoal extends BreedGoal {
        public FoxBreedGoal(double param0) {
            super(Fox.this, param0);
        }

        @Override
        public void start() {
            ((Fox)this.animal).clearStates();
            ((Fox)this.partner).clearStates();
            super.start();
        }

        @Override
        protected void breed() {
            ServerLevel var0 = (ServerLevel)this.level;
            Fox var1 = (Fox)this.animal.getBreedOffspring(var0, this.partner);
            if (var1 != null) {
                ServerPlayer var2 = this.animal.getLoveCause();
                ServerPlayer var3 = this.partner.getLoveCause();
                ServerPlayer var4 = var2;
                if (var2 != null) {
                    var1.addTrustedUUID(var2.getUUID());
                } else {
                    var4 = var3;
                }

                if (var3 != null && var2 != var3) {
                    var1.addTrustedUUID(var3.getUUID());
                }

                if (var4 != null) {
                    var4.awardStat(Stats.ANIMALS_BRED);
                    CriteriaTriggers.BRED_ANIMALS.trigger(var4, this.animal, this.partner, var1);
                }

                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                var1.setAge(-24000);
                var1.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
                var0.addFreshEntityWithPassengers(var1);
                this.level.broadcastEntityEvent(this.animal, (byte)18);
                if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    this.level
                        .addFreshEntity(
                            new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1)
                        );
                }

            }
        }
    }

    public class FoxEatBerriesGoal extends MoveToBlockGoal {
        private static final int WAIT_TICKS = 40;
        protected int ticksWaited;

        public FoxEatBerriesGoal(double param1, int param2, int param3) {
            super(Fox.this, param1, param2, param3);
        }

        @Override
        public double acceptedDistance() {
            return 2.0;
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            BlockState var0 = param0.getBlockState(param1);
            return var0.is(Blocks.SWEET_BERRY_BUSH) && var0.getValue(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasGlowBerries(var0);
        }

        @Override
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && Fox.this.random.nextFloat() < 0.05F) {
                Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
            }

            super.tick();
        }

        protected void onReachedTarget() {
            if (Fox.this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockState var0 = Fox.this.level.getBlockState(this.blockPos);
                if (var0.is(Blocks.SWEET_BERRY_BUSH)) {
                    this.pickSweetBerries(var0);
                } else if (CaveVines.hasGlowBerries(var0)) {
                    this.pickGlowBerry(var0);
                }

            }
        }

        private void pickGlowBerry(BlockState param0) {
            CaveVines.use(param0, Fox.this.level, this.blockPos);
        }

        private void pickSweetBerries(BlockState param0) {
            int var0 = param0.getValue(SweetBerryBushBlock.AGE);
            param0.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1));
            int var1 = 1 + Fox.this.level.random.nextInt(2) + (var0 == 3 ? 1 : 0);
            ItemStack var2 = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (var2.isEmpty()) {
                Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                --var1;
            }

            if (var1 > 0) {
                Block.popResource(Fox.this.level, this.blockPos, new ItemStack(Items.SWEET_BERRIES, var1));
            }

            Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
            Fox.this.level.setBlock(this.blockPos, param0.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 2);
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSleeping() && super.canUse();
        }

        @Override
        public void start() {
            this.ticksWaited = 0;
            Fox.this.setSitting(false);
            super.start();
        }
    }

    class FoxFloatGoal extends FloatGoal {
        public FoxFloatGoal() {
            super(Fox.this);
        }

        @Override
        public void start() {
            super.start();
            Fox.this.clearStates();
        }

        @Override
        public boolean canUse() {
            return Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25 || Fox.this.isInLava();
        }
    }

    class FoxFollowParentGoal extends FollowParentGoal {
        private final Fox fox;

        public FoxFollowParentGoal(Fox param0, double param1) {
            super(param0, param1);
            this.fox = param0;
        }

        @Override
        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        @Override
        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    public static class FoxGroupData extends AgeableMob.AgeableMobGroupData {
        public final Fox.Type type;

        public FoxGroupData(Fox.Type param0) {
            super(false);
            this.type = param0;
        }
    }

    class FoxLookAtPlayerGoal extends LookAtPlayerGoal {
        public FoxLookAtPlayerGoal(Mob param0, Class<? extends LivingEntity> param1, float param2) {
            super(param0, param1, param2);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }
    }

    public class FoxLookControl extends LookControl {
        public FoxLookControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (!Fox.this.isSleeping()) {
                super.tick();
            }

        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Fox.this.isPouncing() && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.isFaceplanted();
        }
    }

    class FoxMeleeAttackGoal extends MeleeAttackGoal {
        public FoxMeleeAttackGoal(double param0, boolean param1) {
            super(Fox.this, param0, param1);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity param0, double param1) {
            double var0 = this.getAttackReachSqr(param0);
            if (param1 <= var0 && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(param0);
                Fox.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
            }

        }

        @Override
        public void start() {
            Fox.this.setIsInterested(false);
            super.start();
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSitting() && !Fox.this.isSleeping() && !Fox.this.isCrouching() && !Fox.this.isFaceplanted() && super.canUse();
        }
    }

    class FoxMoveControl extends MoveControl {
        public FoxMoveControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (Fox.this.canMove()) {
                super.tick();
            }

        }
    }

    class FoxPanicGoal extends PanicGoal {
        public FoxPanicGoal(double param0) {
            super(Fox.this, param0);
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isDefending() && super.canUse();
        }
    }

    public class FoxPounceGoal extends JumpGoal {
        @Override
        public boolean canUse() {
            if (!Fox.this.isFullyCrouched()) {
                return false;
            } else {
                LivingEntity var0 = Fox.this.getTarget();
                if (var0 != null && var0.isAlive()) {
                    if (var0.getMotionDirection() != var0.getDirection()) {
                        return false;
                    } else {
                        boolean var1 = Fox.isPathClear(Fox.this, var0);
                        if (!var1) {
                            Fox.this.getNavigation().createPath(var0, 0);
                            Fox.this.setIsCrouching(false);
                            Fox.this.setIsInterested(false);
                        }

                        return var1;
                    }
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity var0 = Fox.this.getTarget();
            if (var0 != null && var0.isAlive()) {
                double var1 = Fox.this.getDeltaMovement().y;
                return (!(var1 * var1 < 0.05F) || !(Math.abs(Fox.this.getXRot()) < 15.0F) || !Fox.this.onGround) && !Fox.this.isFaceplanted();
            } else {
                return false;
            }
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            Fox.this.setJumping(true);
            Fox.this.setIsPouncing(true);
            Fox.this.setIsInterested(false);
            LivingEntity var0 = Fox.this.getTarget();
            if (var0 != null) {
                Fox.this.getLookControl().setLookAt(var0, 60.0F, 30.0F);
                Vec3 var1 = new Vec3(var0.getX() - Fox.this.getX(), var0.getY() - Fox.this.getY(), var0.getZ() - Fox.this.getZ()).normalize();
                Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add(var1.x * 0.8, 0.9, var1.z * 0.8));
            }

            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setIsCrouching(false);
            Fox.this.crouchAmount = 0.0F;
            Fox.this.crouchAmountO = 0.0F;
            Fox.this.setIsInterested(false);
            Fox.this.setIsPouncing(false);
        }

        @Override
        public void tick() {
            LivingEntity var0 = Fox.this.getTarget();
            if (var0 != null) {
                Fox.this.getLookControl().setLookAt(var0, 60.0F, 30.0F);
            }

            if (!Fox.this.isFaceplanted()) {
                Vec3 var1 = Fox.this.getDeltaMovement();
                if (var1.y * var1.y < 0.03F && Fox.this.getXRot() != 0.0F) {
                    Fox.this.setXRot(Mth.rotlerp(Fox.this.getXRot(), 0.0F, 0.2F));
                } else {
                    double var2 = var1.horizontalDistance();
                    double var3 = Math.signum(-var1.y) * Math.acos(var2 / var1.length()) * 180.0F / (float)Math.PI;
                    Fox.this.setXRot((float)var3);
                }
            }

            if (var0 != null && Fox.this.distanceTo(var0) <= 2.0F) {
                Fox.this.doHurtTarget(var0);
            } else if (Fox.this.getXRot() > 0.0F
                && Fox.this.onGround
                && (float)Fox.this.getDeltaMovement().y != 0.0F
                && Fox.this.level.getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
                Fox.this.setXRot(60.0F);
                Fox.this.setTarget(null);
                Fox.this.setFaceplanted(true);
            }

        }
    }

    class FoxSearchForItemsGoal extends Goal {
        public FoxSearchForItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else if (Fox.this.getTarget() != null || Fox.this.getLastHurtByMob() != null) {
                return false;
            } else if (!Fox.this.canMove()) {
                return false;
            } else if (Fox.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
                return false;
            } else {
                List<ItemEntity> var0 = Fox.this.level
                    .getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
                return !var0.isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            }
        }

        @Override
        public void tick() {
            List<ItemEntity> var0 = Fox.this.level.getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
            ItemStack var1 = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (var1.isEmpty() && !var0.isEmpty()) {
                Fox.this.getNavigation().moveTo(var0.get(0), 1.2F);
            }

        }

        @Override
        public void start() {
            List<ItemEntity> var0 = Fox.this.level.getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
            if (!var0.isEmpty()) {
                Fox.this.getNavigation().moveTo(var0.get(0), 1.2F);
            }

        }
    }

    class FoxStrollThroughVillageGoal extends StrollThroughVillageGoal {
        public FoxStrollThroughVillageGoal(int param0, int param1) {
            super(Fox.this, param1);
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.canFoxMove();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.canFoxMove();
        }

        private boolean canFoxMove() {
            return !Fox.this.isSleeping() && !Fox.this.isSitting() && !Fox.this.isDefending() && Fox.this.getTarget() == null;
        }
    }

    class PerchAndSearchGoal extends Fox.FoxBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return Fox.this.getLastHurtByMob() == null
                && Fox.this.getRandom().nextFloat() < 0.02F
                && !Fox.this.isSleeping()
                && Fox.this.getTarget() == null
                && Fox.this.getNavigation().isDone()
                && !this.alertable()
                && !Fox.this.isPouncing()
                && !Fox.this.isCrouching();
        }

        @Override
        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        @Override
        public void start() {
            this.resetLook();
            this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
            Fox.this.setSitting(true);
            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setSitting(false);
        }

        @Override
        public void tick() {
            --this.lookTime;
            if (this.lookTime <= 0) {
                --this.looksRemaining;
                this.resetLook();
            }

            Fox.this.getLookControl()
                .setLookAt(
                    Fox.this.getX() + this.relX,
                    Fox.this.getEyeY(),
                    Fox.this.getZ() + this.relZ,
                    (float)Fox.this.getMaxHeadYRot(),
                    (float)Fox.this.getMaxHeadXRot()
                );
        }

        private void resetLook() {
            double var0 = (Math.PI * 2) * Fox.this.getRandom().nextDouble();
            this.relX = Math.cos(var0);
            this.relZ = Math.sin(var0);
            this.lookTime = this.adjustedTickDelay(80 + Fox.this.getRandom().nextInt(20));
        }
    }

    class SeekShelterGoal extends FleeSunGoal {
        private int interval = reducedTickDelay(100);

        public SeekShelterGoal(double param0) {
            super(Fox.this, param0);
        }

        @Override
        public boolean canUse() {
            if (Fox.this.isSleeping() || this.mob.getTarget() != null) {
                return false;
            } else if (Fox.this.level.isThundering()) {
                return true;
            } else if (this.interval > 0) {
                --this.interval;
                return false;
            } else {
                this.interval = 100;
                BlockPos var0 = this.mob.blockPosition();
                return Fox.this.level.isDay() && Fox.this.level.canSeeSky(var0) && !((ServerLevel)Fox.this.level).isVillage(var0) && this.setWantedPos();
            }
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }
    }

    class SleepGoal extends Fox.FoxBehaviorGoal {
        private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
        private int countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);

        public SleepGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (Fox.this.xxa == 0.0F && Fox.this.yya == 0.0F && Fox.this.zza == 0.0F) {
                return this.canSleep() || Fox.this.isSleeping();
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return Fox.this.level.isDay() && this.hasShelter() && !this.alertable() && !Fox.this.isInPowderSnow;
            }
        }

        @Override
        public void stop() {
            this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            Fox.this.clearStates();
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setIsCrouching(false);
            Fox.this.setIsInterested(false);
            Fox.this.setJumping(false);
            Fox.this.setSleeping(true);
            Fox.this.getNavigation().stop();
            Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0);
        }
    }

    class StalkPreyGoal extends Goal {
        public StalkPreyGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (Fox.this.isSleeping()) {
                return false;
            } else {
                LivingEntity var0 = Fox.this.getTarget();
                return var0 != null
                    && var0.isAlive()
                    && Fox.STALKABLE_PREY.test(var0)
                    && Fox.this.distanceToSqr(var0) > 36.0
                    && !Fox.this.isCrouching()
                    && !Fox.this.isInterested()
                    && !Fox.this.jumping;
            }
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void stop() {
            LivingEntity var0 = Fox.this.getTarget();
            if (var0 != null && Fox.isPathClear(Fox.this, var0)) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
                Fox.this.getLookControl().setLookAt(var0, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
            } else {
                Fox.this.setIsInterested(false);
                Fox.this.setIsCrouching(false);
            }

        }

        @Override
        public void tick() {
            LivingEntity var0 = Fox.this.getTarget();
            if (var0 != null) {
                Fox.this.getLookControl().setLookAt(var0, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
                if (Fox.this.distanceToSqr(var0) <= 36.0) {
                    Fox.this.setIsInterested(true);
                    Fox.this.setIsCrouching(true);
                    Fox.this.getNavigation().stop();
                } else {
                    Fox.this.getNavigation().moveTo(var0, 1.5);
                }

            }
        }
    }

    public static enum Type {
        RED(0, "red", Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA),
        SNOW(1, "snow", Biomes.SNOWY_TAIGA);

        private static final Fox.Type[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(Fox.Type::getId))
            .toArray(param0 -> new Fox.Type[param0]);
        private static final Map<String, Fox.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Fox.Type::getName, param0 -> param0));
        private final int id;
        private final String name;
        private final List<ResourceKey<Biome>> biomes;

        private Type(int param0, String param1, ResourceKey<Biome>... param2) {
            this.id = param0;
            this.name = param1;
            this.biomes = Arrays.asList(param2);
        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static Fox.Type byName(String param0) {
            return BY_NAME.getOrDefault(param0, RED);
        }

        public static Fox.Type byId(int param0) {
            if (param0 < 0 || param0 > BY_ID.length) {
                param0 = 0;
            }

            return BY_ID[param0];
        }

        public static Fox.Type byBiome(Optional<ResourceKey<Biome>> param0) {
            return param0.isPresent() && SNOW.biomes.contains(param0.get()) ? SNOW : RED;
        }
    }
}
