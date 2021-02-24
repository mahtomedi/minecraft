package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLevelChunkPacket implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final BitSet availableSections;
    private final CompoundTag heightmaps;
    private final int[] biomes;
    private final byte[] buffer;
    private final List<CompoundTag> blockEntitiesTags;

    public ClientboundLevelChunkPacket(LevelChunk param0) {
        ChunkPos var0 = param0.getPos();
        this.x = var0.x;
        this.z = var0.z;
        this.heightmaps = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var1 : param0.getHeightmaps()) {
            if (var1.getKey().sendToClient()) {
                this.heightmaps.put(var1.getKey().getSerializationKey(), new LongArrayTag(var1.getValue().getRawData()));
            }
        }

        this.biomes = param0.getBiomes().writeBiomes();
        this.buffer = new byte[this.calculateChunkSize(param0)];
        this.availableSections = this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), param0);
        this.blockEntitiesTags = Lists.newArrayList();

        for(Entry<BlockPos, BlockEntity> var2 : param0.getBlockEntities().entrySet()) {
            BlockEntity var3 = var2.getValue();
            CompoundTag var4 = var3.getUpdateTag();
            this.blockEntitiesTags.add(var4);
        }

    }

    public ClientboundLevelChunkPacket(FriendlyByteBuf param0) {
        this.x = param0.readInt();
        this.z = param0.readInt();
        this.availableSections = param0.readBitSet();
        this.heightmaps = param0.readNbt();
        this.biomes = param0.readVarIntArray(ChunkBiomeContainer.MAX_SIZE);
        int var0 = param0.readVarInt();
        if (var0 > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        } else {
            this.buffer = new byte[var0];
            param0.readBytes(this.buffer);
            this.blockEntitiesTags = param0.readList(FriendlyByteBuf::readNbt);
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.x);
        param0.writeInt(this.z);
        param0.writeBitSet(this.availableSections);
        param0.writeNbt(this.heightmaps);
        param0.writeVarIntArray(this.biomes);
        param0.writeVarInt(this.buffer.length);
        param0.writeBytes(this.buffer);
        param0.writeCollection(this.blockEntitiesTags, FriendlyByteBuf::writeNbt);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLevelChunk(this);
    }

    @OnlyIn(Dist.CLIENT)
    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf var0 = Unpooled.wrappedBuffer(this.buffer);
        var0.writerIndex(0);
        return var0;
    }

    public BitSet extractChunkData(FriendlyByteBuf param0, LevelChunk param1) {
        BitSet var0 = new BitSet();
        LevelChunkSection[] var1 = param1.getSections();
        int var2 = 0;

        for(int var3 = var1.length; var2 < var3; ++var2) {
            LevelChunkSection var4 = var1[var2];
            if (var4 != LevelChunk.EMPTY_SECTION && !var4.isEmpty()) {
                var0.set(var2);
                var4.write(param0);
            }
        }

        return var0;
    }

    protected int calculateChunkSize(LevelChunk param0) {
        int var0 = 0;
        LevelChunkSection[] var1 = param0.getSections();
        int var2 = 0;

        for(int var3 = var1.length; var2 < var3; ++var2) {
            LevelChunkSection var4 = var1[var2];
            if (var4 != LevelChunk.EMPTY_SECTION && !var4.isEmpty()) {
                var0 += var4.getSerializedSize();
            }
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getX() {
        return this.x;
    }

    @OnlyIn(Dist.CLIENT)
    public int getZ() {
        return this.z;
    }

    @OnlyIn(Dist.CLIENT)
    public BitSet getAvailableSections() {
        return this.availableSections;
    }

    @OnlyIn(Dist.CLIENT)
    public CompoundTag getHeightmaps() {
        return this.heightmaps;
    }

    @OnlyIn(Dist.CLIENT)
    public List<CompoundTag> getBlockEntitiesTags() {
        return this.blockEntitiesTags;
    }

    @OnlyIn(Dist.CLIENT)
    public int[] getBiomes() {
        return this.biomes;
    }
}
