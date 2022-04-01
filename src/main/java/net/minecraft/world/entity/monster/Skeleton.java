package net.minecraft.world.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Skeleton extends AbstractSkeleton {
    private static final EntityDataAccessor<Boolean> DATA_STRAY_CONVERSION_ID = SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPYGLASSES_IN_SOCKETS = SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.INT);
    public static final String CONVERSION_TAG = "StrayConversionTime";
    private int inPowderSnowTime;
    private int conversionTime;

    public Skeleton(EntityType<? extends Skeleton> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_STRAY_CONVERSION_ID, false);
        this.getEntityData().define(DATA_SPYGLASSES_IN_SOCKETS, this.level.random.nextDouble() < 0.0625 ? 2 : 0);
    }

    public boolean isFreezeConverting() {
        return this.getEntityData().get(DATA_STRAY_CONVERSION_ID);
    }

    public void setFreezeConverting(boolean param0) {
        this.entityData.set(DATA_STRAY_CONVERSION_ID, param0);
    }

    public int getSpyglassesInSockets() {
        return this.getEntityData().get(DATA_SPYGLASSES_IN_SOCKETS);
    }

    public void addSpyglassIntoEyeSocket() {
        this.getEntityData().set(DATA_SPYGLASSES_IN_SOCKETS, this.getSpyglassesInSockets() + 1);
    }

    @Override
    public boolean isShaking() {
        return this.isFreezeConverting();
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isFreezeConverting()) {
                --this.conversionTime;
                if (this.conversionTime < 0) {
                    this.doFreezeConversion();
                }
            } else if (this.isInPowderSnow) {
                ++this.inPowderSnowTime;
                if (this.inPowderSnowTime >= 140) {
                    this.startFreezeConversion(300);
                }
            } else {
                this.inPowderSnowTime = -1;
            }
        }

        super.tick();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("StrayConversionTime", this.isFreezeConverting() ? this.conversionTime : -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("StrayConversionTime", 99) && param0.getInt("StrayConversionTime") > -1) {
            this.startFreezeConversion(param0.getInt("StrayConversionTime"));
        }

    }

    private void startFreezeConversion(int param0) {
        this.conversionTime = param0;
        this.entityData.set(DATA_STRAY_CONVERSION_ID, true);
    }

    protected void doFreezeConversion() {
        this.convertTo(EntityType.STRAY, true);
        if (!this.isSilent()) {
            this.level.levelEvent(null, 1048, this.blockPosition(), 0);
        }

    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        Entity var0 = param0.getEntity();
        if (var0 instanceof Creeper var1 && var1.canDropMobsSkull()) {
            var1.increaseDroppedSkulls();
            this.spawnAtLocation(Items.SKELETON_SKULL);
        }

        for(int var2 = 0; var2 < this.getSpyglassesInSockets(); ++var2) {
            this.spawnAtLocation(Items.SPYGLASS);
        }

        this.getEntityData().set(DATA_SPYGLASSES_IN_SOCKETS, 0);
    }
}
