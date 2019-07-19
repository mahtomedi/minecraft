package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
    private boolean locked;

    public ServerboundLockDifficultyPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundLockDifficultyPacket(boolean param0) {
        this.locked = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleLockDifficulty(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.locked = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBoolean(this.locked);
    }

    public boolean isLocked() {
        return this.locked;
    }
}
