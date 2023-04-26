package net.minecraft.world.entity.item;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class PrimedTnt extends Entity implements TraceableEntity {
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
    private static final int DEFAULT_FUSE_TIME = 80;
    @Nullable
    private LivingEntity owner;

    public PrimedTnt(EntityType<? extends PrimedTnt> param0, Level param1) {
        super(param0, param1);
        this.blocksBuilding = true;
    }

    public PrimedTnt(Level param0, double param1, double param2, double param3, @Nullable LivingEntity param4) {
        this(EntityType.TNT, param0);
        this.setPos(param1, param2, param3);
        double var0 = param0.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(var0) * 0.02, 0.2F, -Math.cos(var0) * 0.02);
        this.setFuse(80);
        this.xo = param1;
        this.yo = param2;
        this.zo = param3;
        this.owner = param4;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_FUSE_ID, 80);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }

        int var0 = this.getFuse() - 1;
        this.setFuse(var0);
        if (var0 <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }

    }

    private void explode() {
        float var0 = 4.0F;
        this.level().explode(this, this.getX(), this.getY(0.0625), this.getZ(), 4.0F, Level.ExplosionInteraction.TNT);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("Fuse", (short)this.getFuse());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.setFuse(param0.getShort("Fuse"));
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.owner;
    }

    @Override
    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.15F;
    }

    public void setFuse(int param0) {
        this.entityData.set(DATA_FUSE_ID, param0);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }
}
