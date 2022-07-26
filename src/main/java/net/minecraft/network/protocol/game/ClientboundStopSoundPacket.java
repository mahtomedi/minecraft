package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ClientboundStopSoundPacket implements Packet<ClientGamePacketListener> {
    private static final int HAS_SOURCE = 1;
    private static final int HAS_SOUND = 2;
    @Nullable
    private final ResourceLocation name;
    @Nullable
    private final SoundSource source;

    public ClientboundStopSoundPacket(@Nullable ResourceLocation param0, @Nullable SoundSource param1) {
        this.name = param0;
        this.source = param1;
    }

    public ClientboundStopSoundPacket(FriendlyByteBuf param0) {
        int var0 = param0.readByte();
        if ((var0 & 1) > 0) {
            this.source = param0.readEnum(SoundSource.class);
        } else {
            this.source = null;
        }

        if ((var0 & 2) > 0) {
            this.name = param0.readResourceLocation();
        } else {
            this.name = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        if (this.source != null) {
            if (this.name != null) {
                param0.writeByte(3);
                param0.writeEnum(this.source);
                param0.writeResourceLocation(this.name);
            } else {
                param0.writeByte(1);
                param0.writeEnum(this.source);
            }
        } else if (this.name != null) {
            param0.writeByte(2);
            param0.writeResourceLocation(this.name);
        } else {
            param0.writeByte(0);
        }

    }

    @Nullable
    public ResourceLocation getName() {
        return this.name;
    }

    @Nullable
    public SoundSource getSource() {
        return this.source;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleStopSoundEvent(this);
    }
}
