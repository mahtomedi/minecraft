package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GameTestClearMarkersDebugPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/game_test_clear");

    public GameTestClearMarkersDebugPayload(FriendlyByteBuf param0) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
