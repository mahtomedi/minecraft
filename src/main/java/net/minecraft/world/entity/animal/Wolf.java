package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Wolf extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    public static final Predicate<LivingEntity> PREY_SELECTOR = param0 -> {
        EntityType<?> var0 = param0.getType();
        return var0 == EntityType.SHEEP || var0 == EntityType.RABBIT || var0 == EntityType.FOX;
    };
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;

    public Wolf(EntityType<? extends Wolf> param0, Level param1) {
        super(param0, param1);
        this.setTame(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5, 1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        if (this.isTame()) {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
        } else {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
        }

        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity param0) {
        super.setTarget(param0);
        if (param0 == null) {
            this.setAngry(false);
        } else if (!this.isTame()) {
            this.setAngry(true);
        }

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_INTERESTED_ID, false);
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("Angry", this.isAngry());
        param0.putByte("CollarColor", (byte)this.getCollarColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setAngry(param0.getBoolean("Angry"));
        if (param0.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(param0.getInt("CollarColor")));
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return SoundEvents.WOLF_GROWL;
        } else if (this.random.nextInt(3) == 0) {
            return this.isTame() && this.getHealth() < 10.0F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
            this.level.broadcastEntityEvent(this, (byte)8);
        }

        if (!this.level.isClientSide && this.getTarget() == null && this.isAngry()) {
            this.setAngry(false);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
            this.interestedAngleO = this.interestedAngle;
            if (this.isInterested()) {
                this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
            } else {
                this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
            }

            if (this.isInWaterRainOrBubble()) {
                this.isWet = true;
                this.isShaking = false;
                this.shakeAnim = 0.0F;
                this.shakeAnimO = 0.0F;
            } else if ((this.isWet || this.isShaking) && this.isShaking) {
                if (this.shakeAnim == 0.0F) {
                    this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                this.shakeAnimO = this.shakeAnim;
                this.shakeAnim += 0.05F;
                if (this.shakeAnimO >= 2.0F) {
                    this.isWet = false;
                    this.isShaking = false;
                    this.shakeAnimO = 0.0F;
                    this.shakeAnim = 0.0F;
                }

                if (this.shakeAnim > 0.4F) {
                    float var0 = (float)this.getY();
                    int var1 = (int)(Mth.sin((this.shakeAnim - 0.4F) * (float) Math.PI) * 7.0F);
                    Vec3 var2 = this.getDeltaMovement();

                    for(int var3 = 0; var3 < var1; ++var3) {
                        float var4 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                        float var5 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                        this.level
                            .addParticle(
                                ParticleTypes.SPLASH, this.getX() + (double)var4, (double)(var0 + 0.8F), this.getZ() + (double)var5, var2.x, var2.y, var2.z
                            );
                    }
                }
            }

        }
    }

    @Override
    public void die(DamageSource param0) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0F;
        this.shakeAnim = 0.0F;
        super.die(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isWet() {
        return this.isWet;
    }

    @OnlyIn(Dist.CLIENT)
    public float getWetShade(float param0) {
        return 0.75F + Mth.lerp(param0, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRollAngle(float param0, float param1) {
        float var0 = (Mth.lerp(param0, this.shakeAnimO, this.shakeAnim) + param1) / 1.8F;
        if (var0 < 0.0F) {
            var0 = 0.0F;
        } else if (var0 > 1.0F) {
            var0 = 1.0F;
        }

        return Mth.sin(var0 * (float) Math.PI) * Mth.sin(var0 * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadRollAngle(float param0) {
        return Mth.lerp(param0, this.interestedAngleO, this.interestedAngle) * 0.15F * (float) Math.PI;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.8F;
    }

    @Override
    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            Entity var0 = param0.getEntity();
            this.setOrderedToSit(false);
            if (var0 != null && !(var0 instanceof Player) && !(var0 instanceof AbstractArrow)) {
                param1 = (param1 + 1.0F) / 2.0F;
            }

            return super.hurt(param0, param1);
        }
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = param0.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
        if (var0) {
            this.doEnchantDamageEffects(this, param0);
        }

        return var0;
    }

    @Override
    public void setTame(boolean param0) {
        super.setTame(param0);
        if (param0) {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
            this.setHealth(20.0F);
        } else {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
        }

        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        Item var1 = var0.getItem();
        if (var0.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(param0, param1);
        } else if (this.level.isClientSide) {
            return this.isOwnedBy(param0) || var1 == Items.BONE && !this.isTame() && !this.isAngry();
        } else {
            if (this.isTame()) {
                if (this.isFood(var0) && this.getHealth() < this.getMaxHealth()) {
                    if (!param0.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    this.heal((float)var1.getFoodProperties().getNutrition());
                    return true;
                }

                if (!(var1 instanceof DyeItem)) {
                    boolean var3 = super.mobInteract(param0, param1);
                    if ((!var3 || this.isBaby()) && this.isOwnedBy(param0) && !this.isFood(var0)) {
                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget(null);
                    }

                    return var3;
                }

                DyeColor var2 = ((DyeItem)var1).getDyeColor();
                if (var2 != this.getCollarColor()) {
                    this.setCollarColor(var2);
                    if (!param0.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    return true;
                }
            } else if (var1 == Items.BONE && !this.isAngry()) {
                if (!param0.abilities.instabuild) {
                    var0.shrink(1);
                }

                if (this.random.nextInt(3) == 0) {
                    this.tame(param0);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level.broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level.broadcastEntityEvent(this, (byte)6);
                }

                return true;
            }

            return super.mobInteract(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        } else {
            return this.isTame() ? (0.55F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * (float) Math.PI : (float) (Math.PI / 5);
        }
    }

    @Override
    public boolean isFood(ItemStack param0) {
        Item var0 = param0.getItem();
        return var0.isEdible() && var0.getFoodProperties().isMeat();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    public boolean isAngry() {
        return (this.entityData.get(DATA_FLAGS_ID) & 2) != 0;
    }

    public void setAngry(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 | 2));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 & -3));
        }

    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor param0) {
        this.entityData.set(DATA_COLLAR_COLOR, param0.getId());
    }

    public Wolf getBreedOffspring(AgableMob param0) {
        Wolf var0 = EntityType.WOLF.create(this.level);
        UUID var1 = this.getOwnerUUID();
        if (var1 != null) {
            var0.setOwnerUUID(var1);
            var0.setTame(true);
        }

        return var0;
    }

    public void setIsInterested(boolean param0) {
        this.entityData.set(DATA_INTERESTED_ID, param0);
    }

    @Override
    public boolean canMate(Animal param0) {
        if (param0 == this) {
            return false;
        } else if (!this.isTame()) {
            return false;
        } else if (!(param0 instanceof Wolf)) {
            return false;
        } else {
            Wolf var0 = (Wolf)param0;
            if (!var0.isTame()) {
                return false;
            } else if (var0.isInSittingPose()) {
                return false;
            } else {
                return this.isInLove() && var0.isInLove();
            }
        }
    }

    public boolean isInterested() {
        return this.entityData.get(DATA_INTERESTED_ID);
    }

    @Override
    public boolean wantsToAttack(LivingEntity param0, LivingEntity param1) {
        if (param0 instanceof Creeper || param0 instanceof Ghast) {
            return false;
        } else if (param0 instanceof Wolf) {
            Wolf var0 = (Wolf)param0;
            return !var0.isTame() || var0.getOwner() != param1;
        } else if (param0 instanceof Player && param1 instanceof Player && !((Player)param1).canHarmPlayer((Player)param0)) {
            return false;
        } else if (param0 instanceof AbstractHorse && ((AbstractHorse)param0).isTamed()) {
            return false;
        } else {
            return !(param0 instanceof TamableAnimal) || !((TamableAnimal)param0).isTame();
        }
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return !this.isAngry() && super.canBeLeashed(param0);
    }

    class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(Wolf param0, Class<T> param1, float param2, double param3, double param4) {
            super(param0, param1, param2, param3, param4);
            this.wolf = param0;
        }

        @Override
        public boolean canUse() {
            if (super.canUse() && this.toAvoid instanceof Llama) {
                return !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid);
            } else {
                return false;
            }
        }

        private boolean avoidLlama(Llama param0) {
            return param0.getStrength() >= Wolf.this.random.nextInt(5);
        }

        @Override
        public void start() {
            Wolf.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            Wolf.this.setTarget(null);
            super.tick();
        }
    }
}
