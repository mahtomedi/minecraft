package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
    public static final float LOCATION_ACCURACY = 8.0F;
    private final SoundEvent sound;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundPacket(SoundEvent param0, SoundSource param1, double param2, double param3, double param4, float param5, float param6, long param7) {
        Validate.notNull(param0, "sound");
        this.sound = param0;
        this.source = param1;
        this.x = (int)(param2 * 8.0);
        this.y = (int)(param3 * 8.0);
        this.z = (int)(param4 * 8.0);
        this.volume = param5;
        this.pitch = param6;
        this.seed = param7;
    }

    public ClientboundSoundPacket(FriendlyByteBuf param0) {
        this.sound = param0.readById(Registry.SOUND_EVENT);
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
        param0.writeId(Registry.SOUND_EVENT, this.sound);
        param0.writeEnum(this.source);
        param0.writeInt(this.x);
        param0.writeInt(this.y);
        param0.writeInt(this.z);
        param0.writeFloat(this.volume);
        param0.writeFloat(this.pitch);
        param0.writeLong(this.seed);
    }

    public SoundEvent getSound() {
        return this.sound;
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
        param0.handleSoundEvent(this);
    }
}
