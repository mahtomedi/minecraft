package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
    private final SoundEvent sound;
    private final SoundSource source;
    private final int id;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundEntityPacket(SoundEvent param0, SoundSource param1, Entity param2, float param3, float param4, long param5) {
        Validate.notNull(param0, "sound");
        this.sound = param0;
        this.source = param1;
        this.id = param2.getId();
        this.volume = param3;
        this.pitch = param4;
        this.seed = param5;
    }

    public ClientboundSoundEntityPacket(FriendlyByteBuf param0) {
        this.sound = param0.readById(BuiltInRegistries.SOUND_EVENT);
        this.source = param0.readEnum(SoundSource.class);
        this.id = param0.readVarInt();
        this.volume = param0.readFloat();
        this.pitch = param0.readFloat();
        this.seed = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeId(BuiltInRegistries.SOUND_EVENT, this.sound);
        param0.writeEnum(this.source);
        param0.writeVarInt(this.id);
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

    public int getId() {
        return this.id;
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
        param0.handleSoundEntityEvent(this);
    }
}
