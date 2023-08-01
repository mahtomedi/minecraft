package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record NeighborUpdatesDebugPayload(long time, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/neighbors_update");

    public NeighborUpdatesDebugPayload(FriendlyByteBuf param0) {
        this(param0.readVarLong(), param0.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarLong(this.time);
        param0.writeBlockPos(this.pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
