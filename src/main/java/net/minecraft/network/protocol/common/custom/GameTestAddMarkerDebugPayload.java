package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GameTestAddMarkerDebugPayload(BlockPos pos, int color, String text, int durationMs) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/game_test_add_marker");

    public GameTestAddMarkerDebugPayload(FriendlyByteBuf param0) {
        this(param0.readBlockPos(), param0.readInt(), param0.readUtf(), param0.readInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeInt(this.color);
        param0.writeUtf(this.text);
        param0.writeInt(this.durationMs);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
