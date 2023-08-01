package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PoiRemovedDebugPayload(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/poi_removed");

    public PoiRemovedDebugPayload(FriendlyByteBuf param0) {
        this(param0.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
