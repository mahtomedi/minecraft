package net.minecraft.network.protocol.common.custom;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class GameTestClearMarkersDebugPayload extends Record implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/game_test_clear");

    public GameTestClearMarkersDebugPayload(FriendlyByteBuf param0) {
        this();
    }

    public GameTestClearMarkersDebugPayload() {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",GameTestClearMarkersDebugPayload,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",GameTestClearMarkersDebugPayload,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",GameTestClearMarkersDebugPayload,"">(this, param0);
    }
}
