package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundChunkBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
    private ChunkPos chunkPos;
    private ClientboundChunkBlocksUpdatePacket.BlockUpdate[] updates;

    public ClientboundChunkBlocksUpdatePacket() {
    }

    public ClientboundChunkBlocksUpdatePacket(int param0, short[] param1, LevelChunk param2) {
        this.chunkPos = param2.getPos();
        this.updates = new ClientboundChunkBlocksUpdatePacket.BlockUpdate[param0];

        for(int var0 = 0; var0 < this.updates.length; ++var0) {
            this.updates[var0] = new ClientboundChunkBlocksUpdatePacket.BlockUpdate(param1[var0], param2);
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.chunkPos = new ChunkPos(param0.readInt(), param0.readInt());
        this.updates = new ClientboundChunkBlocksUpdatePacket.BlockUpdate[param0.readVarInt()];

        for(int var0 = 0; var0 < this.updates.length; ++var0) {
            this.updates[var0] = new ClientboundChunkBlocksUpdatePacket.BlockUpdate(param0.readShort(), Block.BLOCK_STATE_REGISTRY.byId(param0.readVarInt()));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.chunkPos.x);
        param0.writeInt(this.chunkPos.z);
        param0.writeVarInt(this.updates.length);

        for(ClientboundChunkBlocksUpdatePacket.BlockUpdate var0 : this.updates) {
            param0.writeShort(var0.getOffset());
            param0.writeVarInt(Block.getId(var0.getBlock()));
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunkBlocksUpdate(this);
    }

    @OnlyIn(Dist.CLIENT)
    public ClientboundChunkBlocksUpdatePacket.BlockUpdate[] getUpdates() {
        return this.updates;
    }

    public class BlockUpdate {
        private final short offset;
        private final BlockState block;

        public BlockUpdate(short param1, BlockState param2) {
            this.offset = param1;
            this.block = param2;
        }

        public BlockUpdate(short param1, LevelChunk param2) {
            this.offset = param1;
            this.block = param2.getBlockState(this.getPos());
        }

        public BlockPos getPos() {
            return new BlockPos(ClientboundChunkBlocksUpdatePacket.this.chunkPos.getBlockAt(this.offset >> 12 & 15, this.offset & 255, this.offset >> 8 & 15));
        }

        public short getOffset() {
            return this.offset;
        }

        public BlockState getBlock() {
            return this.block;
        }
    }
}
