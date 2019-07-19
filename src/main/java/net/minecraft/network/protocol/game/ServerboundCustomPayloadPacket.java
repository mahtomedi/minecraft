package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener> {
    public static final ResourceLocation BRAND = new ResourceLocation("brand");
    private ResourceLocation identifier;
    private FriendlyByteBuf data;

    public ServerboundCustomPayloadPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundCustomPayloadPacket(ResourceLocation param0, FriendlyByteBuf param1) {
        this.identifier = param0;
        this.data = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.identifier = param0.readResourceLocation();
        int var0 = param0.readableBytes();
        if (var0 >= 0 && var0 <= 32767) {
            this.data = new FriendlyByteBuf(param0.readBytes(var0));
        } else {
            throw new IOException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeResourceLocation(this.identifier);
        param0.writeBytes((ByteBuf)this.data);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleCustomPayload(this);
        if (this.data != null) {
            this.data.release();
        }

    }
}
