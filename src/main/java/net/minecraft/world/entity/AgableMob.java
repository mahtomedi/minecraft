package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public abstract class AgableMob extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgableMob.class, EntityDataSerializers.BOOLEAN);
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;

    protected AgableMob(EntityType<? extends AgableMob> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param3 == null) {
            param3 = new AgableMob.AgableMobGroupData();
        }

        AgableMob.AgableMobGroupData var0 = (AgableMob.AgableMobGroupData)param3;
        if (var0.isShouldSpawnBaby() && var0.getGroupSize() > 0 && this.random.nextFloat() <= var0.getBabySpawnChance()) {
            this.setAge(-24000);
        }

        var0.increaseGroupSizeByOne();
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Nullable
    public abstract AgableMob getBreedOffspring(AgableMob var1);

    protected void onOffspringSpawnedFromEgg(Player param0, AgableMob param1) {
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        Item var1 = var0.getItem();
        if (var1 instanceof SpawnEggItem && ((SpawnEggItem)var1).spawnsEntity(var0.getTag(), this.getType())) {
            if (!this.level.isClientSide) {
                AgableMob var2 = this.getBreedOffspring(this);
                if (var2 != null) {
                    var2.setAge(-24000);
                    var2.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                    this.level.addFreshEntity(var2);
                    if (var0.hasCustomHoverName()) {
                        var2.setCustomName(var0.getHoverName());
                    }

                    this.onOffspringSpawnedFromEgg(param0, var2);
                    if (!param0.abilities.instabuild) {
                        var0.shrink(1);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
    }

    public int getAge() {
        if (this.level.isClientSide) {
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
        int var0 = this.age;
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
        if (this.level.isClientSide) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0) {
                    this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
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
    }

    @Override
    public boolean isBaby() {
        return this.getAge() < 0;
    }

    public static class AgableMobGroupData implements SpawnGroupData {
        private int groupSize;
        private boolean shouldSpawnBaby = true;
        private float babySpawnChance = 0.05F;

        public int getGroupSize() {
            return this.groupSize;
        }

        public void increaseGroupSizeByOne() {
            ++this.groupSize;
        }

        public boolean isShouldSpawnBaby() {
            return this.shouldSpawnBaby;
        }

        public void setShouldSpawnBaby(boolean param0) {
            this.shouldSpawnBaby = param0;
        }

        public float getBabySpawnChance() {
            return this.babySpawnChance;
        }

        public void setBabySpawnChance(float param0) {
            this.babySpawnChance = param0;
        }
    }
}
