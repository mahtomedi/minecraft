package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
    private final SoundEvent sound;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;

    public ClientboundSoundPacket(SoundEvent param0, SoundSource param1, double param2, double param3, double param4, float param5, float param6) {
        Validate.notNull(param0, "sound");
        this.sound = param0;
        this.source = param1;
        this.x = (int)(param2 * 8.0);
        this.y = (int)(param3 * 8.0);
        this.z = (int)(param4 * 8.0);
        this.volume = param5;
        this.pitch = param6;
    }

    public ClientboundSoundPacket(FriendlyByteBuf param0) {
        this.sound = Registry.SOUND_EVENT.byId(param0.readVarInt());
        this.source = param0.readEnum(SoundSource.class);
        this.x = param0.readInt();
        this.y = param0.readInt();
        this.z = param0.readInt();
        this.volume = param0.readFloat();
        this.pitch = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
        param0.writeEnum(this.source);
        param0.writeInt(this.x);
        param0.writeInt(this.y);
        param0.writeInt(this.z);
        param0.writeFloat(this.volume);
        param0.writeFloat(this.pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getSound() {
        return this.sound;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundSource getSource() {
        return this.source;
    }

    @OnlyIn(Dist.CLIENT)
    public double getX() {
        return (double)((float)this.x / 8.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public double getY() {
        return (double)((float)this.y / 8.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public double getZ() {
        return (double)((float)this.z / 8.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public float getVolume() {
        return this.volume;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPitch() {
        return this.pitch;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSoundEvent(this);
    }
}
