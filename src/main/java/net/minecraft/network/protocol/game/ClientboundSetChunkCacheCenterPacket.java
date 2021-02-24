package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetChunkCacheCenterPacket implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;

    public ClientboundSetChunkCacheCenterPacket(int param0, int param1) {
        this.x = param0;
        this.z = param1;
    }

    public ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf param0) {
        this.x = param0.readVarInt();
        this.z = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.x);
        param0.writeVarInt(this.z);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetChunkCacheCenter(this);
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
