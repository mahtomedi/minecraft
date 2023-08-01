package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record HiveDebugPayload(HiveDebugPayload.HiveInfo hiveInfo) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/hive");

    public HiveDebugPayload(FriendlyByteBuf param0) {
        this(new HiveDebugPayload.HiveInfo(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.hiveInfo.write(param0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static record HiveInfo(BlockPos pos, String hiveType, int occupantCount, int honeyLevel, boolean sedated) {
        public HiveInfo(FriendlyByteBuf param0) {
            this(param0.readBlockPos(), param0.readUtf(), param0.readInt(), param0.readInt(), param0.readBoolean());
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeBlockPos(this.pos);
            param0.writeUtf(this.hiveType);
            param0.writeInt(this.occupantCount);
            param0.writeInt(this.honeyLevel);
            param0.writeBoolean(this.sedated);
        }
    }
}
