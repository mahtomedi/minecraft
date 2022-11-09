package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
    private static final int TWO_MEGABYTES = 2097152;
    private final CompoundTag heightmaps;
    private final byte[] buffer;
    private final List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk param0) {
        this.heightmaps = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var0 : param0.getHeightmaps()) {
            if (var0.getKey().sendToClient()) {
                this.heightmaps.put(var0.getKey().getSerializationKey(), new LongArrayTag(var0.getValue().getRawData()));
            }
        }

        this.buffer = new byte[calculateChunkSize(param0)];
        extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), param0);
        this.blockEntitiesData = Lists.newArrayList();

        for(Entry<BlockPos, BlockEntity> var1 : param0.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create(var1.getValue()));
        }

    }

    public ClientboundLevelChunkPacketData(FriendlyByteBuf param0, int param1, int param2) {
        this.heightmaps = param0.readNbt();
        if (this.heightmaps == null) {
            throw new RuntimeException("Can't read heightmap in packet for [" + param1 + ", " + param2 + "]");
        } else {
            int var0 = param0.readVarInt();
            if (var0 > 2097152) {
                throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
            } else {
                this.buffer = new byte[var0];
                param0.readBytes(this.buffer);
                this.blockEntitiesData = param0.readList(ClientboundLevelChunkPacketData.BlockEntityInfo::new);
            }
        }
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeNbt(this.heightmaps);
        param0.writeVarInt(this.buffer.length);
        param0.writeBytes(this.buffer);
        param0.writeCollection(this.blockEntitiesData, (param0x, param1) -> param1.write(param0x));
    }

    private static int calculateChunkSize(LevelChunk param0) {
        int var0 = 0;

        for(LevelChunkSection var1 : param0.getSections()) {
            var0 += var1.getSerializedSize();
        }

        return var0;
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf var0 = Unpooled.wrappedBuffer(this.buffer);
        var0.writerIndex(0);
        return var0;
    }

    public static void extractChunkData(FriendlyByteBuf param0, LevelChunk param1) {
        for(LevelChunkSection var0 : param1.getSections()) {
            var0.write(param0);
        }

    }

    public Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int param0, int param1) {
        return param2 -> this.getBlockEntitiesTags(param2, param0, param1);
    }

    private void getBlockEntitiesTags(ClientboundLevelChunkPacketData.BlockEntityTagOutput param0, int param1, int param2) {
        int var0 = 16 * param1;
        int var1 = 16 * param2;
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

        for(ClientboundLevelChunkPacketData.BlockEntityInfo var3 : this.blockEntitiesData) {
            int var4 = var0 + SectionPos.sectionRelative(var3.packedXZ >> 4);
            int var5 = var1 + SectionPos.sectionRelative(var3.packedXZ);
            var2.set(var4, var3.y, var5);
            param0.accept(var2, var3.type, var3.tag);
        }

    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    public CompoundTag getHeightmaps() {
        return this.heightmaps;
    }

    static class BlockEntityInfo {
        final int packedXZ;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final CompoundTag tag;

        private BlockEntityInfo(int param0, int param1, BlockEntityType<?> param2, @Nullable CompoundTag param3) {
            this.packedXZ = param0;
            this.y = param1;
            this.type = param2;
            this.tag = param3;
        }

        private BlockEntityInfo(FriendlyByteBuf param0) {
            this.packedXZ = param0.readByte();
            this.y = param0.readShort();
            this.type = param0.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
            this.tag = param0.readNbt();
        }

        void write(FriendlyByteBuf param0) {
            param0.writeByte(this.packedXZ);
            param0.writeShort(this.y);
            param0.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
            param0.writeNbt(this.tag);
        }

        static ClientboundLevelChunkPacketData.BlockEntityInfo create(BlockEntity param0) {
            CompoundTag var0 = param0.getUpdateTag();
            BlockPos var1 = param0.getBlockPos();
            int var2 = SectionPos.sectionRelative(var1.getX()) << 4 | SectionPos.sectionRelative(var1.getZ());
            return new ClientboundLevelChunkPacketData.BlockEntityInfo(var2, var1.getY(), param0.getType(), var0.isEmpty() ? null : var0);
        }
    }

    @FunctionalInterface
    public interface BlockEntityTagOutput {
        void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}
