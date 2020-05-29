package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class Piglin extends Monster implements CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
        SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.2F, AttributeModifier.Operation.MULTIPLY_BASE
    );
    private int timeInOverworld = 0;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt = false;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.NEAREST_PLAYERS,
        SensorType.NEAREST_ITEMS,
        SensorType.HURT_BY,
        SensorType.INTERACTABLE_DOORS,
        SensorType.PIGLIN_SPECIFIC_SENSOR
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.INTERACTABLE_DOORS,
        MemoryModuleType.OPENED_DOORS,
        MemoryModuleType.LIVING_ENTITIES,
        MemoryModuleType.VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
        MemoryModuleType.NEAREST_ADULT_PIGLINS,
        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.INTERACTION_TARGET,
        MemoryModuleType.PATH,
        MemoryModuleType.ANGRY_AT,
        MemoryModuleType.AVOID_TARGET,
        MemoryModuleType.ADMIRING_ITEM,
        MemoryModuleType.ADMIRING_DISABLED,
        MemoryModuleType.CELEBRATE_LOCATION,
        MemoryModuleType.DANCING,
        MemoryModuleType.HUNTED_RECENTLY,
        MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
        MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN,
        MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON,
        MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,
        MemoryModuleType.RIDE_TARGET,
        MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
        MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
        MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
        MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
        MemoryModuleType.ATE_RECENTLY,
        MemoryModuleType.NEAREST_REPELLENT
    );

    public Piglin(EntityType<? extends Monster> param0, Level param1) {
        super(param0, param1);
        this.setCanPickUpLoot(true);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        this.xpReward = 5;
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.isBaby()) {
            param0.putBoolean("IsBaby", true);
        }

        if (this.isImmuneToZombification()) {
            param0.putBoolean("IsImmuneToZombification", true);
        }

        if (this.cannotHunt) {
            param0.putBoolean("CannotHunt", true);
        }

        param0.putInt("TimeInOverworld", this.timeInOverworld);
        param0.put("Inventory", this.inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setBaby(param0.getBoolean("IsBaby"));
        this.setImmuneToZombification(param0.getBoolean("IsImmuneToZombification"));
        this.setCannotHunt(param0.getBoolean("CannotHunt"));
        this.timeInOverworld = param0.getInt("TimeInOverworld");
        this.inventory.fromTag(param0.getList("Inventory", 10));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
    }

    protected ItemStack addToInventory(ItemStack param0) {
        return this.inventory.addItem(param0);
    }

    protected boolean canAddToInventory(ItemStack param0) {
        return this.inventory.canAddItem(param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
        this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
        this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
        this.entityData.define(DATA_IS_DANCING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_BABY_ID.equals(param0)) {
            this.refreshDimensions();
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.35F).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return !param1.getBlockState(param3.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param2 != MobSpawnType.STRUCTURE) {
            if (param0.getRandom().nextFloat() < 0.2F) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }

        PiglinAi.initMemories(this);
        this.populateDefaultEquipmentSlots(param1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        if (this.isAdult()) {
            this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }

    }

    private void maybeWearArmor(EquipmentSlot param0, ItemStack param1) {
        if (this.level.random.nextFloat() < 0.1F) {
            this.setItemSlot(param0, param1);
        }

    }

    @Override
    protected Brain.Provider<Piglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return PiglinAi.makeBrain(this, this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Piglin> getBrain() {
        return super.getBrain();
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        if (super.mobInteract(param0, param1)) {
            return true;
        } else if (!this.level.isClientSide) {
            return PiglinAi.mobInteract(this, param0, param1);
        } else {
            return PiglinAi.canAdmire(this, param0.getItemInHand(param1)) && this.getArmPose() != Piglin.PiglinArmPose.ADMIRING_ITEM;
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? 0.93F : 1.74F;
    }

    @Override
    public double getMyRidingOffset() {
        return this.isBaby() ? -0.1 : -0.45;
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getBbHeight() * 0.92;
    }

    @Override
    public void setBaby(boolean param0) {
        this.getEntityData().set(DATA_BABY_ID, param0);
        if (!this.level.isClientSide) {
            AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            var0.removeModifier(SPEED_MODIFIER_BABY);
            if (param0) {
                var0.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }

    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public void setImmuneToZombification(boolean param0) {
        this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, param0);
    }

    private boolean isImmuneToZombification() {
        return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    private void setCannotHunt(boolean param0) {
        this.cannotHunt = param0;
    }

    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    public boolean isConverting() {
        return !this.level.dimensionType().isNether() && !this.isImmuneToZombification() && !this.isNoAi();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("piglinBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        PiglinAi.updateActivity(this);
        PiglinAi.maybePlayActivitySound(this);
        if (this.isConverting()) {
            ++this.timeInOverworld;
        } else {
            this.timeInOverworld = 0;
        }

        if (this.timeInOverworld > 300) {
            this.playConvertedSound();
            this.finishConversion((ServerLevel)this.level);
        }

    }

    @Override
    protected int getExperienceReward(Player param0) {
        return this.xpReward;
    }

    private void finishConversion(ServerLevel param0) {
        PiglinAi.cancelAdmiring(this);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
        ZombifiedPiglin var0 = this.convertTo(EntityType.ZOMBIFIED_PIGLIN);
        var0.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private ItemStack createSpawnWeapon() {
        return (double)this.random.nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
    }

    private boolean isChargingCrossbow() {
        return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean param0) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, param0);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public Piglin.PiglinArmPose getArmPose() {
        if (this.isDancing()) {
            return Piglin.PiglinArmPose.DANCING;
        } else if (this.swinging) {
            return Piglin.PiglinArmPose.DEFAULT;
        } else if (PiglinAi.isLovedItem(this.getOffhandItem().getItem())) {
            return Piglin.PiglinArmPose.ADMIRING_ITEM;
        } else if (this.isChargingCrossbow()) {
            return Piglin.PiglinArmPose.CROSSBOW_CHARGE;
        } else if (this.isAggressive() && this.isHolding(Items.CROSSBOW)) {
            return Piglin.PiglinArmPose.CROSSBOW_HOLD;
        } else {
            return this.isAggressive() && this.isHoldingMeleeWeapon() ? Piglin.PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON : Piglin.PiglinArmPose.DEFAULT;
        }
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_IS_DANCING);
    }

    public void setDancing(boolean param0) {
        this.entityData.set(DATA_IS_DANCING, param0);
    }

    private boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().getItem() instanceof TieredItem;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        boolean var0 = super.hurt(param0, param1);
        if (this.level.isClientSide) {
            return false;
        } else {
            if (var0 && param0.getEntity() instanceof LivingEntity) {
                PiglinAi.wasHurtBy(this, (LivingEntity)param0.getEntity());
            }

            return var0;
        }
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        this.performCrossbowAttack(this, 1.6F);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity param0, ItemStack param1, Projectile param2, float param3) {
        this.shootCrossbowProjectile(this, param0, param2, param3, 1.6F);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem param0) {
        return param0 == Items.CROSSBOW;
    }

    protected void holdInMainHand(ItemStack param0) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, param0);
    }

    protected void holdInOffHand(ItemStack param0) {
        if (param0.getItem() == PiglinAi.BARTERING_ITEM) {
            this.setItemSlot(EquipmentSlot.OFFHAND, param0);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, param0);
        }

    }

    @Override
    public boolean wantsToPickUp(ItemStack param0) {
        return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && PiglinAi.wantsToPickup(this, param0);
    }

    protected boolean canReplaceCurrentItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        ItemStack var1 = this.getItemBySlot(var0);
        return this.canReplaceCurrentItem(param0, var1);
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack param0, ItemStack param1) {
        boolean var0 = PiglinAi.isLovedItem(param0.getItem()) || param0.getItem() == Items.CROSSBOW;
        boolean var1 = PiglinAi.isLovedItem(param1.getItem()) || param1.getItem() == Items.CROSSBOW;
        if (var0 && !var1) {
            return true;
        } else if (!var0 && var1) {
            return false;
        } else {
            return this.isAdult() && param0.getItem() != Items.CROSSBOW && param1.getItem() == Items.CROSSBOW
                ? false
                : super.canReplaceCurrentItem(param0, param1);
        }
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        this.onItemPickup(param0);
        PiglinAi.pickUpItem(this, param0);
    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        if (this.isBaby() && param0.getType() == EntityType.HOGLIN) {
            param0 = this.getTopPassenger(param0, 3);
        }

        return super.startRiding(param0, param1);
    }

    private Entity getTopPassenger(Entity param0, int param1) {
        List<Entity> var0 = param0.getPassengers();
        return param1 != 1 && !var0.isEmpty() ? this.getTopPassenger(var0.get(0), param1 - 1) : param0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
    }

    protected void playAdmiringSound() {
        this.playSound(SoundEvents.PIGLIN_ADMIRING_ITEM, 1.0F, this.getVoicePitch());
    }

    @Override
    public void playAmbientSound() {
        if (PiglinAi.isIdle(this)) {
            super.playAmbientSound();
        }

    }

    protected void playAngrySound() {
        this.playSound(SoundEvents.PIGLIN_ANGRY, 1.0F, this.getVoicePitch());
    }

    protected void playCelebrateSound() {
        this.playSound(SoundEvents.PIGLIN_CELEBRATE, 1.0F, this.getVoicePitch());
    }

    protected void playRetreatSound() {
        this.playSound(SoundEvents.PIGLIN_RETREAT, 1.0F, this.getVoicePitch());
    }

    protected void playJealousSound() {
        this.playSound(SoundEvents.PIGLIN_JEALOUS, 1.0F, this.getVoicePitch());
    }

    private void playConvertedSound() {
        this.playSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED, 1.0F, this.getVoicePitch());
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public static enum PiglinArmPose {
        ATTACKING_WITH_MELEE_WEAPON,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        ADMIRING_ITEM,
        DANCING,
        DEFAULT;
    }
}
