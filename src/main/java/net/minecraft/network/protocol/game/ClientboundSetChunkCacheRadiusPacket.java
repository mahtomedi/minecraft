package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetChunkCacheRadiusPacket implements Packet<ClientGamePacketListener> {
    private final int radius;

    public ClientboundSetChunkCacheRadiusPacket(int param0) {
        this.radius = param0;
    }

    public ClientboundSetChunkCacheRadiusPacket(FriendlyByteBuf param0) {
        this.radius = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.radius);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetChunkCacheRadius(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getRadius() {
        return this.radius;
    }
}
