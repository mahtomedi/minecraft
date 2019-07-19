package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener> {
    private int x;
    private int z;

    public ClientboundForgetLevelChunkPacket() {
    }

    public ClientboundForgetLevelChunkPacket(int param0, int param1) {
        this.x = param0;
        this.z = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = param0.readInt();
        this.z = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.x);
        param0.writeInt(this.z);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleForgetLevelChunk(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getX() {
        return this.x;
    }

    @OnlyIn(Dist.CLIENT)
    public int getZ() {
        return this.z;
    }
}
