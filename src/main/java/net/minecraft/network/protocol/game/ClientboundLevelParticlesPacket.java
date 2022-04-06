package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final ParticleOptions particle;

    public <T extends ParticleOptions> ClientboundLevelParticlesPacket(
        T param0, boolean param1, double param2, double param3, double param4, float param5, float param6, float param7, float param8, int param9
    ) {
        this.particle = param0;
        this.overrideLimiter = param1;
        this.x = param2;
        this.y = param3;
        this.z = param4;
        this.xDist = param5;
        this.yDist = param6;
        this.zDist = param7;
        this.maxSpeed = param8;
        this.count = param9;
    }

    public ClientboundLevelParticlesPacket(FriendlyByteBuf param0) {
        ParticleType<?> var0 = param0.readById(Registry.PARTICLE_TYPE);
        this.overrideLimiter = param0.readBoolean();
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.xDist = param0.readFloat();
        this.yDist = param0.readFloat();
        this.zDist = param0.readFloat();
        this.maxSpeed = param0.readFloat();
        this.count = param0.readInt();
        this.particle = this.readParticle(param0, var0);
    }

    private <T extends ParticleOptions> T readParticle(FriendlyByteBuf param0, ParticleType<T> param1) {
        return param1.getDeserializer().fromNetwork(param1, param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeId(Registry.PARTICLE_TYPE, this.particle.getType());
        param0.writeBoolean(this.overrideLimiter);
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeFloat(this.xDist);
        param0.writeFloat(this.yDist);
        param0.writeFloat(this.zDist);
        param0.writeFloat(this.maxSpeed);
        param0.writeInt(this.count);
        this.particle.writeToNetwork(param0);
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getXDist() {
        return this.xDist;
    }

    public float getYDist() {
        return this.yDist;
    }

    public float getZDist() {
        return this.zDist;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public ParticleOptions getParticle() {
        return this.particle;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleParticleEvent(this);
    }
}
