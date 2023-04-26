package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Zombie extends Monster {
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
        SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE
    );
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    public static final float ZOMBIE_LEADER_CHANCE = 0.05F;
    public static final int REINFORCEMENT_ATTEMPTS = 50;
    public static final int REINFORCEMENT_RANGE_MAX = 40;
    public static final int REINFORCEMENT_RANGE_MIN = 7;
    protected static final float BABY_EYE_HEIGHT_ADJUSTMENT = 0.81F;
    private static final float BREAK_DOOR_CHANCE = 0.1F;
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = param0 -> param0 == Difficulty.HARD;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
    private boolean canBreakDoors;
    private int inWaterTime;
    private int conversionTime;

    public Zombie(EntityType<? extends Zombie> param0, Level param1) {
        super(param0, param1);
    }

    public Zombie(Level param0) {
        this(EntityType.ZOMBIE, param0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new Zombie.ZombieAttackTurtleEggGoal(this, 1.0, 3));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 35.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23F)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_BABY_ID, false);
        this.getEntityData().define(DATA_SPECIAL_TYPE_ID, 0);
        this.getEntityData().define(DATA_DROWNED_CONVERSION_ID, false);
    }

    public boolean isUnderWaterConverting() {
        return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    public void setCanBreakDoors(boolean param0) {
        if (this.supportsBreakDoorGoal() && GoalUtils.hasGroundPathNavigation(this)) {
            if (this.canBreakDoors != param0) {
                this.canBreakDoors = param0;
                ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(param0);
                if (param0) {
                    this.goalSelector.addGoal(1, this.breakDoorGoal);
                } else {
                    this.goalSelector.removeGoal(this.breakDoorGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.removeGoal(this.breakDoorGoal);
            this.canBreakDoors = false;
        }

    }

    protected boolean supportsBreakDoorGoal() {
        return true;
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    @Override
    public int getExperienceReward() {
        if (this.isBaby()) {
            this.xpReward = (int)((double)this.xpReward * 2.5);
        }

        return super.getExperienceReward();
    }

    @Override
    public void setBaby(boolean param0) {
        this.getEntityData().set(DATA_BABY_ID, param0);
        if (this.level() != null && !this.level().isClientSide) {
            AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            var0.removeModifier(SPEED_MODIFIER_BABY);
            if (param0) {
                var0.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_BABY_ID.equals(param0)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(param0);
    }

    protected boolean convertsInWater() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isUnderWaterConverting()) {
                --this.conversionTime;
                if (this.conversionTime < 0) {
                    this.doUnderWaterConversion();
                }
            } else if (this.convertsInWater()) {
                if (this.isEyeInFluid(FluidTags.WATER)) {
                    ++this.inWaterTime;
                    if (this.inWaterTime >= 600) {
                        this.startUnderWaterConversion(300);
                    }
                } else {
                    this.inWaterTime = -1;
                }
            }
        }

        super.tick();
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            boolean var0 = this.isSunSensitive() && this.isSunBurnTick();
            if (var0) {
                ItemStack var1 = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!var1.isEmpty()) {
                    if (var1.isDamageableItem()) {
                        var1.setDamageValue(var1.getDamageValue() + this.random.nextInt(2));
                        if (var1.getDamageValue() >= var1.getMaxDamage()) {
                            this.broadcastBreakEvent(EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }

                    var0 = false;
                }

                if (var0) {
                    this.setSecondsOnFire(8);
                }
            }
        }

        super.aiStep();
    }

    private void startUnderWaterConversion(int param0) {
        this.conversionTime = param0;
        this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
    }

    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityType.DROWNED);
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1040, this.blockPosition(), 0);
        }

    }

    protected void convertToZombieType(EntityType<? extends Zombie> param0) {
        Zombie var0 = this.convertTo(param0, true);
        if (var0 != null) {
            var0.handleAttributes(var0.level().getCurrentDifficultyAt(var0.blockPosition()).getSpecialMultiplier());
            var0.setCanBreakDoors(var0.supportsBreakDoorGoal() && this.canBreakDoors());
        }

    }

    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (!super.hurt(param0, param1)) {
            return false;
        } else if (!(this.level() instanceof ServerLevel)) {
            return false;
        } else {
            ServerLevel var0 = (ServerLevel)this.level();
            LivingEntity var1 = this.getTarget();
            if (var1 == null && param0.getEntity() instanceof LivingEntity) {
                var1 = (LivingEntity)param0.getEntity();
            }

            if (var1 != null
                && this.level().getDifficulty() == Difficulty.HARD
                && (double)this.random.nextFloat() < this.getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                int var2 = Mth.floor(this.getX());
                int var3 = Mth.floor(this.getY());
                int var4 = Mth.floor(this.getZ());
                Zombie var5 = new Zombie(this.level());

                for(int var6 = 0; var6 < 50; ++var6) {
                    int var7 = var2 + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    int var8 = var3 + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    int var9 = var4 + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    BlockPos var10 = new BlockPos(var7, var8, var9);
                    EntityType<?> var11 = var5.getType();
                    SpawnPlacements.Type var12 = SpawnPlacements.getPlacementType(var11);
                    if (NaturalSpawner.isSpawnPositionOk(var12, this.level(), var10, var11)
                        && SpawnPlacements.checkSpawnRules(var11, var0, MobSpawnType.REINFORCEMENT, var10, this.level().random)) {
                        var5.setPos((double)var7, (double)var8, (double)var9);
                        if (!this.level().hasNearbyAlivePlayer((double)var7, (double)var8, (double)var9, 7.0)
                            && this.level().isUnobstructed(var5)
                            && this.level().noCollision(var5)
                            && !this.level().containsAnyLiquid(var5.getBoundingBox())) {
                            var5.setTarget(var1);
                            var5.finalizeSpawn(var0, this.level().getCurrentDifficultyAt(var5.blockPosition()), MobSpawnType.REINFORCEMENT, null, null);
                            var0.addFreshEntityWithPassengers(var5);
                            this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                                .addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05F, AttributeModifier.Operation.ADDITION));
                            var5.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                                .addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05F, AttributeModifier.Operation.ADDITION));
                            break;
                        }
                    }
                }
            }

            return true;
        }
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = super.doHurtTarget(param0);
        if (var0) {
            float var1 = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < var1 * 0.3F) {
                param0.setSecondsOnFire(2 * (int)var1);
            }
        }

        return var0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource param0, DifficultyInstance param1) {
        super.populateDefaultEquipmentSlots(param0, param1);
        if (param0.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            int var0 = param0.nextInt(3);
            if (var0 == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("IsBaby", this.isBaby());
        param0.putBoolean("CanBreakDoors", this.canBreakDoors());
        param0.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
        param0.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setBaby(param0.getBoolean("IsBaby"));
        this.setCanBreakDoors(param0.getBoolean("CanBreakDoors"));
        this.inWaterTime = param0.getInt("InWaterTime");
        if (param0.contains("DrownedConversionTime", 99) && param0.getInt("DrownedConversionTime") > -1) {
            this.startUnderWaterConversion(param0.getInt("DrownedConversionTime"));
        }

    }

    @Override
    public boolean killedEntity(ServerLevel param0, LivingEntity param1) {
        boolean var0 = super.killedEntity(param0, param1);
        if ((param0.getDifficulty() == Difficulty.NORMAL || param0.getDifficulty() == Difficulty.HARD) && param1 instanceof Villager var1) {
            if (param0.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return var0;
            }

            ZombieVillager var2 = var1.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (var2 != null) {
                var2.finalizeSpawn(
                    param0, param0.getCurrentDifficultyAt(var2.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null
                );
                var2.setVillagerData(var1.getVillagerData());
                var2.setGossips(var1.getGossips().store(NbtOps.INSTANCE));
                var2.setTradeOffers(var1.getOffers().createTag());
                var2.setVillagerXp(var1.getVillagerXp());
                if (!this.isSilent()) {
                    param0.levelEvent(null, 1026, this.blockPosition(), 0);
                }

                var0 = false;
            }
        }

        return var0;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? 0.93F : 1.74F;
    }

    @Override
    public boolean canHoldItem(ItemStack param0) {
        return param0.is(Items.EGG) && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(param0);
    }

    @Override
    public boolean wantsToPickUp(ItemStack param0) {
        return param0.is(Items.GLOW_INK_SAC) ? false : super.wantsToPickUp(param0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        RandomSource var0 = param0.getRandom();
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        float var1 = param1.getSpecialMultiplier();
        this.setCanPickUpLoot(var0.nextFloat() < 0.55F * var1);
        if (param3 == null) {
            param3 = new Zombie.ZombieGroupData(getSpawnAsBabyOdds(var0), true);
        }

        if (param3 instanceof Zombie.ZombieGroupData var2) {
            if (var2.isBaby) {
                this.setBaby(true);
                if (var2.canSpawnJockey) {
                    if ((double)var0.nextFloat() < 0.05) {
                        List<Chicken> var3 = param0.getEntitiesOfClass(
                            Chicken.class, this.getBoundingBox().inflate(5.0, 3.0, 5.0), EntitySelector.ENTITY_NOT_BEING_RIDDEN
                        );
                        if (!var3.isEmpty()) {
                            Chicken var4 = var3.get(0);
                            var4.setChickenJockey(true);
                            this.startRiding(var4);
                        }
                    } else if ((double)var0.nextFloat() < 0.05) {
                        Chicken var5 = EntityType.CHICKEN.create(this.level());
                        if (var5 != null) {
                            var5.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                            var5.finalizeSpawn(param0, param1, MobSpawnType.JOCKEY, null, null);
                            var5.setChickenJockey(true);
                            this.startRiding(var5);
                            param0.addFreshEntity(var5);
                        }
                    }
                }
            }

            this.setCanBreakDoors(this.supportsBreakDoorGoal() && var0.nextFloat() < var1 * 0.1F);
            this.populateDefaultEquipmentSlots(var0, param1);
            this.populateDefaultEquipmentEnchantments(var0, param1);
        }

        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate var6 = LocalDate.now();
            int var7 = var6.get(ChronoField.DAY_OF_MONTH);
            int var8 = var6.get(ChronoField.MONTH_OF_YEAR);
            if (var8 == 10 && var7 == 31 && var0.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(var0.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        this.handleAttributes(var1);
        return param3;
    }

    public static boolean getSpawnAsBabyOdds(RandomSource param0) {
        return param0.nextFloat() < 0.05F;
    }

    protected void handleAttributes(float param0) {
        this.randomizeReinforcementsChance();
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
            .addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05F, AttributeModifier.Operation.ADDITION));
        double var0 = this.random.nextDouble() * 1.5 * (double)param0;
        if (var0 > 1.0) {
            this.getAttribute(Attributes.FOLLOW_RANGE)
                .addPermanentModifier(new AttributeModifier("Random zombie-spawn bonus", var0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (this.random.nextFloat() < param0 * 0.05F) {
            this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADDITION));
            this.getAttribute(Attributes.MAX_HEALTH)
                .addPermanentModifier(
                    new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            this.setCanBreakDoors(this.supportsBreakDoorGoal());
        }

    }

    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * 0.1F);
    }

    @Override
    public double getMyRidingOffset() {
        return this.isBaby() ? 0.0 : -0.45;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        Entity var0 = param0.getEntity();
        if (var0 instanceof Creeper var1 && var1.canDropMobsSkull()) {
            ItemStack var2 = this.getSkull();
            if (!var2.isEmpty()) {
                var1.increaseDroppedSkulls();
                this.spawnAtLocation(var2);
            }
        }

    }

    protected ItemStack getSkull() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

    class ZombieAttackTurtleEggGoal extends RemoveBlockGoal {
        ZombieAttackTurtleEggGoal(PathfinderMob param0, double param1, int param2) {
            super(Blocks.TURTLE_EGG, param0, param1, param2);
        }

        @Override
        public void playDestroyProgressSound(LevelAccessor param0, BlockPos param1) {
            param0.playSound(null, param1, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + Zombie.this.random.nextFloat() * 0.2F);
        }

        @Override
        public void playBreakSound(Level param0, BlockPos param1) {
            param0.playSound(null, param1, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + param0.random.nextFloat() * 0.2F);
        }

        @Override
        public double acceptedDistance() {
            return 1.14;
        }
    }

    public static class ZombieGroupData implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public ZombieGroupData(boolean param0, boolean param1) {
            this.isBaby = param0;
            this.canSpawnJockey = param1;
        }
    }
}
