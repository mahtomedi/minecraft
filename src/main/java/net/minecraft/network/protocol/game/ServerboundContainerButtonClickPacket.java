package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final int buttonId;

    public ServerboundContainerButtonClickPacket(int param0, int param1) {
        this.containerId = param0;
        this.buttonId = param1;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerButtonClick(this);
    }

    public ServerboundContainerButtonClickPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.buttonId = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeByte(this.buttonId);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getButtonId() {
        return this.buttonId;
    }
}
