package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Blaze extends Monster {
    private float allowedHeightOffset = 0.5F;
    private int nextHeightOffsetChangeTick;
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Blaze.class, EntityDataSerializers.BYTE);

    public Blaze(EntityType<? extends Blaze> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new Blaze.BlazeAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23F);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    public void aiStep() {
        if (!this.onGround && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }

        if (this.level.isClientSide) {
            if (this.random.nextInt(24) == 0 && !this.isSilent()) {
                this.level
                    .playLocalSound(
                        this.getX() + 0.5,
                        this.getY() + 0.5,
                        this.getZ() + 0.5,
                        SoundEvents.BLAZE_BURN,
                        this.getSoundSource(),
                        1.0F + this.random.nextFloat(),
                        this.random.nextFloat() * 0.7F + 0.3F,
                        false
                    );
            }

            for(int var0 = 0; var0 < 2; ++var0) {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            }
        }

        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isInWaterRainOrBubble()) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }

        --this.nextHeightOffsetChangeTick;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = 0.5F + (float)this.random.nextGaussian() * 3.0F;
        }

        LivingEntity var0 = this.getTarget();
        if (var0 != null && var0.getEyeY() > this.getEyeY() + (double)this.allowedHeightOffset && this.canAttack(var0)) {
            Vec3 var1 = this.getDeltaMovement();
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, (0.3F - var1.y) * 0.3F, 0.0));
            this.hasImpulse = true;
        }

        super.customServerAiStep();
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return this.isCharged();
    }

    private boolean isCharged() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    private void setCharged(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            var0 = (byte)(var0 | 1);
        } else {
            var0 = (byte)(var0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, var0);
    }

    static class BlazeAttackGoal extends Goal {
        private final Blaze blaze;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public BlazeAttackGoal(Blaze param0) {
            this.blaze = param0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity var0 = this.blaze.getTarget();
            return var0 != null && var0.isAlive() && this.blaze.canAttack(var0);
        }

        @Override
        public void start() {
            this.attackStep = 0;
        }

        @Override
        public void stop() {
            this.blaze.setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity var0 = this.blaze.getTarget();
            if (var0 != null) {
                boolean var1 = this.blaze.getSensing().canSee(var0);
                if (var1) {
                    this.lastSeen = 0;
                } else {
                    ++this.lastSeen;
                }

                double var2 = this.blaze.distanceToSqr(var0);
                if (var2 < 4.0) {
                    if (!var1) {
                        return;
                    }

                    if (this.attackTime <= 0) {
                        this.attackTime = 20;
                        this.blaze.doHurtTarget(var0);
                    }

                    this.blaze.getMoveControl().setWantedPosition(var0.getX(), var0.getY(), var0.getZ(), 1.0);
                } else if (var2 < this.getFollowDistance() * this.getFollowDistance() && var1) {
                    double var3 = var0.getX() - this.blaze.getX();
                    double var4 = var0.getY(0.5) - this.blaze.getY(0.5);
                    double var5 = var0.getZ() - this.blaze.getZ();
                    if (this.attackTime <= 0) {
                        ++this.attackStep;
                        if (this.attackStep == 1) {
                            this.attackTime = 60;
                            this.blaze.setCharged(true);
                        } else if (this.attackStep <= 4) {
                            this.attackTime = 6;
                        } else {
                            this.attackTime = 100;
                            this.attackStep = 0;
                            this.blaze.setCharged(false);
                        }

                        if (this.attackStep > 1) {
                            float var6 = Mth.sqrt(Mth.sqrt(var2)) * 0.5F;
                            this.blaze.level.levelEvent(null, 1018, new BlockPos(this.blaze), 0);

                            for(int var7 = 0; var7 < 1; ++var7) {
                                SmallFireball var8 = new SmallFireball(
                                    this.blaze.level,
                                    this.blaze,
                                    var3 + this.blaze.getRandom().nextGaussian() * (double)var6,
                                    var4,
                                    var5 + this.blaze.getRandom().nextGaussian() * (double)var6
                                );
                                var8.setPos(var8.getX(), this.blaze.getY(0.5) + 0.5, var8.getZ());
                                this.blaze.level.addFreshEntity(var8);
                            }
                        }
                    }

                    this.blaze.getLookControl().setLookAt(var0, 10.0F, 10.0F);
                } else if (this.lastSeen < 5) {
                    this.blaze.getMoveControl().setWantedPosition(var0.getX(), var0.getY(), var0.getZ(), 1.0);
                }

                super.tick();
            }
        }

        private double getFollowDistance() {
            return this.blaze.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue();
        }
    }
}
