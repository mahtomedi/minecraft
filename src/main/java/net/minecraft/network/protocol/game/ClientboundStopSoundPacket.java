package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundStopSoundPacket implements Packet<ClientGamePacketListener> {
    private ResourceLocation name;
    private SoundSource source;

    public ClientboundStopSoundPacket() {
    }

    public ClientboundStopSoundPacket(@Nullable ResourceLocation param0, @Nullable SoundSource param1) {
        this.name = param0;
        this.source = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        int var0 = param0.readByte();
        if ((var0 & 1) > 0) {
            this.source = param0.readEnum(SoundSource.class);
        }

        if ((var0 & 2) > 0) {
            this.name = param0.readResourceLocation();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getName() {
        return this.name;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public SoundSource getSource() {
        return this.source;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleStopSoundEvent(this);
    }
}
