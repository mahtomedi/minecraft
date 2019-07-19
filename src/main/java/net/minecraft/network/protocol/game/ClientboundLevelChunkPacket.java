package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLevelChunkPacket implements Packet<ClientGamePacketListener> {
    private int x;
    private int z;
    private int availableSections;
    private CompoundTag heightmaps;
    private byte[] buffer;
    private List<CompoundTag> blockEntitiesTags;
    private boolean fullChunk;

    public ClientboundLevelChunkPacket() {
    }

    public ClientboundLevelChunkPacket(LevelChunk param0, int param1) {
        ChunkPos var0 = param0.getPos();
        this.x = var0.x;
        this.z = var0.z;
        this.fullChunk = param1 == 65535;
        this.heightmaps = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var1 : param0.getHeightmaps()) {
            if (var1.getKey().sendToClient()) {
                this.heightmaps.put(var1.getKey().getSerializationKey(), new LongArrayTag(var1.getValue().getRawData()));
            }
        }

        this.buffer = new byte[this.calculateChunkSize(param0, param1)];
        this.availableSections = this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), param0, param1);
        this.blockEntitiesTags = Lists.newArrayList();

        for(Entry<BlockPos, BlockEntity> var2 : param0.getBlockEntities().entrySet()) {
            BlockPos var3 = var2.getKey();
            BlockEntity var4 = var2.getValue();
            int var5 = var3.getY() >> 4;
            if (this.isFullChunk() || (param1 & 1 << var5) != 0) {
                CompoundTag var6 = var4.getUpdateTag();
                this.blockEntitiesTags.add(var6);
            }
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = param0.readInt();
        this.z = param0.readInt();
        this.fullChunk = param0.readBoolean();
        this.availableSections = param0.readVarInt();
        this.heightmaps = param0.readNbt();
        int var0 = param0.readVarInt();
        if (var0 > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        } else {
            this.buffer = new byte[var0];
            param0.readBytes(this.buffer);
            int var1 = param0.readVarInt();
            this.blockEntitiesTags = Lists.newArrayList();

            for(int var2 = 0; var2 < var1; ++var2) {
                this.blockEntitiesTags.add(param0.readNbt());
            }

        }
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.x);
        param0.writeInt(this.z);
        param0.writeBoolean(this.fullChunk);
        param0.writeVarInt(this.availableSections);
        param0.writeNbt(this.heightmaps);
        param0.writeVarInt(this.buffer.length);
        param0.writeBytes(this.buffer);
        param0.writeVarInt(this.blockEntitiesTags.size());

        for(CompoundTag var0 : this.blockEntitiesTags) {
            param0.writeNbt(var0);
        }

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

    public int extractChunkData(FriendlyByteBuf param0, LevelChunk param1, int param2) {
        int var0 = 0;
        LevelChunkSection[] var1 = param1.getSections();
        int var2 = 0;

        for(int var3 = var1.length; var2 < var3; ++var2) {
            LevelChunkSection var4 = var1[var2];
            if (var4 != LevelChunk.EMPTY_SECTION && (!this.isFullChunk() || !var4.isEmpty()) && (param2 & 1 << var2) != 0) {
                var0 |= 1 << var2;
                var4.write(param0);
            }
        }

        if (this.isFullChunk()) {
            Biome[] var5 = param1.getBiomes();

            for(int var6 = 0; var6 < var5.length; ++var6) {
                param0.writeInt(Registry.BIOME.getId(var5[var6]));
            }
        }

        return var0;
    }

    protected int calculateChunkSize(LevelChunk param0, int param1) {
        int var0 = 0;
        LevelChunkSection[] var1 = param0.getSections();
        int var2 = 0;

        for(int var3 = var1.length; var2 < var3; ++var2) {
            LevelChunkSection var4 = var1[var2];
            if (var4 != LevelChunk.EMPTY_SECTION && (!this.isFullChunk() || !var4.isEmpty()) && (param1 & 1 << var2) != 0) {
                var0 += var4.getSerializedSize();
            }
        }

        if (this.isFullChunk()) {
            var0 += param0.getBiomes().length * 4;
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
    public int getAvailableSections() {
        return this.availableSections;
    }

    public boolean isFullChunk() {
        return this.fullChunk;
    }

    @OnlyIn(Dist.CLIENT)
    public CompoundTag getHeightmaps() {
        return this.heightmaps;
    }

    @OnlyIn(Dist.CLIENT)
    public List<CompoundTag> getBlockEntitiesTags() {
        return this.blockEntitiesTags;
    }
}
