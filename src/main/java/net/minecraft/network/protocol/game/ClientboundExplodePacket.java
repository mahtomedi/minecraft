package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final Explosion.BlockInteraction blockInteraction;
    private final SoundEvent explosionSound;

    public ClientboundExplodePacket(
        double param0,
        double param1,
        double param2,
        float param3,
        List<BlockPos> param4,
        @Nullable Vec3 param5,
        Explosion.BlockInteraction param6,
        ParticleOptions param7,
        ParticleOptions param8,
        SoundEvent param9
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.power = param3;
        this.toBlow = Lists.newArrayList(param4);
        this.explosionSound = param9;
        if (param5 != null) {
            this.knockbackX = (float)param5.x;
            this.knockbackY = (float)param5.y;
            this.knockbackZ = (float)param5.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

        this.blockInteraction = param6;
        this.smallExplosionParticles = param7;
        this.largeExplosionParticles = param8;
    }

    public ClientboundExplodePacket(FriendlyByteBuf param0) {
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.power = param0.readFloat();
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y);
        int var2 = Mth.floor(this.z);
        this.toBlow = param0.readList(param3 -> {
            int var0x = param3.readByte() + var0;
            int var1x = param3.readByte() + var1;
            int var2x = param3.readByte() + var2;
            return new BlockPos(var0x, var1x, var2x);
        });
        this.knockbackX = param0.readFloat();
        this.knockbackY = param0.readFloat();
        this.knockbackZ = param0.readFloat();
        this.blockInteraction = param0.readEnum(Explosion.BlockInteraction.class);
        this.smallExplosionParticles = readParticle(param0, param0.readById(BuiltInRegistries.PARTICLE_TYPE));
        this.largeExplosionParticles = readParticle(param0, param0.readById(BuiltInRegistries.PARTICLE_TYPE));
        this.explosionSound = SoundEvent.readFromNetwork(param0);
    }

    private static <T extends ParticleOptions> T readParticle(FriendlyByteBuf param0, ParticleType<T> param1) {
        return param1.getDeserializer().fromNetwork(param1, param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeFloat(this.power);
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y);
        int var2 = Mth.floor(this.z);
        param0.writeCollection(this.toBlow, (param3, param4) -> {
            int var0x = param4.getX() - var0;
            int var1x = param4.getY() - var1;
            int var2x = param4.getZ() - var2;
            param3.writeByte(var0x);
            param3.writeByte(var1x);
            param3.writeByte(var2x);
        });
        param0.writeFloat(this.knockbackX);
        param0.writeFloat(this.knockbackY);
        param0.writeFloat(this.knockbackZ);
        param0.writeEnum(this.blockInteraction);
        param0.writeId(BuiltInRegistries.PARTICLE_TYPE, this.smallExplosionParticles.getType());
        param0.writeId(BuiltInRegistries.PARTICLE_TYPE, this.largeExplosionParticles.getType());
        this.explosionSound.writeToNetwork(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
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

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public SoundEvent getExplosionSound() {
        return this.explosionSound;
    }
}
