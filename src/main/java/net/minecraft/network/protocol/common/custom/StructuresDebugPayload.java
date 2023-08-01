package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record StructuresDebugPayload(ResourceKey<Level> dimension, BoundingBox mainBB, List<StructuresDebugPayload.PieceInfo> pieces)
    implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("debug/structures");

    public StructuresDebugPayload(FriendlyByteBuf param0) {
        this(param0.readResourceKey(Registries.DIMENSION), readBoundingBox(param0), param0.readList(StructuresDebugPayload.PieceInfo::new));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceKey(this.dimension);
        writeBoundingBox(param0, this.mainBB);
        param0.writeCollection(this.pieces, (param1, param2) -> param2.write(param0));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    static BoundingBox readBoundingBox(FriendlyByteBuf param0) {
        return new BoundingBox(param0.readInt(), param0.readInt(), param0.readInt(), param0.readInt(), param0.readInt(), param0.readInt());
    }

    static void writeBoundingBox(FriendlyByteBuf param0, BoundingBox param1) {
        param0.writeInt(param1.minX());
        param0.writeInt(param1.minY());
        param0.writeInt(param1.minZ());
        param0.writeInt(param1.maxX());
        param0.writeInt(param1.maxY());
        param0.writeInt(param1.maxZ());
    }

    public static record PieceInfo(BoundingBox boundingBox, boolean isStart) {
        public PieceInfo(FriendlyByteBuf param0) {
            this(StructuresDebugPayload.readBoundingBox(param0), param0.readBoolean());
        }

        public void write(FriendlyByteBuf param0) {
            StructuresDebugPayload.writeBoundingBox(param0, this.boundingBox);
            param0.writeBoolean(this.isStart);
        }
    }
}
