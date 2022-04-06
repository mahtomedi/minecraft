package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ClientboundCustomSoundPacket implements Packet<ClientGamePacketListener> {
    public static final float LOCATION_ACCURACY = 8.0F;
    private final ResourceLocation name;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundCustomSoundPacket(ResourceLocation param0, SoundSource param1, Vec3 param2, float param3, float param4, long param5) {
        this.name = param0;
        this.source = param1;
        this.x = (int)(param2.x * 8.0);
        this.y = (int)(param2.y * 8.0);
        this.z = (int)(param2.z * 8.0);
        this.volume = param3;
        this.pitch = param4;
        this.seed = param5;
    }

    public ClientboundCustomSoundPacket(FriendlyByteBuf param0) {
        this.name = param0.readResourceLocation();
        this.source = param0.readEnum(SoundSource.class);
        this.x = param0.readInt();
        this.y = param0.readInt();
        this.z = param0.readInt();
        this.volume = param0.readFloat();
        this.pitch = param0.readFloat();
        this.seed = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.name);
        param0.writeEnum(this.source);
        param0.writeInt(this.x);
        param0.writeInt(this.y);
        param0.writeInt(this.z);
        param0.writeFloat(this.volume);
        param0.writeFloat(this.pitch);
        param0.writeLong(this.seed);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public SoundSource getSource() {
        return this.source;
    }

    public double getX() {
        return (double)((float)this.x / 8.0F);
    }

    public double getY() {
        return (double)((float)this.y / 8.0F);
    }

    public double getZ() {
        return (double)((float)this.z / 8.0F);
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleCustomSoundEvent(this);
    }
}
