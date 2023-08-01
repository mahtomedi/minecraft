package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Piglin extends AbstractPiglin implements CrossbowAttackMob, InventoryCarrier {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
        SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.2F, AttributeModifier.Operation.MULTIPLY_BASE
    );
    private static final int MAX_HEALTH = 16;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
    private static final int ATTACK_DAMAGE = 5;
    private static final float CROSSBOW_POWER = 1.6F;
    private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1F;
    private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
    private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;
    private static final float BABY_EYE_HEIGHT_ADJUSTMENT = 0.82F;
    private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.DOORS_TO_CLOSE,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
        MemoryModuleType.NEARBY_ADULT_PIGLINS,
        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
        MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.INTERACTION_TARGET,
        MemoryModuleType.PATH,
        MemoryModuleType.ANGRY_AT,
        MemoryModuleType.UNIVERSAL_ANGER,
        MemoryModuleType.AVOID_TARGET,
        MemoryModuleType.ADMIRING_ITEM,
        MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM,
        MemoryModuleType.ADMIRING_DISABLED,
        MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM,
        MemoryModuleType.CELEBRATE_LOCATION,
        MemoryModuleType.DANCING,
        MemoryModuleType.HUNTED_RECENTLY,
        MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
        MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
        MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,
        MemoryModuleType.RIDE_TARGET,
        MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
        MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
        MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
        MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
        MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
        MemoryModuleType.ATE_RECENTLY,
        MemoryModuleType.NEAREST_REPELLENT
    );

    public Piglin(EntityType<? extends AbstractPiglin> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.isBaby()) {
            param0.putBoolean("IsBaby", true);
        }

        if (this.cannotHunt) {
            param0.putBoolean("CannotHunt", true);
        }

        this.writeInventoryToTag(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setBaby(param0.getBoolean("IsBaby"));
        this.setCannotHunt(param0.getBoolean("CannotHunt"));
        this.readInventoryFromTag(param0);
    }

    @VisibleForDebug
    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        Entity var0 = param0.getEntity();
        if (var0 instanceof Creeper var1 && var1.canDropMobsSkull()) {
            ItemStack var2 = new ItemStack(Items.PIGLIN_HEAD);
            var1.increaseDroppedSkulls();
            this.spawnAtLocation(var2);
        }

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

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        return !param1.getBlockState(param3.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        RandomSource var0 = param0.getRandom();
        if (param2 != MobSpawnType.STRUCTURE) {
            if (var0.nextFloat() < 0.2F) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }

        PiglinAi.initMemories(this, param0.getRandom());
        this.populateDefaultEquipmentSlots(var0, param1);
        this.populateDefaultEquipmentEnchantments(var0, param1);
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
    protected void populateDefaultEquipmentSlots(RandomSource param0, DifficultyInstance param1) {
        if (this.isAdult()) {
            this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), param0);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), param0);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), param0);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), param0);
        }

    }

    private void maybeWearArmor(EquipmentSlot param0, ItemStack param1, RandomSource param2) {
        if (param2.nextFloat() < 0.1F) {
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
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        InteractionResult var0 = super.mobInteract(param0, param1);
        if (var0.consumesAction()) {
            return var0;
        } else if (!this.level().isClientSide) {
            return PiglinAi.mobInteract(this, param0, param1);
        } else {
            boolean var1 = PiglinAi.canAdmire(this, param0.getItemInHand(param1)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
            return var1 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        float var0 = super.getStandingEyeHeight(param0, param1);
        return this.isBaby() ? var0 - 0.82F : var0;
    }

    @Override
    public void setBaby(boolean param0) {
        this.getEntityData().set(DATA_BABY_ID, param0);
        if (!this.level().isClientSide) {
            AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            var0.removeModifier(SPEED_MODIFIER_BABY.getId());
            if (param0) {
                var0.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }

    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    private void setCannotHunt(boolean param0) {
        this.cannotHunt = param0;
    }

    @Override
    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        PiglinAi.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public int getExperienceReward() {
        return this.xpReward;
    }

    @Override
    protected void finishConversion(ServerLevel param0) {
        PiglinAi.cancelAdmiring(this);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
        super.finishConversion(param0);
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

    @Override
    public PiglinArmPose getArmPose() {
        if (this.isDancing()) {
            return PiglinArmPose.DANCING;
        } else if (PiglinAi.isLovedItem(this.getOffhandItem())) {
            return PiglinArmPose.ADMIRING_ITEM;
        } else if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        } else if (this.isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        } else {
            return this.isAggressive() && this.isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
        }
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_IS_DANCING);
    }

    public void setDancing(boolean param0) {
        this.entityData.set(DATA_IS_DANCING, param0);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        boolean var0 = super.hurt(param0, param1);
        if (this.level().isClientSide) {
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
        if (param0.is(PiglinAi.BARTERING_ITEM)) {
            this.setItemSlot(EquipmentSlot.OFFHAND, param0);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, param0);
        }

    }

    @Override
    public boolean wantsToPickUp(ItemStack param0) {
        return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, param0);
    }

    protected boolean canReplaceCurrentItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        ItemStack var1 = this.getItemBySlot(var0);
        return this.canReplaceCurrentItem(param0, var1);
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack param0, ItemStack param1) {
        if (EnchantmentHelper.hasBindingCurse(param1)) {
            return false;
        } else {
            boolean var0 = PiglinAi.isLovedItem(param0) || param0.is(Items.CROSSBOW);
            boolean var1 = PiglinAi.isLovedItem(param1) || param1.is(Items.CROSSBOW);
            if (var0 && !var1) {
                return true;
            } else if (!var0 && var1) {
                return false;
            } else {
                return this.isAdult() && !param0.is(Items.CROSSBOW) && param1.is(Items.CROSSBOW) ? false : super.canReplaceCurrentItem(param0, param1);
            }
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
        return this.level().isClientSide ? null : PiglinAi.getSoundForCurrentActivity(this).orElse(null);
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

    protected void playSoundEvent(SoundEvent param0) {
        this.playSound(param0, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void playConvertedSound() {
        this.playSoundEvent(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}
