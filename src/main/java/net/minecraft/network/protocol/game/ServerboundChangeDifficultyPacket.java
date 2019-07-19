package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener> {
    private Difficulty difficulty;

    public ServerboundChangeDifficultyPacket() {
    }

    public ServerboundChangeDifficultyPacket(Difficulty param0) {
        this.difficulty = param0;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChangeDifficulty(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.difficulty = Difficulty.byId(param0.readUnsignedByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.difficulty.getId());
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }
}
