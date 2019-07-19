package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
    private Difficulty difficulty;
    private boolean locked;

    public ClientboundChangeDifficultyPacket() {
    }

    public ClientboundChangeDifficultyPacket(Difficulty param0, boolean param1) {
        this.difficulty = param0;
        this.locked = param1;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChangeDifficulty(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.difficulty = Difficulty.byId(param0.readUnsignedByte());
        this.locked = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.difficulty.getId());
        param0.writeBoolean(this.locked);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLocked() {
        return this.locked;
    }

    @OnlyIn(Dist.CLIENT)
    public Difficulty getDifficulty() {
        return this.difficulty;
    }
}
