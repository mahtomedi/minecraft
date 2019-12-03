package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private float xDist;
    private float yDist;
    private float zDist;
    private float maxSpeed;
    private int count;
    private boolean overrideLimiter;
    private ParticleOptions particle;

    public ClientboundLevelParticlesPacket() {
    }

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

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        ParticleType<?> var0 = Registry.PARTICLE_TYPE.byId(param0.readInt());
        if (var0 == null) {
            var0 = ParticleTypes.BARRIER;
        }

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
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(Registry.PARTICLE_TYPE.getId(this.particle.getType()));
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

    @OnlyIn(Dist.CLIENT)
    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    @OnlyIn(Dist.CLIENT)
    public double getX() {
        return this.x;
    }

    @OnlyIn(Dist.CLIENT)
    public double getY() {
        return this.y;
    }

    @OnlyIn(Dist.CLIENT)
    public double getZ() {
        return this.z;
    }

    @OnlyIn(Dist.CLIENT)
    public float getXDist() {
        return this.xDist;
    }

    @OnlyIn(Dist.CLIENT)
    public float getYDist() {
        return this.yDist;
    }

    @OnlyIn(Dist.CLIENT)
    public float getZDist() {
        return this.zDist;
    }

    @OnlyIn(Dist.CLIENT)
    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    @OnlyIn(Dist.CLIENT)
    public int getCount() {
        return this.count;
    }

    @OnlyIn(Dist.CLIENT)
    public ParticleOptions getParticle() {
        return this.particle;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleParticleEvent(this);
    }
}
