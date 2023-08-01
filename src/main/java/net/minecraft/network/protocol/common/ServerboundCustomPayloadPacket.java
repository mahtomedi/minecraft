package net.minecraft.network.protocol.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 32767;
    private static final Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> KNOWN_TYPES = ImmutableMap.<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>>builder(
            
        )
        .put(BrandPayload.ID, BrandPayload::new)
        .build();

    public ServerboundCustomPayloadPacket(FriendlyByteBuf param0) {
        this(readPayload(param0.readResourceLocation(), param0));
    }

    private static CustomPacketPayload readPayload(ResourceLocation param0, FriendlyByteBuf param1) {
        FriendlyByteBuf.Reader<? extends CustomPacketPayload> var0 = KNOWN_TYPES.get(param0);
        return (CustomPacketPayload)(var0 != null ? var0.apply(param1) : readUnknownPayload(param0, param1));
    }

    private static DiscardedPayload readUnknownPayload(ResourceLocation param0, FriendlyByteBuf param1) {
        int var0 = param1.readableBytes();
        if (var0 >= 0 && var0 <= 32767) {
            param1.skipBytes(var0);
            return new DiscardedPayload(param0);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.payload.id());
        this.payload.write(param0);
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handleCustomPayload(this);
    }
}
