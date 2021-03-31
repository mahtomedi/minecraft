package net.minecraft.world.entity;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class ItemBasedSteering {
    private static final int MIN_BOOST_TIME = 140;
    private static final int MAX_BOOST_TIME = 700;
    private final SynchedEntityData entityData;
    private final EntityDataAccessor<Integer> boostTimeAccessor;
    private final EntityDataAccessor<Boolean> hasSaddleAccessor;
    public boolean boosting;
    public int boostTime;
    public int boostTimeTotal;

    public ItemBasedSteering(SynchedEntityData param0, EntityDataAccessor<Integer> param1, EntityDataAccessor<Boolean> param2) {
        this.entityData = param0;
        this.boostTimeAccessor = param1;
        this.hasSaddleAccessor = param2;
    }

    public void onSynced() {
        this.boosting = true;
        this.boostTime = 0;
        this.boostTimeTotal = this.entityData.get(this.boostTimeAccessor);
    }

    public boolean boost(Random param0) {
        if (this.boosting) {
            return false;
        } else {
            this.boosting = true;
            this.boostTime = 0;
            this.boostTimeTotal = param0.nextInt(841) + 140;
            this.entityData.set(this.boostTimeAccessor, this.boostTimeTotal);
            return true;
        }
    }

    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putBoolean("Saddle", this.hasSaddle());
    }

    public void readAdditionalSaveData(CompoundTag param0) {
        this.setSaddle(param0.getBoolean("Saddle"));
    }

    public void setSaddle(boolean param0) {
        this.entityData.set(this.hasSaddleAccessor, param0);
    }

    public boolean hasSaddle() {
        return this.entityData.get(this.hasSaddleAccessor);
    }
}
