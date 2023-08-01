package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PoiTicketCountDebugPayload(BlockPos pos, int freeTicketCount) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/poi_ticket_count");

    public PoiTicketCountDebugPayload(FriendlyByteBuf param0) {
        this(param0.readBlockPos(), param0.readInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeInt(this.freeTicketCount);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
