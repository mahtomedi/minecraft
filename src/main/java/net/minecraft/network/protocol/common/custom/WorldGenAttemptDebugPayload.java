package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record WorldGenAttemptDebugPayload(BlockPos pos, float scale, float red, float green, float blue, float alpha) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/worldgen_attempt");

    public WorldGenAttemptDebugPayload(FriendlyByteBuf param0) {
        this(param0.readBlockPos(), param0.readFloat(), param0.readFloat(), param0.readFloat(), param0.readFloat(), param0.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeFloat(this.scale);
        param0.writeFloat(this.red);
        param0.writeFloat(this.green);
        param0.writeFloat(this.blue);
        param0.writeFloat(this.alpha);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
