package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RaidsDebugPayload(List<BlockPos> raidCenters) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/raids");

    public RaidsDebugPayload(FriendlyByteBuf param0) {
        this(param0.readList(FriendlyByteBuf::readBlockPos));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.raidCenters, FriendlyByteBuf::writeBlockPos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
