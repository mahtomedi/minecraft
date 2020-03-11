package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Piglin extends Monster implements CrossbowAttackMob {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
        SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.2F, AttributeModifier.Operation.MULTIPLY_BASE
    );
    private int timeInOverworld = 0;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private static int createCounter = 0;
    private static int dieCounter = 0;
    private static int killedByHoglinCounter = 0;
    private static int removeCounter = 0;
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
        MemoryModuleType.HUNTED_RECENTLY,
        MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
        MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN,
        MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN,
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
    }

    @Override
    public void die(DamageSource param0) {
        super.die(param0);
    }

    @Override
    public void remove() {
        super.remove();
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

        param0.putInt("TimeInOverworld", this.timeInOverworld);
        param0.put("Inventory", this.inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setBaby(param0.getBoolean("IsBaby"));
        this.setImmuneToZombification(param0.getBoolean("IsImmuneToZombification"));
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
        this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
        this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_BABY_ID.equals(param0)) {
            this.refreshDimensions();
        }

    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getBlockState(param3.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param0.getRandom().nextFloat() < 0.2F) {
            this.setBaby(true);
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
            this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
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
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return PiglinAi.makeBrain(this, param0);
    }

    @Override
    public Brain<Piglin> getBrain() {
        return super.getBrain();
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        if (super.mobInteract(param0, param1)) {
            return true;
        } else {
            return this.level.isClientSide ? false : PiglinAi.mobInteract(this, param0, param1);
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? 0.93F : 1.74F;
    }

    @Override
    public void setBaby(boolean param0) {
        this.getEntityData().set(DATA_BABY_ID, param0);
        if (!this.level.isClientSide) {
            AttributeInstance var0 = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            var0.removeModifier(SPEED_MODIFIER_BABY);
            if (param0) {
                var0.addModifier(SPEED_MODIFIER_BABY);
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

    private void setImmuneToZombification(boolean param0) {
        this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, param0);
    }

    private boolean isImmuneToZombification() {
        return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    public boolean isConverting() {
        return this.level.getDimension().getType() == DimensionType.OVERWORLD && !this.isImmuneToZombification() && !this.isNoAi();
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
        ZombifiedPiglin var0 = EntityType.ZOMBIFIED_PIGLIN.create(param0);
        if (var0 != null) {
            var0.copyPosition(this);
            var0.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var0.blockPosition()), MobSpawnType.CONVERSION, null, null);
            var0.setBaby(this.isBaby());
            var0.setNoAi(this.isNoAi());
            PiglinAi.cancelAdmiring(this);

            for(EquipmentSlot var1 : EquipmentSlot.values()) {
                if (!this.isAdult() || var1 != EquipmentSlot.MAINHAND) {
                    ItemStack var2 = this.getItemBySlot(var1);
                    if (!var2.isEmpty()) {
                        var0.setItemSlot(var1, var2.copy());
                        var0.setDropChance(var1, this.getEquipmentDropChance(var1));
                        var2.setCount(0);
                    }
                }
            }

            if (this.hasCustomName()) {
                var0.setCustomName(this.getCustomName());
                var0.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
                var0.setPersistenceRequired();
            }

            this.remove();
            param0.addFreshEntity(var0);
            var0.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private ItemStack createSpawnWeapon() {
        return (double)this.random.nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
    }

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    public Piglin.PiglinArmPose getArmPose() {
        if (this.swinging) {
            return Piglin.PiglinArmPose.DEFAULT;
        } else if (PiglinAi.isLovedItem(this.getOffhandItem().getItem())) {
            return Piglin.PiglinArmPose.ADMIRING_ITEM;
        } else if (this.isChargingCrossbow()) {
            return Piglin.PiglinArmPose.CROSSBOW_CHARGE;
        } else {
            return this.isHolding(Items.CROSSBOW) && this.isAggressive() ? Piglin.PiglinArmPose.CROSSBOW_HOLD : Piglin.PiglinArmPose.DEFAULT;
        }
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
        if (param0.getItem() == Items.GOLD_INGOT) {
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
        return this.canReplaceCurrentItem(param0, var1, var0);
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack param0, ItemStack param1, EquipmentSlot param2) {
        if (PiglinAi.isLovedItem(param1.getItem())) {
            return false;
        } else if (this.isAdult() && param1.getItem() == Items.CROSSBOW) {
            return false;
        } else {
            return PiglinAi.isLovedItem(param0.getItem()) ? true : super.canReplaceCurrentItem(param0, param1, param2);
        }
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        PiglinAi.pickUpItem(this, param0);
    }

    protected float getMovementSpeed() {
        return (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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

    @OnlyIn(Dist.CLIENT)
    public static enum PiglinArmPose {
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        ADMIRING_ITEM,
        DEFAULT;
    }
}
