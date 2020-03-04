package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Guardian extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.INT);
    protected float clientSideTailAnimation;
    protected float clientSideTailAnimationO;
    protected float clientSideTailAnimationSpeed;
    protected float clientSideSpikesAnimation;
    protected float clientSideSpikesAnimationO;
    private LivingEntity clientSideCachedAttackTarget;
    private int clientSideAttackTime;
    private boolean clientSideTouchedGround;
    protected RandomStrollGoal randomStrollGoal;

    public Guardian(EntityType<? extends Guardian> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 10;
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.moveControl = new Guardian.GuardianMoveControl(this);
        this.clientSideTailAnimation = this.random.nextFloat();
        this.clientSideTailAnimationO = this.clientSideTailAnimation;
    }

    @Override
    protected void registerGoals() {
        MoveTowardsRestrictionGoal var0 = new MoveTowardsRestrictionGoal(this, 1.0);
        this.randomStrollGoal = new RandomStrollGoal(this, 1.0, 80);
        this.goalSelector.addGoal(4, new Guardian.GuardianAttackGoal(this));
        this.goalSelector.addGoal(5, var0);
        this.goalSelector.addGoal(7, this.randomStrollGoal);
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0F, 0.01F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        var0.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, new Guardian.GuardianAttackSelector(this)));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new WaterBoundPathNavigation(this, param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_MOVING, false);
        this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    public boolean isMoving() {
        return this.entityData.get(DATA_ID_MOVING);
    }

    private void setMoving(boolean param0) {
        this.entityData.set(DATA_ID_MOVING, param0);
    }

    public int getAttackDuration() {
        return 80;
    }

    private void setActiveAttackTarget(int param0) {
        this.entityData.set(DATA_ID_ATTACK_TARGET, param0);
    }

    public boolean hasActiveAttackTarget() {
        return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
    }

    @Nullable
    public LivingEntity getActiveAttackTarget() {
        if (!this.hasActiveAttackTarget()) {
            return null;
        } else if (this.level.isClientSide) {
            if (this.clientSideCachedAttackTarget != null) {
                return this.clientSideCachedAttackTarget;
            } else {
                Entity var0 = this.level.getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
                if (var0 instanceof LivingEntity) {
                    this.clientSideCachedAttackTarget = (LivingEntity)var0;
                    return this.clientSideCachedAttackTarget;
                } else {
                    return null;
                }
            }
        } else {
            return this.getTarget();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_ID_ATTACK_TARGET.equals(param0)) {
            this.clientSideAttackTime = 0;
            this.clientSideCachedAttackTarget = null;
        }

    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.5F;
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return param1.getFluidState(param0).is(FluidTags.WATER) ? 10.0F + param1.getBrightness(param0) - 0.5F : super.getWalkTargetValue(param0, param1);
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            if (this.level.isClientSide) {
                this.clientSideTailAnimationO = this.clientSideTailAnimation;
                if (!this.isInWater()) {
                    this.clientSideTailAnimationSpeed = 2.0F;
                    Vec3 var0 = this.getDeltaMovement();
                    if (var0.y > 0.0 && this.clientSideTouchedGround && !this.isSilent()) {
                        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), 1.0F, 1.0F, false);
                    }

                    this.clientSideTouchedGround = var0.y < 0.0 && this.level.loadedAndEntityCanStandOn(this.blockPosition().below(), this);
                } else if (this.isMoving()) {
                    if (this.clientSideTailAnimationSpeed < 0.5F) {
                        this.clientSideTailAnimationSpeed = 4.0F;
                    } else {
                        this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
                    }
                } else {
                    this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
                }

                this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
                this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
                if (!this.isInWaterOrBubble()) {
                    this.clientSideSpikesAnimation = this.random.nextFloat();
                } else if (this.isMoving()) {
                    this.clientSideSpikesAnimation += (0.0F - this.clientSideSpikesAnimation) * 0.25F;
                } else {
                    this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
                }

                if (this.isMoving() && this.isInWater()) {
                    Vec3 var1 = this.getViewVector(0.0F);

                    for(int var2 = 0; var2 < 2; ++var2) {
                        this.level
                            .addParticle(
                                ParticleTypes.BUBBLE,
                                this.getRandomX(0.5) - var1.x * 1.5,
                                this.getRandomY() - var1.y * 1.5,
                                this.getRandomZ(0.5) - var1.z * 1.5,
                                0.0,
                                0.0,
                                0.0
                            );
                    }
                }

                if (this.hasActiveAttackTarget()) {
                    if (this.clientSideAttackTime < this.getAttackDuration()) {
                        ++this.clientSideAttackTime;
                    }

                    LivingEntity var3 = this.getActiveAttackTarget();
                    if (var3 != null) {
                        this.getLookControl().setLookAt(var3, 90.0F, 90.0F);
                        this.getLookControl().tick();
                        double var4 = (double)this.getAttackAnimationScale(0.0F);
                        double var5 = var3.getX() - this.getX();
                        double var6 = var3.getY(0.5) - this.getEyeY();
                        double var7 = var3.getZ() - this.getZ();
                        double var8 = Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
                        var5 /= var8;
                        var6 /= var8;
                        var7 /= var8;
                        double var9 = this.random.nextDouble();

                        while(var9 < var8) {
                            var9 += 1.8 - var4 + this.random.nextDouble() * (1.7 - var4);
                            this.level
                                .addParticle(
                                    ParticleTypes.BUBBLE, this.getX() + var5 * var9, this.getEyeY() + var6 * var9, this.getZ() + var7 * var9, 0.0, 0.0, 0.0
                                );
                        }
                    }
                }
            }

            if (this.isInWaterOrBubble()) {
                this.setAirSupply(300);
            } else if (this.onGround) {
                this.setDeltaMovement(
                    this.getDeltaMovement()
                        .add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F), 0.5, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F))
                );
                this.yRot = this.random.nextFloat() * 360.0F;
                this.onGround = false;
                this.hasImpulse = true;
            }

            if (this.hasActiveAttackTarget()) {
                this.yRot = this.yHeadRot;
            }
        }

        super.aiStep();
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.GUARDIAN_FLOP;
    }

    @OnlyIn(Dist.CLIENT)
    public float getTailAnimation(float param0) {
        return Mth.lerp(param0, this.clientSideTailAnimationO, this.clientSideTailAnimation);
    }

    @OnlyIn(Dist.CLIENT)
    public float getSpikesAnimation(float param0) {
        return Mth.lerp(param0, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
    }

    public float getAttackAnimationScale(float param0) {
        return ((float)this.clientSideAttackTime + param0) / (float)this.getAttackDuration();
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    public static boolean checkGuardianSpawnRules(
        EntityType<? extends Guardian> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4
    ) {
        return (param4.nextInt(20) == 0 || !param1.canSeeSkyFromBelowWater(param3))
            && param1.getDifficulty() != Difficulty.PEACEFUL
            && (param2 == MobSpawnType.SPAWNER || param1.getFluidState(param3).is(FluidTags.WATER));
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (!this.isMoving() && !param0.isMagic() && param0.getDirectEntity() instanceof LivingEntity) {
            LivingEntity var0 = (LivingEntity)param0.getDirectEntity();
            if (!param0.isExplosion()) {
                var0.hurt(DamageSource.thorns(this), 2.0F);
            }
        }

        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.trigger();
        }

        return super.hurt(param0, param1);
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1F, param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (!this.isMoving() && this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(param0);
        }

    }

    static class GuardianAttackGoal extends Goal {
        private final Guardian guardian;
        private int attackTime;
        private final boolean elder;

        public GuardianAttackGoal(Guardian param0) {
            this.guardian = param0;
            this.elder = param0 instanceof ElderGuardian;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity var0 = this.guardian.getTarget();
            return var0 != null && var0.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && (this.elder || this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0);
        }

        @Override
        public void start() {
            this.attackTime = -10;
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(this.guardian.getTarget(), 90.0F, 90.0F);
            this.guardian.hasImpulse = true;
        }

        @Override
        public void stop() {
            this.guardian.setActiveAttackTarget(0);
            this.guardian.setTarget(null);
            this.guardian.randomStrollGoal.trigger();
        }

        @Override
        public void tick() {
            LivingEntity var0 = this.guardian.getTarget();
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(var0, 90.0F, 90.0F);
            if (!this.guardian.canSee(var0)) {
                this.guardian.setTarget(null);
            } else {
                ++this.attackTime;
                if (this.attackTime == 0) {
                    this.guardian.setActiveAttackTarget(this.guardian.getTarget().getId());
                    this.guardian.level.broadcastEntityEvent(this.guardian, (byte)21);
                } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                    float var1 = 1.0F;
                    if (this.guardian.level.getDifficulty() == Difficulty.HARD) {
                        var1 += 2.0F;
                    }

                    if (this.elder) {
                        var1 += 2.0F;
                    }

                    var0.hurt(DamageSource.indirectMagic(this.guardian, this.guardian), var1);
                    var0.hurt(DamageSource.mobAttack(this.guardian), (float)this.guardian.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue());
                    this.guardian.setTarget(null);
                }

                super.tick();
            }
        }
    }

    static class GuardianAttackSelector implements Predicate<LivingEntity> {
        private final Guardian guardian;

        public GuardianAttackSelector(Guardian param0) {
            this.guardian = param0;
        }

        public boolean test(@Nullable LivingEntity param0) {
            return (param0 instanceof Player || param0 instanceof Squid) && param0.distanceToSqr(this.guardian) > 9.0;
        }
    }

    static class GuardianMoveControl extends MoveControl {
        private final Guardian guardian;

        public GuardianMoveControl(Guardian param0) {
            super(param0);
            this.guardian = param0;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && !this.guardian.getNavigation().isDone()) {
                Vec3 var0 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
                double var1 = var0.length();
                double var2 = var0.x / var1;
                double var3 = var0.y / var1;
                double var4 = var0.z / var1;
                float var5 = (float)(Mth.atan2(var0.z, var0.x) * 180.0F / (float)Math.PI) - 90.0F;
                this.guardian.yRot = this.rotlerp(this.guardian.yRot, var5, 90.0F);
                this.guardian.yBodyRot = this.guardian.yRot;
                float var6 = (float)(this.speedModifier * this.guardian.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
                float var7 = Mth.lerp(0.125F, this.guardian.getSpeed(), var6);
                this.guardian.setSpeed(var7);
                double var8 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.5) * 0.05;
                double var9 = Math.cos((double)(this.guardian.yRot * (float) (Math.PI / 180.0)));
                double var10 = Math.sin((double)(this.guardian.yRot * (float) (Math.PI / 180.0)));
                double var11 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.75) * 0.05;
                this.guardian
                    .setDeltaMovement(
                        this.guardian.getDeltaMovement().add(var8 * var9, var11 * (var10 + var9) * 0.25 + (double)var7 * var3 * 0.1, var8 * var10)
                    );
                LookControl var12 = this.guardian.getLookControl();
                double var13 = this.guardian.getX() + var2 * 2.0;
                double var14 = this.guardian.getEyeY() + var3 / var1;
                double var15 = this.guardian.getZ() + var4 * 2.0;
                double var16 = var12.getWantedX();
                double var17 = var12.getWantedY();
                double var18 = var12.getWantedZ();
                if (!var12.isHasWanted()) {
                    var16 = var13;
                    var17 = var14;
                    var18 = var15;
                }

                this.guardian
                    .getLookControl()
                    .setLookAt(Mth.lerp(0.125, var16, var13), Mth.lerp(0.125, var17, var14), Mth.lerp(0.125, var18, var15), 10.0F, 40.0F);
                this.guardian.setMoving(true);
            } else {
                this.guardian.setSpeed(0.0F);
                this.guardian.setMoving(false);
            }
        }
    }
}
