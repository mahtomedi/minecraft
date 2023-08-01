package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record BrandPayload(String brand) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("brand");

    public BrandPayload(FriendlyByteBuf param0) {
        this(param0.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.brand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
