package net.minecraft.world.entity.animal.horse;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractHorse extends Animal implements ContainerListener, PlayerRideableJumping, Saddleable {
    private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = param0 -> param0 instanceof AbstractHorse && ((AbstractHorse)param0).isBred();
    private static final TargetingConditions MOMMY_TARGETING = new TargetingConditions()
        .range(16.0)
        .allowInvulnerable()
        .allowSameTeam()
        .allowUnseeable()
        .selector(PARENT_HORSE_SELECTOR);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(
        Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE
    );
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> DATA_ID_OWNER_UUID = SynchedEntityData.defineId(
        AbstractHorse.class, EntityDataSerializers.OPTIONAL_UUID
    );
    private int eatingCounter;
    private int mouthCounter;
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected boolean isJumping;
    protected SimpleContainer inventory;
    protected int temper;
    protected float playerJumpPendingScale;
    private boolean allowStandSliding;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop = true;
    protected int gallopSoundCounter;

    protected AbstractHorse(EntityType<? extends AbstractHorse> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
        this.createInventory();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, AbstractHorse.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
        this.entityData.define(DATA_ID_OWNER_UUID, Optional.empty());
    }

    protected boolean getFlag(int param0) {
        return (this.entityData.get(DATA_ID_FLAGS) & param0) != 0;
    }

    protected void setFlag(int param0, boolean param1) {
        byte var0 = this.entityData.get(DATA_ID_FLAGS);
        if (param1) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 | param0));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 & ~param0));
        }

    }

    public boolean isTamed() {
        return this.getFlag(2);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_ID_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID param0) {
        this.entityData.set(DATA_ID_OWNER_UUID, Optional.ofNullable(param0));
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public void setTamed(boolean param0) {
        this.setFlag(2, param0);
    }

    public void setIsJumping(boolean param0) {
        this.isJumping = param0;
    }

    @Override
    protected void onLeashDistance(float param0) {
        if (param0 > 6.0F && this.isEating()) {
            this.setEating(false);
        }

    }

    public boolean isEating() {
        return this.getFlag(16);
    }

    public boolean isStanding() {
        return this.getFlag(32);
    }

    public boolean isBred() {
        return this.getFlag(8);
    }

    public void setBred(boolean param0) {
        this.setFlag(8, param0);
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby() && this.isTamed();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource param0) {
        this.inventory.setItem(0, new ItemStack(Items.SADDLE));
        if (param0 != null) {
            this.level.playSound(null, this, SoundEvents.HORSE_SADDLE, param0, 0.5F, 1.0F);
        }

    }

    @Override
    public boolean isSaddled() {
        return this.getFlag(4);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int param0) {
        this.temper = param0;
    }

    public int modifyTemper(int param0) {
        int var0 = Mth.clamp(this.getTemper() + param0, 0, this.getMaxTemper());
        this.setTemper(var0);
        return var0;
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    private void eating() {
        this.openMouth();
        if (!this.isSilent()) {
            SoundEvent var0 = this.getEatingSound();
            if (var0 != null) {
                this.level
                    .playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        var0,
                        this.getSoundSource(),
                        1.0F,
                        1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                    );
            }
        }

    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        if (param0 > 1.0F) {
            this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }

        int var0 = this.calculateFallDamage(param0, param1);
        if (var0 <= 0) {
            return false;
        } else {
            this.hurt(param2, (float)var0);
            if (this.isVehicle()) {
                for(Entity var1 : this.getIndirectPassengers()) {
                    var1.hurt(param2, (float)var0);
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    @Override
    protected int calculateFallDamage(float param0, float param1) {
        return Mth.ceil((param0 * 0.5F - 3.0F) * param1);
    }

    protected int getInventorySize() {
        return 2;
    }

    protected void createInventory() {
        SimpleContainer var0 = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if (var0 != null) {
            var0.removeListener(this);
            int var1 = Math.min(var0.getContainerSize(), this.inventory.getContainerSize());

            for(int var2 = 0; var2 < var1; ++var2) {
                ItemStack var3 = var0.getItem(var2);
                if (!var3.isEmpty()) {
                    this.inventory.setItem(var2, var3.copy());
                }
            }
        }

        this.inventory.addListener(this);
        this.updateContainerEquipment();
    }

    protected void updateContainerEquipment() {
        if (!this.level.isClientSide) {
            this.setFlag(4, !this.inventory.getItem(0).isEmpty());
        }
    }

    @Override
    public void containerChanged(Container param0) {
        boolean var0 = this.isSaddled();
        this.updateContainerEquipment();
        if (this.tickCount > 20 && !var0 && this.isSaddled()) {
            this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
        }

    }

    public double getCustomJump() {
        return this.getAttributeValue(Attributes.JUMP_STRENGTH);
    }

    @Nullable
    protected SoundEvent getEatingSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        if (this.random.nextInt(3) == 0) {
            this.stand();
        }

        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(10) == 0 && !this.isImmobile()) {
            this.stand();
        }

        return null;
    }

    @Nullable
    protected SoundEvent getAngrySound() {
        this.stand();
        return null;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        if (!param1.getMaterial().isLiquid()) {
            BlockState var0 = this.level.getBlockState(param0.above());
            SoundType var1 = param1.getSoundType();
            if (var0.is(Blocks.SNOW)) {
                var1 = var0.getSoundType();
            }

            if (this.isVehicle() && this.canGallop) {
                ++this.gallopSoundCounter;
                if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                    this.playGallopSound(var1);
                } else if (this.gallopSoundCounter <= 5) {
                    this.playSound(SoundEvents.HORSE_STEP_WOOD, var1.getVolume() * 0.15F, var1.getPitch());
                }
            } else if (var1 == SoundType.WOOD) {
                this.playSound(SoundEvents.HORSE_STEP_WOOD, var1.getVolume() * 0.15F, var1.getPitch());
            } else {
                this.playSound(SoundEvents.HORSE_STEP, var1.getVolume() * 0.15F, var1.getPitch());
            }

        }
    }

    protected void playGallopSound(SoundType param0) {
        this.playSound(SoundEvents.HORSE_GALLOP, param0.getVolume() * 0.15F, param0.getPitch());
    }

    public static AttributeSupplier.Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0).add(Attributes.MOVEMENT_SPEED, 0.225F);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    public void openInventory(Player param0) {
        if (!this.level.isClientSide && (!this.isVehicle() || this.hasPassenger(param0)) && this.isTamed()) {
            param0.openHorseInventory(this, this.inventory);
        }

    }

    public InteractionResult fedFood(Player param0, ItemStack param1) {
        boolean var0 = this.handleEating(param0, param1);
        if (!param0.getAbilities().instabuild) {
            param1.shrink(1);
        }

        if (this.level.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            return var0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    protected boolean handleEating(Player param0, ItemStack param1) {
        boolean var0 = false;
        float var1 = 0.0F;
        int var2 = 0;
        int var3 = 0;
        if (param1.is(Items.WHEAT)) {
            var1 = 2.0F;
            var2 = 20;
            var3 = 3;
        } else if (param1.is(Items.SUGAR)) {
            var1 = 1.0F;
            var2 = 30;
            var3 = 3;
        } else if (param1.is(Blocks.HAY_BLOCK.asItem())) {
            var1 = 20.0F;
            var2 = 180;
        } else if (param1.is(Items.APPLE)) {
            var1 = 3.0F;
            var2 = 60;
            var3 = 3;
        } else if (param1.is(Items.GOLDEN_CARROT)) {
            var1 = 4.0F;
            var2 = 60;
            var3 = 5;
            if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                var0 = true;
                this.setInLove(param0);
            }
        } else if (param1.is(Items.GOLDEN_APPLE) || param1.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            var1 = 10.0F;
            var2 = 240;
            var3 = 10;
            if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                var0 = true;
                this.setInLove(param0);
            }
        }

        if (this.getHealth() < this.getMaxHealth() && var1 > 0.0F) {
            this.heal(var1);
            var0 = true;
        }

        if (this.isBaby() && var2 > 0) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level.isClientSide) {
                this.ageUp(var2);
            }

            var0 = true;
        }

        if (var3 > 0 && (var0 || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            var0 = true;
            if (!this.level.isClientSide) {
                this.modifyTemper(var3);
            }
        }

        if (var0) {
            this.eating();
            this.gameEvent(GameEvent.EAT, this.eyeBlockPosition());
        }

        return var0;
    }

    protected void doPlayerRide(Player param0) {
        this.setEating(false);
        this.setStanding(false);
        if (!this.level.isClientSide) {
            param0.yRot = this.yRot;
            param0.xRot = this.xRot;
            param0.startRiding(this);
        }

    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return FOOD_ITEMS.test(param0);
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.inventory != null) {
            for(int var0 = 0; var0 < this.inventory.getContainerSize(); ++var0) {
                ItemStack var1 = this.inventory.getItem(var0);
                if (!var1.isEmpty() && !EnchantmentHelper.hasVanishingCurse(var1)) {
                    this.spawnAtLocation(var1);
                }
            }

        }
    }

    @Override
    public void aiStep() {
        if (this.random.nextInt(200) == 0) {
            this.moveTail();
        }

        super.aiStep();
        if (!this.level.isClientSide && this.isAlive()) {
            if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.0F);
            }

            if (this.canEatGrass()) {
                if (!this.isEating()
                    && !this.isVehicle()
                    && this.random.nextInt(300) == 0
                    && this.level.getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                    this.setEating(true);
                }

                if (this.isEating() && ++this.eatingCounter > 50) {
                    this.eatingCounter = 0;
                    this.setEating(false);
                }
            }

            this.followMommy();
        }
    }

    protected void followMommy() {
        if (this.isBred() && this.isBaby() && !this.isEating()) {
            LivingEntity var0 = this.level
                .getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0));
            if (var0 != null && this.distanceToSqr(var0) > 4.0) {
                this.navigation.createPath(var0, 0);
            }
        }

    }

    public boolean canEatGrass() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
            this.mouthCounter = 0;
            this.setFlag(64, false);
        }

        if ((this.isControlledByLocalInstance() || this.isEffectiveAi()) && this.standCounter > 0 && ++this.standCounter > 20) {
            this.standCounter = 0;
            this.setStanding(false);
        }

        if (this.tailCounter > 0 && ++this.tailCounter > 8) {
            this.tailCounter = 0;
        }

        if (this.sprintCounter > 0) {
            ++this.sprintCounter;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }

        this.eatAnimO = this.eatAnim;
        if (this.isEating()) {
            this.eatAnim += (1.0F - this.eatAnim) * 0.4F + 0.05F;
            if (this.eatAnim > 1.0F) {
                this.eatAnim = 1.0F;
            }
        } else {
            this.eatAnim += (0.0F - this.eatAnim) * 0.4F - 0.05F;
            if (this.eatAnim < 0.0F) {
                this.eatAnim = 0.0F;
            }
        }

        this.standAnimO = this.standAnim;
        if (this.isStanding()) {
            this.eatAnim = 0.0F;
            this.eatAnimO = this.eatAnim;
            this.standAnim += (1.0F - this.standAnim) * 0.4F + 0.05F;
            if (this.standAnim > 1.0F) {
                this.standAnim = 1.0F;
            }
        } else {
            this.allowStandSliding = false;
            this.standAnim += (0.8F * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6F - 0.05F;
            if (this.standAnim < 0.0F) {
                this.standAnim = 0.0F;
            }
        }

        this.mouthAnimO = this.mouthAnim;
        if (this.getFlag(64)) {
            this.mouthAnim += (1.0F - this.mouthAnim) * 0.7F + 0.05F;
            if (this.mouthAnim > 1.0F) {
                this.mouthAnim = 1.0F;
            }
        } else {
            this.mouthAnim += (0.0F - this.mouthAnim) * 0.7F - 0.05F;
            if (this.mouthAnim < 0.0F) {
                this.mouthAnim = 0.0F;
            }
        }

    }

    private void openMouth() {
        if (!this.level.isClientSide) {
            this.mouthCounter = 1;
            this.setFlag(64, true);
        }

    }

    public void setEating(boolean param0) {
        this.setFlag(16, param0);
    }

    public void setStanding(boolean param0) {
        if (param0) {
            this.setEating(false);
        }

        this.setFlag(32, param0);
    }

    private void stand() {
        if (this.isControlledByLocalInstance() || this.isEffectiveAi()) {
            this.standCounter = 1;
            this.setStanding(true);
        }

    }

    public void makeMad() {
        if (!this.isStanding()) {
            this.stand();
            SoundEvent var0 = this.getAngrySound();
            if (var0 != null) {
                this.playSound(var0, this.getSoundVolume(), this.getVoicePitch());
            }
        }

    }

    public boolean tameWithName(Player param0) {
        this.setOwnerUUID(param0.getUUID());
        this.setTamed(true);
        if (param0 instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)param0, this);
        }

        this.level.broadcastEntityEvent(this, (byte)7);
        return true;
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.canBeControlledByRider() && this.isSaddled()) {
                LivingEntity var0 = (LivingEntity)this.getControllingPassenger();
                this.yRot = var0.yRot;
                this.yRotO = this.yRot;
                this.xRot = var0.xRot * 0.5F;
                this.setRot(this.yRot, this.xRot);
                this.yBodyRot = this.yRot;
                this.yHeadRot = this.yBodyRot;
                float var1 = var0.xxa * 0.5F;
                float var2 = var0.zza;
                if (var2 <= 0.0F) {
                    var2 *= 0.25F;
                    this.gallopSoundCounter = 0;
                }

                if (this.onGround && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
                    var1 = 0.0F;
                    var2 = 0.0F;
                }

                if (this.playerJumpPendingScale > 0.0F && !this.isJumping() && this.onGround) {
                    double var3 = this.getCustomJump() * (double)this.playerJumpPendingScale * (double)this.getBlockJumpFactor();
                    double var4;
                    if (this.hasEffect(MobEffects.JUMP)) {
                        var4 = var3 + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                    } else {
                        var4 = var3;
                    }

                    Vec3 var6 = this.getDeltaMovement();
                    this.setDeltaMovement(var6.x, var4, var6.z);
                    this.setIsJumping(true);
                    this.hasImpulse = true;
                    if (var2 > 0.0F) {
                        float var7 = Mth.sin(this.yRot * (float) (Math.PI / 180.0));
                        float var8 = Mth.cos(this.yRot * (float) (Math.PI / 180.0));
                        this.setDeltaMovement(
                            this.getDeltaMovement()
                                .add((double)(-0.4F * var7 * this.playerJumpPendingScale), 0.0, (double)(0.4F * var8 * this.playerJumpPendingScale))
                        );
                    }

                    this.playerJumpPendingScale = 0.0F;
                }

                this.flyingSpeed = this.getSpeed() * 0.1F;
                if (this.isControlledByLocalInstance()) {
                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vec3((double)var1, param0.y, (double)var2));
                } else if (var0 instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                if (this.onGround) {
                    this.playerJumpPendingScale = 0.0F;
                    this.setIsJumping(false);
                }

                this.calculateEntityAnimation(this, false);
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(param0);
            }
        }
    }

    protected void playJumpSound() {
        this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("EatingHaystack", this.isEating());
        param0.putBoolean("Bred", this.isBred());
        param0.putInt("Temper", this.getTemper());
        param0.putBoolean("Tame", this.isTamed());
        if (this.getOwnerUUID() != null) {
            param0.putUUID("Owner", this.getOwnerUUID());
        }

        if (!this.inventory.getItem(0).isEmpty()) {
            param0.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setEating(param0.getBoolean("EatingHaystack"));
        this.setBred(param0.getBoolean("Bred"));
        this.setTemper(param0.getInt("Temper"));
        this.setTamed(param0.getBoolean("Tame"));
        UUID var0;
        if (param0.hasUUID("Owner")) {
            var0 = param0.getUUID("Owner");
        } else {
            String var1 = param0.getString("Owner");
            var0 = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), var1);
        }

        if (var0 != null) {
            this.setOwnerUUID(var0);
        }

        if (param0.contains("SaddleItem", 10)) {
            ItemStack var3 = ItemStack.of(param0.getCompound("SaddleItem"));
            if (var3.is(Items.SADDLE)) {
                this.inventory.setItem(0, var3);
            }
        }

        this.updateContainerEquipment();
    }

    @Override
    public boolean canMate(Animal param0) {
        return false;
    }

    protected boolean canParent() {
        return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return null;
    }

    protected void setOffspringAttributes(AgeableMob param0, AbstractHorse param1) {
        double var0 = this.getAttributeBaseValue(Attributes.MAX_HEALTH)
            + param0.getAttributeBaseValue(Attributes.MAX_HEALTH)
            + (double)this.generateRandomMaxHealth();
        param1.getAttribute(Attributes.MAX_HEALTH).setBaseValue(var0 / 3.0);
        double var1 = this.getAttributeBaseValue(Attributes.JUMP_STRENGTH)
            + param0.getAttributeBaseValue(Attributes.JUMP_STRENGTH)
            + this.generateRandomJumpStrength();
        param1.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(var1 / 3.0);
        double var2 = this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)
            + param0.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)
            + this.generateRandomSpeed();
        param1.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(var2 / 3.0);
    }

    @Override
    public boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @OnlyIn(Dist.CLIENT)
    public float getEatAnim(float param0) {
        return Mth.lerp(param0, this.eatAnimO, this.eatAnim);
    }

    @OnlyIn(Dist.CLIENT)
    public float getStandAnim(float param0) {
        return Mth.lerp(param0, this.standAnimO, this.standAnim);
    }

    @OnlyIn(Dist.CLIENT)
    public float getMouthAnim(float param0) {
        return Mth.lerp(param0, this.mouthAnimO, this.mouthAnim);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onPlayerJump(int param0) {
        if (this.isSaddled()) {
            if (param0 < 0) {
                param0 = 0;
            } else {
                this.allowStandSliding = true;
                this.stand();
            }

            if (param0 >= 90) {
                this.playerJumpPendingScale = 1.0F;
            } else {
                this.playerJumpPendingScale = 0.4F + 0.4F * (float)param0 / 90.0F;
            }

        }
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int param0) {
        this.allowStandSliding = true;
        this.stand();
        this.playJumpSound();
    }

    @Override
    public void handleStopJump() {
    }

    @OnlyIn(Dist.CLIENT)
    protected void spawnTamingParticles(boolean param0) {
        ParticleOptions var0 = param0 ? ParticleTypes.HEART : ParticleTypes.SMOKE;

        for(int var1 = 0; var1 < 7; ++var1) {
            double var2 = this.random.nextGaussian() * 0.02;
            double var3 = this.random.nextGaussian() * 0.02;
            double var4 = this.random.nextGaussian() * 0.02;
            this.level.addParticle(var0, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var2, var3, var4);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 7) {
            this.spawnTamingParticles(true);
        } else if (param0 == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Override
    public void positionRider(Entity param0) {
        super.positionRider(param0);
        if (param0 instanceof Mob) {
            Mob var0 = (Mob)param0;
            this.yBodyRot = var0.yBodyRot;
        }

        if (this.standAnimO > 0.0F) {
            float var1 = Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0));
            float var2 = Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0));
            float var3 = 0.7F * this.standAnimO;
            float var4 = 0.15F * this.standAnimO;
            param0.setPos(
                this.getX() + (double)(var3 * var1),
                this.getY() + this.getPassengersRidingOffset() + param0.getMyRidingOffset() + (double)var4,
                this.getZ() - (double)(var3 * var2)
            );
            if (param0 instanceof LivingEntity) {
                ((LivingEntity)param0).yBodyRot = this.yBodyRot;
            }
        }

    }

    protected float generateRandomMaxHealth() {
        return 15.0F + (float)this.random.nextInt(8) + (float)this.random.nextInt(9);
    }

    protected double generateRandomJumpStrength() {
        return 0.4F + this.random.nextDouble() * 0.2 + this.random.nextDouble() * 0.2 + this.random.nextDouble() * 0.2;
    }

    protected double generateRandomSpeed() {
        return (0.45F + this.random.nextDouble() * 0.3 + this.random.nextDouble() * 0.3 + this.random.nextDouble() * 0.3) * 0.25;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.95F;
    }

    public boolean canWearArmor() {
        return false;
    }

    public boolean isWearingArmor() {
        return !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
    }

    public boolean isArmor(ItemStack param0) {
        return false;
    }

    private SlotAccess createEquipmentSlotAccess(final int param0, final Predicate<ItemStack> param1) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return AbstractHorse.this.inventory.getItem(param0);
            }

            @Override
            public boolean set(ItemStack param0x) {
                if (!param1.test(param0)) {
                    return false;
                } else {
                    AbstractHorse.this.inventory.setItem(param0, param0);
                    AbstractHorse.this.updateContainerEquipment();
                    return true;
                }
            }
        };
    }

    @Override
    public SlotAccess getSlot(int param0) {
        int var0 = param0 - 400;
        if (var0 >= 0 && var0 < 2 && var0 < this.inventory.getContainerSize()) {
            if (var0 == 0) {
                return this.createEquipmentSlotAccess(var0, param0x -> param0x.isEmpty() || param0x.is(Items.SADDLE));
            }

            if (var0 == 1) {
                if (!this.canWearArmor()) {
                    return SlotAccess.NULL;
                }

                return this.createEquipmentSlotAccess(var0, param0x -> param0x.isEmpty() || this.isArmor(param0x));
            }
        }

        int var1 = param0 - 500 + 2;
        return var1 >= 2 && var1 < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, var1) : super.getSlot(param0);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }

    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 param0, LivingEntity param1) {
        double var0 = this.getX() + param0.x;
        double var1 = this.getBoundingBox().minY;
        double var2 = this.getZ() + param0.z;
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(Pose var4 : param1.getDismountPoses()) {
            var3.set(var0, var1, var2);
            double var5 = this.getBoundingBox().maxY + 0.75;

            do {
                double var6 = this.level.getBlockFloorHeight(var3);
                if ((double)var3.getY() + var6 > var5) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(var6)) {
                    AABB var7 = param1.getLocalBoundsForPose(var4);
                    Vec3 var8 = new Vec3(var0, (double)var3.getY() + var6, var2);
                    if (DismountHelper.canDismountTo(this.level, param1, var7.move(var8))) {
                        param1.setPose(var4);
                        return var8;
                    }
                }

                var3.move(Direction.UP);
            } while(!((double)var3.getY() < var5));
        }

        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity param0) {
        Vec3 var0 = getCollisionHorizontalEscapeVector(
            (double)this.getBbWidth(), (double)param0.getBbWidth(), this.yRot + (param0.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F)
        );
        Vec3 var1 = this.getDismountLocationInDirection(var0, param0);
        if (var1 != null) {
            return var1;
        } else {
            Vec3 var2 = getCollisionHorizontalEscapeVector(
                (double)this.getBbWidth(), (double)param0.getBbWidth(), this.yRot + (param0.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F)
            );
            Vec3 var3 = this.getDismountLocationInDirection(var2, param0);
            return var3 != null ? var3 : this.position();
        }
    }

    protected void randomizeAttributes() {
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param3 == null) {
            param3 = new AgeableMob.AgeableMobGroupData(0.2F);
        }

        this.randomizeAttributes();
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public boolean hasInventoryChanged(Container param0) {
        return this.inventory != param0;
    }
}
