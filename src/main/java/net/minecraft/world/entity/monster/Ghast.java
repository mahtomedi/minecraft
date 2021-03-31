package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ghast extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(Ghast.class, EntityDataSerializers.BOOLEAN);
    private int explosionPower = 1;

    public Ghast(EntityType<? extends Ghast> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
        this.moveControl = new Ghast.GhastMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
        this.goalSelector.addGoal(7, new Ghast.GhastShootFireballGoal(this));
        this.targetSelector
            .addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, param0 -> Math.abs(param0.getY() - this.getY()) <= 4.0));
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean param0) {
        this.entityData.set(DATA_IS_CHARGING, param0);
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (param0.getDirectEntity() instanceof LargeFireball && param0.getEntity() instanceof Player) {
            super.hurt(param0, 1000.0F);
            return true;
        } else {
            return super.hurt(param0, param1);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FOLLOW_RANGE, 100.0);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    public static boolean checkGhastSpawnRules(EntityType<Ghast> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getDifficulty() != Difficulty.PEACEFUL && param4.nextInt(20) == 0 && checkMobSpawnRules(param0, param1, param2, param3, param4);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("ExplosionPower", this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("ExplosionPower", 99)) {
            this.explosionPower = param0.getInt("ExplosionPower");
        }

    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 2.6F;
    }

    static class GhastLookGoal extends Goal {
        private final Ghast ghast;

        public GhastLookGoal(Ghast param0) {
            this.ghast = param0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (this.ghast.getTarget() == null) {
                Vec3 var0 = this.ghast.getDeltaMovement();
                this.ghast.yRot = -((float)Mth.atan2(var0.x, var0.z)) * (180.0F / (float)Math.PI);
                this.ghast.yBodyRot = this.ghast.yRot;
            } else {
                LivingEntity var1 = this.ghast.getTarget();
                double var2 = 64.0;
                if (var1.distanceToSqr(this.ghast) < 4096.0) {
                    double var3 = var1.getX() - this.ghast.getX();
                    double var4 = var1.getZ() - this.ghast.getZ();
                    this.ghast.yRot = -((float)Mth.atan2(var3, var4)) * (180.0F / (float)Math.PI);
                    this.ghast.yBodyRot = this.ghast.yRot;
                }
            }

        }
    }

    static class GhastMoveControl extends MoveControl {
        private final Ghast ghast;
        private int floatDuration;

        public GhastMoveControl(Ghast param0) {
            super(param0);
            this.ghast = param0;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
                    Vec3 var0 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
                    double var1 = var0.length();
                    var0 = var0.normalize();
                    if (this.canReach(var0, Mth.ceil(var1))) {
                        this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(var0.scale(0.1)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }

            }
        }

        private boolean canReach(Vec3 param0, int param1) {
            AABB var0 = this.ghast.getBoundingBox();

            for(int var1 = 1; var1 < param1; ++var1) {
                var0 = var0.move(param0);
                if (!this.ghast.level.noCollision(this.ghast, var0)) {
                    return false;
                }
            }

            return true;
        }
    }

    static class GhastShootFireballGoal extends Goal {
        private final Ghast ghast;
        public int chargeTime;

        public GhastShootFireballGoal(Ghast param0) {
            this.ghast = param0;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            this.ghast.setCharging(false);
        }

        @Override
        public void tick() {
            LivingEntity var0 = this.ghast.getTarget();
            double var1 = 64.0;
            if (var0.distanceToSqr(this.ghast) < 4096.0 && this.ghast.canSee(var0)) {
                Level var2 = this.ghast.level;
                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    var2.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
                }

                if (this.chargeTime == 20) {
                    double var3 = 4.0;
                    Vec3 var4 = this.ghast.getViewVector(1.0F);
                    double var5 = var0.getX() - (this.ghast.getX() + var4.x * 4.0);
                    double var6 = var0.getY(0.5) - (0.5 + this.ghast.getY(0.5));
                    double var7 = var0.getZ() - (this.ghast.getZ() + var4.z * 4.0);
                    if (!this.ghast.isSilent()) {
                        var2.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                    }

                    LargeFireball var8 = new LargeFireball(var2, this.ghast, var5, var6, var7);
                    var8.explosionPower = this.ghast.getExplosionPower();
                    var8.setPos(this.ghast.getX() + var4.x * 4.0, this.ghast.getY(0.5) + 0.5, var8.getZ() + var4.z * 4.0);
                    var2.addFreshEntity(var8);
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }

            this.ghast.setCharging(this.chargeTime > 10);
        }
    }

    static class RandomFloatAroundGoal extends Goal {
        private final Ghast ghast;

        public RandomFloatAroundGoal(Ghast param0) {
            this.ghast = param0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl var0 = this.ghast.getMoveControl();
            if (!var0.hasWanted()) {
                return true;
            } else {
                double var1 = var0.getWantedX() - this.ghast.getX();
                double var2 = var0.getWantedY() - this.ghast.getY();
                double var3 = var0.getWantedZ() - this.ghast.getZ();
                double var4 = var1 * var1 + var2 * var2 + var3 * var3;
                return var4 < 1.0 || var4 > 3600.0;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Random var0 = this.ghast.getRandom();
            double var1 = this.ghast.getX() + (double)((var0.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double var2 = this.ghast.getY() + (double)((var0.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double var3 = this.ghast.getZ() + (double)((var0.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.ghast.getMoveControl().setWantedPosition(var1, var2, var3, 1.0);
        }
    }
}
