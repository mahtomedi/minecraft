package net.minecraft.world.entity.ambient;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Bat extends AmbientCreature {
    public static final float FLAP_DEGREES_PER_TICK = 74.48451F;
    public static final int TICKS_PER_FLAP = Mth.ceil(2.4166098F);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Bat.class, EntityDataSerializers.BYTE);
    private static final int FLAG_RESTING = 1;
    private static final TargetingConditions BAT_RESTING_TARGETING = new TargetingConditions().range(4.0).allowSameTeam();
    private BlockPos targetPosition;

    public Bat(EntityType<? extends Bat> param0, Level param1) {
        super(param0, param1);
        this.setResting(true);
    }

    @Override
    public boolean isFlapping() {
        return !this.isResting() && this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        return this.isResting() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity param0) {
    }

    @Override
    protected void pushEntities() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0);
    }

    public boolean isResting() {
        return (this.entityData.get(DATA_ID_FLAGS) & 1) != 0;
    }

    public void setResting(boolean param0) {
        byte var0 = this.entityData.get(DATA_ID_FLAGS);
        if (param0) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 | 1));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 & -2));
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isResting()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), (double)Mth.floor(this.getY()) + 1.0 - (double)this.getBbHeight(), this.getZ());
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }

    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        BlockPos var0 = this.blockPosition();
        BlockPos var1 = var0.above();
        if (this.isResting()) {
            boolean var2 = this.isSilent();
            if (this.level.getBlockState(var1).isRedstoneConductor(this.level, var0)) {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = (float)this.random.nextInt(360);
                }

                if (this.level.getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
                    this.setResting(false);
                    if (!var2) {
                        this.level.levelEvent(null, 1025, var0, 0);
                    }
                }
            } else {
                this.setResting(false);
                if (!var2) {
                    this.level.levelEvent(null, 1025, var0, 0);
                }
            }
        } else {
            if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight())) {
                this.targetPosition = null;
            }

            if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerThan(this.position(), 2.0)) {
                this.targetPosition = new BlockPos(
                    this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7),
                    this.getY() + (double)this.random.nextInt(6) - 2.0,
                    this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7)
                );
            }

            double var3 = (double)this.targetPosition.getX() + 0.5 - this.getX();
            double var4 = (double)this.targetPosition.getY() + 0.1 - this.getY();
            double var5 = (double)this.targetPosition.getZ() + 0.5 - this.getZ();
            Vec3 var6 = this.getDeltaMovement();
            Vec3 var7 = var6.add(
                (Math.signum(var3) * 0.5 - var6.x) * 0.1F, (Math.signum(var4) * 0.7F - var6.y) * 0.1F, (Math.signum(var5) * 0.5 - var6.z) * 0.1F
            );
            this.setDeltaMovement(var7);
            float var8 = (float)(Mth.atan2(var7.z, var7.x) * 180.0F / (float)Math.PI) - 90.0F;
            float var9 = Mth.wrapDegrees(var8 - this.yRot);
            this.zza = 0.5F;
            this.yRot += var9;
            if (this.random.nextInt(100) == 0 && this.level.getBlockState(var1).isRedstoneConductor(this.level, var1)) {
                this.setResting(true);
            }
        }

    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        return false;
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            if (!this.level.isClientSide && this.isResting()) {
                this.setResting(false);
            }

            return super.hurt(param0, param1);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.entityData.set(DATA_ID_FLAGS, param0.getByte("BatFlags"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putByte("BatFlags", this.entityData.get(DATA_ID_FLAGS));
    }

    public static boolean checkBatSpawnRules(EntityType<Bat> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        if (param3.getY() >= param1.getSeaLevel()) {
            return false;
        } else {
            int var0 = param1.getMaxLocalRawBrightness(param3);
            int var1 = 4;
            if (isHalloween()) {
                var1 = 7;
            } else if (param4.nextBoolean()) {
                return false;
            }

            return var0 > param4.nextInt(var1) ? false : checkMobSpawnRules(param0, param1, param2, param3, param4);
        }
    }

    private static boolean isHalloween() {
        LocalDate var0 = LocalDate.now();
        int var1 = var0.get(ChronoField.DAY_OF_MONTH);
        int var2 = var0.get(ChronoField.MONTH_OF_YEAR);
        return var2 == 10 && var1 >= 20 || var2 == 11 && var1 <= 3;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height / 2.0F;
    }
}
