package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Bee extends Animal implements NeutralMob, FlyingAnimal {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.INT);
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;
    private float rollAmount;
    private float rollAmountO;
    private int timeSinceSting;
    private int ticksWithoutNectarSinceExitingHive;
    private int stayOutOfHiveCountdown;
    private int numCropsGrownSincePollination;
    private int remainingCooldownBeforeLocatingNewHive = 0;
    private int remainingCooldownBeforeLocatingNewFlower = 0;
    @Nullable
    private BlockPos savedFlowerPos = null;
    @Nullable
    private BlockPos hivePos = null;
    private Bee.BeePollinateGoal beePollinateGoal;
    private Bee.BeeGoToHiveGoal goToHiveGoal;
    private Bee.BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
    private int underWaterTicks;

    public Bee(EntityType<? extends Bee> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new Bee.BeeLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return param1.getBlockState(param0).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Bee.BeeAttackGoal(this, 1.4F, true));
        this.goalSelector.addGoal(1, new Bee.BeeEnterHiveGoal());
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(ItemTags.FLOWERS), false));
        this.beePollinateGoal = new Bee.BeePollinateGoal();
        this.goalSelector.addGoal(4, this.beePollinateGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new Bee.BeeLocateHiveGoal());
        this.goToHiveGoal = new Bee.BeeGoToHiveGoal();
        this.goalSelector.addGoal(5, this.goToHiveGoal);
        this.goToKnownFlowerGoal = new Bee.BeeGoToKnownFlowerGoal();
        this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
        this.goalSelector.addGoal(7, new Bee.BeeGrowCropGoal());
        this.goalSelector.addGoal(8, new Bee.BeeWanderGoal());
        this.goalSelector.addGoal(9, new FloatGoal(this));
        this.targetSelector.addGoal(1, new Bee.BeeHurtByOtherGoal(this).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new Bee.BeeBecomeAngryTargetGoal(this));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.hasHive()) {
            param0.put("HivePos", NbtUtils.writeBlockPos(this.getHivePos()));
        }

        if (this.hasSavedFlowerPos()) {
            param0.put("FlowerPos", NbtUtils.writeBlockPos(this.getSavedFlowerPos()));
        }

        param0.putBoolean("HasNectar", this.hasNectar());
        param0.putBoolean("HasStung", this.hasStung());
        param0.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
        param0.putInt("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
        param0.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
        this.addPersistentAngerSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.hivePos = null;
        if (param0.contains("HivePos")) {
            this.hivePos = NbtUtils.readBlockPos(param0.getCompound("HivePos"));
        }

        this.savedFlowerPos = null;
        if (param0.contains("FlowerPos")) {
            this.savedFlowerPos = NbtUtils.readBlockPos(param0.getCompound("FlowerPos"));
        }

        super.readAdditionalSaveData(param0);
        this.setHasNectar(param0.getBoolean("HasNectar"));
        this.setHasStung(param0.getBoolean("HasStung"));
        this.ticksWithoutNectarSinceExitingHive = param0.getInt("TicksSincePollination");
        this.stayOutOfHiveCountdown = param0.getInt("CannotEnterHiveTicks");
        this.numCropsGrownSincePollination = param0.getInt("CropsGrownSincePollination");
        this.readPersistentAngerSaveData((ServerLevel)this.level, param0);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = param0.hurt(DamageSource.sting(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (var0) {
            this.doEnchantDamageEffects(this, param0);
            if (param0 instanceof LivingEntity) {
                ((LivingEntity)param0).setStingerCount(((LivingEntity)param0).getStingerCount() + 1);
                int var1 = 0;
                if (this.level.getDifficulty() == Difficulty.NORMAL) {
                    var1 = 10;
                } else if (this.level.getDifficulty() == Difficulty.HARD) {
                    var1 = 18;
                }

                if (var1 > 0) {
                    ((LivingEntity)param0).addEffect(new MobEffectInstance(MobEffects.POISON, var1 * 20, 0));
                }
            }

            this.setHasStung(true);
            this.stopBeingAngry();
            this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
        }

        return var0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05F) {
            for(int var0 = 0; var0 < this.random.nextInt(2) + 1; ++var0) {
                this.spawnFluidParticle(
                    this.level, this.getX() - 0.3F, this.getX() + 0.3F, this.getZ() - 0.3F, this.getZ() + 0.3F, this.getY(0.5), ParticleTypes.FALLING_NECTAR
                );
            }
        }

        this.updateRollAmount();
    }

    private void spawnFluidParticle(Level param0, double param1, double param2, double param3, double param4, double param5, ParticleOptions param6) {
        param0.addParticle(
            param6, Mth.lerp(param0.random.nextDouble(), param1, param2), param5, Mth.lerp(param0.random.nextDouble(), param3, param4), 0.0, 0.0, 0.0
        );
    }

    private void pathfindRandomlyTowards(BlockPos param0) {
        Vec3 var0 = Vec3.atBottomCenterOf(param0);
        int var1 = 0;
        BlockPos var2 = this.blockPosition();
        int var3 = (int)var0.y - var2.getY();
        if (var3 > 2) {
            var1 = 4;
        } else if (var3 < -2) {
            var1 = -4;
        }

        int var4 = 6;
        int var5 = 8;
        int var6 = var2.distManhattan(param0);
        if (var6 < 15) {
            var4 = var6 / 2;
            var5 = var6 / 2;
        }

        Vec3 var7 = RandomPos.getAirPosTowards(this, var4, var5, var1, var0, (float) (Math.PI / 10));
        if (var7 != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(var7.x, var7.y, var7.z, 1.0);
        }
    }

    @Nullable
    public BlockPos getSavedFlowerPos() {
        return this.savedFlowerPos;
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    public void setSavedFlowerPos(BlockPos param0) {
        this.savedFlowerPos = param0;
    }

    private boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingHive > 3600;
    }

    private boolean wantsToEnterHive() {
        if (this.stayOutOfHiveCountdown <= 0 && !this.beePollinateGoal.isPollinating() && !this.hasStung() && this.getTarget() == null) {
            boolean var0 = this.isTiredOfLookingForNectar() || this.level.isRaining() || this.level.isNight() || this.hasNectar();
            return var0 && !this.isHiveNearFire();
        } else {
            return false;
        }
    }

    public void setStayOutOfHiveCountdown(int param0) {
        this.stayOutOfHiveCountdown = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getRollAmount(float param0) {
        return Mth.lerp(param0, this.rollAmountO, this.rollAmount);
    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        if (this.isRolling()) {
            this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
        } else {
            this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
        }

    }

    @Override
    protected void customServerAiStep() {
        boolean var0 = this.hasStung();
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }

        if (var0) {
            ++this.timeSinceSting;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurt(DamageSource.GENERIC, this.getHealth());
            }
        }

        if (!this.hasNectar()) {
            ++this.ticksWithoutNectarSinceExitingHive;
        }

        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level, false);
        }

    }

    public void resetTicksWithoutNectarSinceExitingHive() {
        this.ticksWithoutNectarSinceExitingHive = 0;
    }

    private boolean isHiveNearFire() {
        if (this.hivePos == null) {
            return false;
        } else {
            BlockEntity var0 = this.level.getBlockEntity(this.hivePos);
            return var0 instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)var0).isFireNearby();
        }
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int param0) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, param0);
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID param0) {
        this.persistentAngerTarget = param0;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    private boolean doesHiveHaveSpace(BlockPos param0) {
        BlockEntity var0 = this.level.getBlockEntity(param0);
        if (var0 instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)var0).isFull();
        } else {
            return false;
        }
    }

    public boolean hasHive() {
        return this.hivePos != null;
    }

    @Nullable
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendBeeInfo(this);
    }

    private int getCropsGrownSincePollination() {
        return this.numCropsGrownSincePollination;
    }

    private void resetNumCropsGrownSincePollination() {
        this.numCropsGrownSincePollination = 0;
    }

    private void incrementNumCropsGrownSincePollination() {
        ++this.numCropsGrownSincePollination;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.stayOutOfHiveCountdown > 0) {
                --this.stayOutOfHiveCountdown;
            }

            if (this.remainingCooldownBeforeLocatingNewHive > 0) {
                --this.remainingCooldownBeforeLocatingNewHive;
            }

            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                --this.remainingCooldownBeforeLocatingNewFlower;
            }

            boolean var0 = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
            this.setRolling(var0);
            if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
        }

    }

    private boolean isHiveValid() {
        if (!this.hasHive()) {
            return false;
        } else {
            BlockEntity var0 = this.level.getBlockEntity(this.hivePos);
            return var0 != null && var0.getType() == BlockEntityType.BEEHIVE;
        }
    }

    public boolean hasNectar() {
        return this.getFlag(8);
    }

    private void setHasNectar(boolean param0) {
        if (param0) {
            this.resetTicksWithoutNectarSinceExitingHive();
        }

        this.setFlag(8, param0);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    private void setHasStung(boolean param0) {
        this.setFlag(4, param0);
    }

    private boolean isRolling() {
        return this.getFlag(2);
    }

    private void setRolling(boolean param0) {
        this.setFlag(2, param0);
    }

    private boolean isTooFarAway(BlockPos param0) {
        return !this.closerThan(param0, 32);
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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.FLYING_SPEED, 0.6F)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        FlyingPathNavigation var0 = new FlyingPathNavigation(this, param0) {
            @Override
            public boolean isStableDestination(BlockPos param0) {
                return !this.level.getBlockState(param0.below()).isAir();
            }

            @Override
            public void tick() {
                if (!Bee.this.beePollinateGoal.isPollinating()) {
                    super.tick();
                }
            }
        };
        var0.setCanOpenDoors(false);
        var0.setCanFloat(false);
        var0.setCanPassDoors(true);
        return var0;
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.getItem().is(ItemTags.FLOWERS);
    }

    private boolean isFlowerValid(BlockPos param0) {
        return this.level.isLoaded(param0) && this.level.getBlockState(param0).getBlock().is(BlockTags.FLOWERS);
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    public Bee getBreedOffspring(ServerLevel param0, AgableMob param1) {
        return EntityType.BEE.create(param0);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.isBaby() ? param1.height * 0.5F : param1.height * 0.5F;
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        return false;
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    protected boolean makeFlySound() {
        return true;
    }

    public void dropOffNectar() {
        this.setHasNectar(false);
        this.resetNumCropsGrownSincePollination();
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            Entity var0 = param0.getEntity();
            if (!this.level.isClientSide) {
                this.beePollinateGoal.stopPollinating();
            }

            return super.hurt(param0, param1);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    protected void jumpInLiquid(Tag<Fluid> param0) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
    }

    private boolean closerThan(BlockPos param0, int param1) {
        return param0.closerThan(this.blockPosition(), (double)param1);
    }

    abstract class BaseBeeGoal extends Goal {
        private BaseBeeGoal() {
        }

        public abstract boolean canBeeUse();

        public abstract boolean canBeeContinueToUse();

        @Override
        public boolean canUse() {
            return this.canBeeUse() && !Bee.this.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canBeeContinueToUse() && !Bee.this.isAngry();
        }
    }

    class BeeAttackGoal extends MeleeAttackGoal {
        BeeAttackGoal(PathfinderMob param0, double param1, boolean param2) {
            super(param0, param1, param2);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }
    }

    static class BeeBecomeAngryTargetGoal extends NearestAttackableTargetGoal<Player> {
        BeeBecomeAngryTargetGoal(Bee param0) {
            super(param0, Player.class, 10, true, false, param0::isAngryAt);
        }

        @Override
        public boolean canUse() {
            return this.beeCanTarget() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            boolean var0 = this.beeCanTarget();
            if (var0 && this.mob.getTarget() != null) {
                return super.canContinueToUse();
            } else {
                this.targetMob = null;
                return false;
            }
        }

        private boolean beeCanTarget() {
            Bee var0 = (Bee)this.mob;
            return var0.isAngry() && !var0.hasStung();
        }
    }

    class BeeEnterHiveGoal extends Bee.BaseBeeGoal {
        private BeeEnterHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.hasHive() && Bee.this.wantsToEnterHive() && Bee.this.hivePos.closerThan(Bee.this.position(), 2.0)) {
                BlockEntity var0 = Bee.this.level.getBlockEntity(Bee.this.hivePos);
                if (var0 instanceof BeehiveBlockEntity) {
                    BeehiveBlockEntity var1 = (BeehiveBlockEntity)var0;
                    if (!var1.isFull()) {
                        return true;
                    }

                    Bee.this.hivePos = null;
                }
            }

            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            BlockEntity var0 = Bee.this.level.getBlockEntity(Bee.this.hivePos);
            if (var0 instanceof BeehiveBlockEntity) {
                BeehiveBlockEntity var1 = (BeehiveBlockEntity)var0;
                var1.addOccupant(Bee.this, Bee.this.hasNectar());
            }

        }
    }

    public class BeeGoToHiveGoal extends Bee.BaseBeeGoal {
        private int travellingTicks = Bee.this.level.random.nextInt(10);
        private List<BlockPos> blacklistedTargets = Lists.newArrayList();
        @Nullable
        private Path lastPath = null;
        private int ticksStuck;

        BeeGoToHiveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.hivePos != null
                && !Bee.this.hasRestriction()
                && Bee.this.wantsToEnterHive()
                && !this.hasReachedTarget(Bee.this.hivePos)
                && Bee.this.level.getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.hivePos != null) {
                ++this.travellingTicks;
                if (this.travellingTicks > 600) {
                    this.dropAndBlacklistHive();
                } else if (!Bee.this.navigation.isInProgress()) {
                    if (!Bee.this.closerThan(Bee.this.hivePos, 16)) {
                        if (Bee.this.isTooFarAway(Bee.this.hivePos)) {
                            this.dropHive();
                        } else {
                            Bee.this.pathfindRandomlyTowards(Bee.this.hivePos);
                        }
                    } else {
                        boolean var0 = this.pathfindDirectlyTowards(Bee.this.hivePos);
                        if (!var0) {
                            this.dropAndBlacklistHive();
                        } else if (this.lastPath != null && Bee.this.navigation.getPath().sameAs(this.lastPath)) {
                            ++this.ticksStuck;
                            if (this.ticksStuck > 60) {
                                this.dropHive();
                                this.ticksStuck = 0;
                            }
                        } else {
                            this.lastPath = Bee.this.navigation.getPath();
                        }

                    }
                }
            }
        }

        private boolean pathfindDirectlyTowards(BlockPos param0) {
            Bee.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
            Bee.this.navigation.moveTo((double)param0.getX(), (double)param0.getY(), (double)param0.getZ(), 1.0);
            return Bee.this.navigation.getPath() != null && Bee.this.navigation.getPath().canReach();
        }

        private boolean isTargetBlacklisted(BlockPos param0) {
            return this.blacklistedTargets.contains(param0);
        }

        private void blacklistTarget(BlockPos param0) {
            this.blacklistedTargets.add(param0);

            while(this.blacklistedTargets.size() > 3) {
                this.blacklistedTargets.remove(0);
            }

        }

        private void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        private void dropAndBlacklistHive() {
            if (Bee.this.hivePos != null) {
                this.blacklistTarget(Bee.this.hivePos);
            }

            this.dropHive();
        }

        private void dropHive() {
            Bee.this.hivePos = null;
            Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
        }

        private boolean hasReachedTarget(BlockPos param0) {
            if (Bee.this.closerThan(param0, 2)) {
                return true;
            } else {
                Path var0 = Bee.this.navigation.getPath();
                return var0 != null && var0.getTarget().equals(param0) && var0.canReach() && var0.isDone();
            }
        }
    }

    public class BeeGoToKnownFlowerGoal extends Bee.BaseBeeGoal {
        private int travellingTicks = Bee.this.level.random.nextInt(10);

        BeeGoToKnownFlowerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.savedFlowerPos != null
                && !Bee.this.hasRestriction()
                && this.wantsToGoToKnownFlower()
                && Bee.this.isFlowerValid(Bee.this.savedFlowerPos)
                && !Bee.this.closerThan(Bee.this.savedFlowerPos, 2);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.savedFlowerPos != null) {
                ++this.travellingTicks;
                if (this.travellingTicks > 600) {
                    Bee.this.savedFlowerPos = null;
                } else if (!Bee.this.navigation.isInProgress()) {
                    if (Bee.this.isTooFarAway(Bee.this.savedFlowerPos)) {
                        Bee.this.savedFlowerPos = null;
                    } else {
                        Bee.this.pathfindRandomlyTowards(Bee.this.savedFlowerPos);
                    }
                }
            }
        }

        private boolean wantsToGoToKnownFlower() {
            return Bee.this.ticksWithoutNectarSinceExitingHive > 2400;
        }
    }

    class BeeGrowCropGoal extends Bee.BaseBeeGoal {
        private BeeGrowCropGoal() {
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.getCropsGrownSincePollination() >= 10) {
                return false;
            } else if (Bee.this.random.nextFloat() < 0.3F) {
                return false;
            } else {
                return Bee.this.hasNectar() && Bee.this.isHiveValid();
            }
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void tick() {
            if (Bee.this.random.nextInt(30) == 0) {
                for(int var0 = 1; var0 <= 2; ++var0) {
                    BlockPos var1 = Bee.this.blockPosition().below(var0);
                    BlockState var2 = Bee.this.level.getBlockState(var1);
                    Block var3 = var2.getBlock();
                    boolean var4 = false;
                    IntegerProperty var5 = null;
                    if (var3.is(BlockTags.BEE_GROWABLES)) {
                        if (var3 instanceof CropBlock) {
                            CropBlock var6 = (CropBlock)var3;
                            if (!var6.isMaxAge(var2)) {
                                var4 = true;
                                var5 = var6.getAgeProperty();
                            }
                        } else if (var3 instanceof StemBlock) {
                            int var7 = var2.getValue(StemBlock.AGE);
                            if (var7 < 7) {
                                var4 = true;
                                var5 = StemBlock.AGE;
                            }
                        } else if (var3 == Blocks.SWEET_BERRY_BUSH) {
                            int var8 = var2.getValue(SweetBerryBushBlock.AGE);
                            if (var8 < 3) {
                                var4 = true;
                                var5 = SweetBerryBushBlock.AGE;
                            }
                        }

                        if (var4) {
                            Bee.this.level.levelEvent(2005, var1, 0);
                            Bee.this.level.setBlockAndUpdate(var1, var2.setValue(var5, Integer.valueOf(var2.getValue(var5) + 1)));
                            Bee.this.incrementNumCropsGrownSincePollination();
                        }
                    }
                }

            }
        }
    }

    class BeeHurtByOtherGoal extends HurtByTargetGoal {
        BeeHurtByOtherGoal(Bee param0) {
            super(param0);
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.isAngry() && super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob param0, LivingEntity param1) {
            if (param0 instanceof Bee && this.mob.canSee(param1)) {
                param0.setTarget(param1);
            }

        }
    }

    class BeeLocateHiveGoal extends Bee.BaseBeeGoal {
        private BeeLocateHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.remainingCooldownBeforeLocatingNewHive == 0 && !Bee.this.hasHive() && Bee.this.wantsToEnterHive();
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
            List<BlockPos> var0 = this.findNearbyHivesWithSpace();
            if (!var0.isEmpty()) {
                for(BlockPos var1 : var0) {
                    if (!Bee.this.goToHiveGoal.isTargetBlacklisted(var1)) {
                        Bee.this.hivePos = var1;
                        return;
                    }
                }

                Bee.this.goToHiveGoal.clearBlacklist();
                Bee.this.hivePos = var0.get(0);
            }
        }

        private List<BlockPos> findNearbyHivesWithSpace() {
            BlockPos var0 = Bee.this.blockPosition();
            PoiManager var1 = ((ServerLevel)Bee.this.level).getPoiManager();
            Stream<PoiRecord> var2 = var1.getInRange(param0 -> param0 == PoiType.BEEHIVE || param0 == PoiType.BEE_NEST, var0, 20, PoiManager.Occupancy.ANY);
            return var2.map(PoiRecord::getPos)
                .filter(param1 -> Bee.this.doesHiveHaveSpace(param1))
                .sorted(Comparator.comparingDouble(param1 -> param1.distSqr(var0)))
                .collect(Collectors.toList());
        }
    }

    class BeeLookControl extends LookControl {
        BeeLookControl(Mob param0) {
            super(param0);
        }

        @Override
        public void tick() {
            if (!Bee.this.isAngry()) {
                super.tick();
            }
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Bee.this.beePollinateGoal.isPollinating();
        }
    }

    class BeePollinateGoal extends Bee.BaseBeeGoal {
        private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = param0 -> {
            if (param0.is(BlockTags.TALL_FLOWERS)) {
                if (param0.is(Blocks.SUNFLOWER)) {
                    return param0.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
                } else {
                    return true;
                }
            } else {
                return param0.is(BlockTags.SMALL_FLOWERS);
            }
        };
        private int successfulPollinatingTicks = 0;
        private int lastSoundPlayedTick = 0;
        private boolean pollinating;
        private Vec3 hoverPos;
        private int pollinatingTicks = 0;

        BeePollinateGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
                return false;
            } else if (Bee.this.hasNectar()) {
                return false;
            } else if (Bee.this.level.isRaining()) {
                return false;
            } else if (Bee.this.random.nextFloat() < 0.7F) {
                return false;
            } else {
                Optional<BlockPos> var0 = this.findNearbyFlower();
                if (var0.isPresent()) {
                    Bee.this.savedFlowerPos = var0.get();
                    Bee.this.navigation
                        .moveTo(
                            (double)Bee.this.savedFlowerPos.getX() + 0.5,
                            (double)Bee.this.savedFlowerPos.getY() + 0.5,
                            (double)Bee.this.savedFlowerPos.getZ() + 0.5,
                            1.2F
                        );
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean canBeeContinueToUse() {
            if (!this.pollinating) {
                return false;
            } else if (!Bee.this.hasSavedFlowerPos()) {
                return false;
            } else if (Bee.this.level.isRaining()) {
                return false;
            } else if (this.hasPollinatedLongEnough()) {
                return Bee.this.random.nextFloat() < 0.2F;
            } else if (Bee.this.tickCount % 20 == 0 && !Bee.this.isFlowerValid(Bee.this.savedFlowerPos)) {
                Bee.this.savedFlowerPos = null;
                return false;
            } else {
                return true;
            }
        }

        private boolean hasPollinatedLongEnough() {
            return this.successfulPollinatingTicks > 400;
        }

        private boolean isPollinating() {
            return this.pollinating;
        }

        private void stopPollinating() {
            this.pollinating = false;
        }

        @Override
        public void start() {
            this.successfulPollinatingTicks = 0;
            this.pollinatingTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.pollinating = true;
            Bee.this.resetTicksWithoutNectarSinceExitingHive();
        }

        @Override
        public void stop() {
            if (this.hasPollinatedLongEnough()) {
                Bee.this.setHasNectar(true);
            }

            this.pollinating = false;
            Bee.this.navigation.stop();
            Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
        }

        @Override
        public void tick() {
            ++this.pollinatingTicks;
            if (this.pollinatingTicks > 600) {
                Bee.this.savedFlowerPos = null;
            } else {
                Vec3 var0 = Vec3.atBottomCenterOf(Bee.this.savedFlowerPos).add(0.0, 0.6F, 0.0);
                if (var0.distanceTo(Bee.this.position()) > 1.0) {
                    this.hoverPos = var0;
                    this.setWantedPos();
                } else {
                    if (this.hoverPos == null) {
                        this.hoverPos = var0;
                    }

                    boolean var1 = Bee.this.position().distanceTo(this.hoverPos) <= 0.1;
                    boolean var2 = true;
                    if (!var1 && this.pollinatingTicks > 600) {
                        Bee.this.savedFlowerPos = null;
                    } else {
                        if (var1) {
                            boolean var3 = Bee.this.random.nextInt(25) == 0;
                            if (var3) {
                                this.hoverPos = new Vec3(var0.x() + (double)this.getOffset(), var0.y(), var0.z() + (double)this.getOffset());
                                Bee.this.navigation.stop();
                            } else {
                                var2 = false;
                            }

                            Bee.this.getLookControl().setLookAt(var0.x(), var0.y(), var0.z());
                        }

                        if (var2) {
                            this.setWantedPos();
                        }

                        ++this.successfulPollinatingTicks;
                        if (Bee.this.random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                            this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                            Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }

        private void setWantedPos() {
            Bee.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35F);
        }

        private float getOffset() {
            return (Bee.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
        }

        private Optional<BlockPos> findNearbyFlower() {
            return this.findNearestBlock(this.VALID_POLLINATION_BLOCKS, 5.0);
        }

        private Optional<BlockPos> findNearestBlock(Predicate<BlockState> param0, double param1) {
            BlockPos var0 = Bee.this.blockPosition();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = 0; (double)var2 <= param1; var2 = var2 > 0 ? -var2 : 1 - var2) {
                for(int var3 = 0; (double)var3 < param1; ++var3) {
                    for(int var4 = 0; var4 <= var3; var4 = var4 > 0 ? -var4 : 1 - var4) {
                        for(int var5 = var4 < var3 && var4 > -var3 ? var3 : 0; var5 <= var3; var5 = var5 > 0 ? -var5 : 1 - var5) {
                            var1.setWithOffset(var0, var4, var2 - 1, var5);
                            if (var0.closerThan(var1, param1) && param0.test(Bee.this.level.getBlockState(var1))) {
                                return Optional.of(var1);
                            }
                        }
                    }
                }
            }

            return Optional.empty();
        }
    }

    class BeeWanderGoal extends Goal {
        BeeWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Bee.this.navigation.isDone() && Bee.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 var0 = this.findPos();
            if (var0 != null) {
                Bee.this.navigation.moveTo(Bee.this.navigation.createPath(new BlockPos(var0), 1), 1.0);
            }

        }

        @Nullable
        private Vec3 findPos() {
            Vec3 var1;
            if (Bee.this.isHiveValid() && !Bee.this.closerThan(Bee.this.hivePos, 22)) {
                Vec3 var0 = Vec3.atCenterOf(Bee.this.hivePos);
                var1 = var0.subtract(Bee.this.position()).normalize();
            } else {
                var1 = Bee.this.getViewVector(0.0F);
            }

            int var3 = 8;
            Vec3 var4 = RandomPos.getAboveLandPos(Bee.this, 8, 7, var1, (float) (Math.PI / 2), 2, 1);
            return var4 != null ? var4 : RandomPos.getAirPos(Bee.this, 8, 4, -2, var1, (float) (Math.PI / 2));
        }
    }
}
