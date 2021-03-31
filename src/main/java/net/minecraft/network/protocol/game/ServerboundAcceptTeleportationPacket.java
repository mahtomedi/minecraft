package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
    private final int id;

    public ServerboundAcceptTeleportationPacket(int param0) {
        this.id = param0;
    }

    public ServerboundAcceptTeleportationPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleAcceptTeleportPacket(this);
    }

    public int getId() {
        return this.id;
    }
}
