package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AgeableMob extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
    public static final int BABY_START_AGE = -24000;
    private static final int FORCED_AGE_PARTICLE_TICKS = 40;
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;

    protected AgeableMob(EntityType<? extends AgeableMob> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param3 == null) {
            param3 = new AgeableMob.AgeableMobGroupData(true);
        }

        AgeableMob.AgeableMobGroupData var0 = (AgeableMob.AgeableMobGroupData)param3;
        if (var0.isShouldSpawnBaby() && var0.getGroupSize() > 0 && param0.getRandom().nextFloat() <= var0.getBabySpawnChance()) {
            this.setAge(-24000);
        }

        var0.increaseGroupSizeByOne();
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Nullable
    public abstract AgeableMob getBreedOffspring(ServerLevel var1, AgeableMob var2);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
    }

    public boolean canBreed() {
        return false;
    }

    public int getAge() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
        } else {
            return this.age;
        }
    }

    public void ageUp(int param0, boolean param1) {
        int var0 = this.getAge();
        var0 += param0 * 20;
        if (var0 > 0) {
            var0 = 0;
        }

        int var2 = var0 - var0;
        this.setAge(var0);
        if (param1) {
            this.forcedAge += var2;
            if (this.forcedAgeTimer == 0) {
                this.forcedAgeTimer = 40;
            }
        }

        if (this.getAge() == 0) {
            this.setAge(this.forcedAge);
        }

    }

    public void ageUp(int param0) {
        this.ageUp(param0, false);
    }

    public void setAge(int param0) {
        int var0 = this.getAge();
        this.age = param0;
        if (var0 < 0 && param0 >= 0 || var0 >= 0 && param0 < 0) {
            this.entityData.set(DATA_BABY_ID, param0 < 0);
            this.ageBoundaryReached();
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Age", this.getAge());
        param0.putInt("ForcedAge", this.forcedAge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setAge(param0.getInt("Age"));
        this.forcedAge = param0.getInt("ForcedAge");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_BABY_ID.equals(param0)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
                }

                --this.forcedAgeTimer;
            }
        } else if (this.isAlive()) {
            int var0 = this.getAge();
            if (var0 < 0) {
                this.setAge(++var0);
            } else if (var0 > 0) {
                this.setAge(--var0);
            }
        }

    }

    protected void ageBoundaryReached() {
        if (!this.isBaby() && this.isPassenger()) {
            Entity var2 = this.getVehicle();
            if (var2 instanceof Boat var0 && !var0.hasEnoughSpaceFor(this)) {
                this.stopRiding();
            }
        }

    }

    @Override
    public boolean isBaby() {
        return this.getAge() < 0;
    }

    @Override
    public void setBaby(boolean param0) {
        this.setAge(param0 ? -24000 : 0);
    }

    public static int getSpeedUpSecondsWhenFeeding(int param0) {
        return (int)((float)(param0 / 20) * 0.1F);
    }

    public static class AgeableMobGroupData implements SpawnGroupData {
        private int groupSize;
        private final boolean shouldSpawnBaby;
        private final float babySpawnChance;

        private AgeableMobGroupData(boolean param0, float param1) {
            this.shouldSpawnBaby = param0;
            this.babySpawnChance = param1;
        }

        public AgeableMobGroupData(boolean param0) {
            this(param0, 0.05F);
        }

        public AgeableMobGroupData(float param0) {
            this(true, param0);
        }

        public int getGroupSize() {
            return this.groupSize;
        }

        public void increaseGroupSizeByOne() {
            ++this.groupSize;
        }

        public boolean isShouldSpawnBaby() {
            return this.shouldSpawnBaby;
        }

        public float getBabySpawnChance() {
            return this.babySpawnChance;
        }
    }
}
