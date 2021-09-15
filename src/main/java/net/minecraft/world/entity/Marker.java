package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

public class Marker extends Entity {
    private static final String DATA_TAG = "data";
    private CompoundTag data = new CompoundTag();

    public Marker(EntityType<?> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.data = param0.getCompound("data");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.put("data", this.data.copy());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected void addPassenger(Entity param0) {
        param0.stopRiding();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}
