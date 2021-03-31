package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
    private final boolean locked;

    public ServerboundLockDifficultyPacket(boolean param0) {
        this.locked = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleLockDifficulty(this);
    }

    public ServerboundLockDifficultyPacket(FriendlyByteBuf param0) {
        this.locked = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.locked);
    }

    public boolean isLocked() {
        return this.locked;
    }
}
