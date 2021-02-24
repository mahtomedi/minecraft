package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
    private final String name;

    public ServerboundRenameItemPacket(String param0) {
        this.name = param0;
    }

    public ServerboundRenameItemPacket(FriendlyByteBuf param0) {
        this.name = param0.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRenameItem(this);
    }

    public String getName() {
        return this.name;
    }
}
