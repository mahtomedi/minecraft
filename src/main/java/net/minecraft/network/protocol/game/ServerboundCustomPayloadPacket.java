package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener> {
    public static final ResourceLocation BRAND = new ResourceLocation("brand");
    private final ResourceLocation identifier;
    private final FriendlyByteBuf data;

    @OnlyIn(Dist.CLIENT)
    public ServerboundCustomPayloadPacket(ResourceLocation param0, FriendlyByteBuf param1) {
        this.identifier = param0;
        this.data = param1;
    }

    public ServerboundCustomPayloadPacket(FriendlyByteBuf param0) {
        this.identifier = param0.readResourceLocation();
        int var0 = param0.readableBytes();
        if (var0 >= 0 && var0 <= 32767) {
            this.data = new FriendlyByteBuf(param0.readBytes(var0));
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.identifier);
        param0.writeBytes((ByteBuf)this.data);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleCustomPayload(this);
        this.data.release();
    }
}
