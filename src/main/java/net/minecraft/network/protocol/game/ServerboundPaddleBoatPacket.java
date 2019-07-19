package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
    private boolean left;
    private boolean right;

    public ServerboundPaddleBoatPacket() {
    }

    public ServerboundPaddleBoatPacket(boolean param0, boolean param1) {
        this.left = param0;
        this.right = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.left = param0.readBoolean();
        this.right = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBoolean(this.left);
        param0.writeBoolean(this.right);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePaddleBoat(this);
    }

    public boolean getLeft() {
        return this.left;
    }

    public boolean getRight() {
        return this.right;
    }
}
