package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket implements Packet<ServerGamePacketListener> {
    private boolean isFlying;

    public ServerboundPlayerAbilitiesPacket() {
    }

    public ServerboundPlayerAbilitiesPacket(Abilities param0) {
        this.isFlying = param0.flying;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        byte var0 = param0.readByte();
        this.isFlying = (var0 & 2) != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        byte var0 = 0;
        if (this.isFlying) {
            var0 = (byte)(var0 | 2);
        }

        param0.writeByte(var0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlayerAbilities(this);
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}
