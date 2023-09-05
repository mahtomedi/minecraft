package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ClientInformation;

public record ServerboundClientInformationPacket(ClientInformation information) implements Packet<ServerCommonPacketListener> {
    public ServerboundClientInformationPacket(FriendlyByteBuf param0) {
        this(new ClientInformation(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.information.write(param0);
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handleClientInformation(this);
    }
}
