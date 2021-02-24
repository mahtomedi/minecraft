package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket implements Packet<ServerGamePacketListener> {
    private final boolean isFlying;

    public ServerboundPlayerAbilitiesPacket(Abilities param0) {
        this.isFlying = param0.flying;
    }

    public ServerboundPlayerAbilitiesPacket(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        this.isFlying = (var0 & 2) != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
