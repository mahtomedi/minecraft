package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
    private String name;

    public ServerboundRenameItemPacket() {
    }

    public ServerboundRenameItemPacket(String param0) {
        this.name = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.name = param0.readUtf(32767);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.name);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRenameItem(this);
    }

    public String getName() {
        return this.name;
    }
}
