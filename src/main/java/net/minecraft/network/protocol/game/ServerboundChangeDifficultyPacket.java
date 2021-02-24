package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener> {
    private final Difficulty difficulty;

    public ServerboundChangeDifficultyPacket(Difficulty param0) {
        this.difficulty = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChangeDifficulty(this);
    }

    public ServerboundChangeDifficultyPacket(FriendlyByteBuf param0) {
        this.difficulty = Difficulty.byId(param0.readUnsignedByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.difficulty.getId());
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }
}
