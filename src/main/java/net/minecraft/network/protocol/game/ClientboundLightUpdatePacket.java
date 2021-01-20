package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
    private int x;
    private int z;
    private BitSet skyYMask = new BitSet();
    private BitSet blockYMask = new BitSet();
    private BitSet emptySkyYMask = new BitSet();
    private BitSet emptyBlockYMask = new BitSet();
    private final List<byte[]> skyUpdates = Lists.newArrayList();
    private final List<byte[]> blockUpdates = Lists.newArrayList();
    private boolean trustEdges;

    public ClientboundLightUpdatePacket() {
    }

    public ClientboundLightUpdatePacket(ChunkPos param0, LevelLightEngine param1, @Nullable BitSet param2, @Nullable BitSet param3, boolean param4) {
        this.x = param0.x;
        this.z = param0.z;
        this.trustEdges = param4;

        for(int var0 = 0; var0 < param1.getLightSectionCount(); ++var0) {
            if (param2 == null || param2.get(var0)) {
                prepareSectionData(param0, param1, LightLayer.SKY, var0, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }

            if (param3 == null || param3.get(var0)) {
                prepareSectionData(param0, param1, LightLayer.BLOCK, var0, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
            }
        }

    }

    private static void prepareSectionData(
        ChunkPos param0, LevelLightEngine param1, LightLayer param2, int param3, BitSet param4, BitSet param5, List<byte[]> param6
    ) {
        DataLayer var0 = param1.getLayerListener(param2).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + param3));
        if (var0 != null) {
            if (var0.isEmpty()) {
                param5.set(param3);
            } else {
                param4.set(param3);
                param6.add((byte[])var0.getData().clone());
            }
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = param0.readVarInt();
        this.z = param0.readVarInt();
        this.trustEdges = param0.readBoolean();
        this.skyYMask = param0.readBitSet();
        this.blockYMask = param0.readBitSet();
        this.emptySkyYMask = param0.readBitSet();
        this.emptyBlockYMask = param0.readBitSet();
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.skyUpdates.add(param0.readByteArray(2048));
        }

        int var2 = param0.readVarInt();

        for(int var3 = 0; var3 < var2; ++var3) {
            this.blockUpdates.add(param0.readByteArray(2048));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.x);
        param0.writeVarInt(this.z);
        param0.writeBoolean(this.trustEdges);
        param0.writeBitSet(this.skyYMask);
        param0.writeBitSet(this.blockYMask);
        param0.writeBitSet(this.emptySkyYMask);
        param0.writeBitSet(this.emptyBlockYMask);
        param0.writeVarInt(this.skyUpdates.size());

        for(byte[] var0 : this.skyUpdates) {
            param0.writeByteArray(var0);
        }

        param0.writeVarInt(this.blockUpdates.size());

        for(byte[] var1 : this.blockUpdates) {
            param0.writeByteArray(var1);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLightUpdatePacked(this);
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
    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    @OnlyIn(Dist.CLIENT)
    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getTrustEdges() {
        return this.trustEdges;
    }
}
