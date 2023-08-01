package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Path;

public record PathfindingDebugPayload(int entityId, Path path, float maxNodeDistance) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/path");

    public PathfindingDebugPayload(FriendlyByteBuf param0) {
        this(param0.readInt(), Path.createFromStream(param0), param0.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.entityId);
        this.path.writeToStream(param0);
        param0.writeFloat(this.maxNodeDistance);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
