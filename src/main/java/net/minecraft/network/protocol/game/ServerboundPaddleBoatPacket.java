package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
    private final boolean left;
    private final boolean right;

    public ServerboundPaddleBoatPacket(boolean param0, boolean param1) {
        this.left = param0;
        this.right = param1;
    }

    public ServerboundPaddleBoatPacket(FriendlyByteBuf param0) {
        this.left = param0.readBoolean();
        this.right = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
