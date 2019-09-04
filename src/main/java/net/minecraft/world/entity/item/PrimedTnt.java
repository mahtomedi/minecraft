package net.minecraft.world.entity.item;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class PrimedTnt extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
    @Nullable
    private LivingEntity owner;
    private int life = 80;

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
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }

        --this.life;
        if (this.life <= 0) {
            this.remove();
            if (!this.level.isClientSide) {
                this.explode();
            }
        } else {
            this.updateInWaterState();
            this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y + 0.5, this.z, 0.0, 0.0, 0.0);
        }

    }

    private void explode() {
        float var0 = 4.0F;
        this.level.explode(this, this.x, this.y + (double)(this.getBbHeight() / 16.0F), this.z, 4.0F, Explosion.BlockInteraction.BREAK);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("Fuse", (short)this.getLife());
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
        return 0.0F;
    }

    public void setFuse(int param0) {
        this.entityData.set(DATA_FUSE_ID, param0);
        this.life = param0;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_FUSE_ID.equals(param0)) {
            this.life = this.getFuse();
        }

    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }

    public int getLife() {
        return this.life;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
