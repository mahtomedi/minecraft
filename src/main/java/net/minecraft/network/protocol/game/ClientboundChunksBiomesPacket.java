package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public record ClientboundChunksBiomesPacket(List<ClientboundChunksBiomesPacket.ChunkBiomeData> chunkBiomeData) implements Packet<ClientGamePacketListener> {
    private static final int TWO_MEGABYTES = 2097152;

    public ClientboundChunksBiomesPacket(FriendlyByteBuf param0) {
        this(param0.readList(ClientboundChunksBiomesPacket.ChunkBiomeData::new));
    }

    public static ClientboundChunksBiomesPacket forChunks(List<LevelChunk> param0) {
        return new ClientboundChunksBiomesPacket(param0.stream().map(ClientboundChunksBiomesPacket.ChunkBiomeData::new).toList());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.chunkBiomeData, (param0x, param1) -> param1.write(param0x));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunksBiomes(this);
    }

    public static record ChunkBiomeData(ChunkPos pos, byte[] buffer) {
        public ChunkBiomeData(LevelChunk param0) {
            this(param0.getPos(), new byte[calculateChunkSize(param0)]);
            extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), param0);
        }

        public ChunkBiomeData(FriendlyByteBuf param0) {
            this(param0.readChunkPos(), param0.readByteArray(2097152));
        }

        private static int calculateChunkSize(LevelChunk param0) {
            int var0 = 0;

            for(LevelChunkSection var1 : param0.getSections()) {
                var0 += var1.getBiomes().getSerializedSize();
            }

            return var0;
        }

        public FriendlyByteBuf getReadBuffer() {
            return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
        }

        private ByteBuf getWriteBuffer() {
            ByteBuf var0 = Unpooled.wrappedBuffer(this.buffer);
            var0.writerIndex(0);
            return var0;
        }

        public static void extractChunkData(FriendlyByteBuf param0, LevelChunk param1) {
            for(LevelChunkSection var0 : param1.getSections()) {
                var0.getBiomes().write(param0);
            }

        }

        public void write(FriendlyByteBuf param0) {
            param0.writeChunkPos(this.pos);
            param0.writeByteArray(this.buffer);
        }
    }
}
