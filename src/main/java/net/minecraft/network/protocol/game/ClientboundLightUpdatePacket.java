package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
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
    private long skyYMask;
    private long blockYMask;
    private long emptySkyYMask;
    private long emptyBlockYMask;
    private List<byte[]> skyUpdates;
    private List<byte[]> blockUpdates;
    private boolean trustEdges;

    public ClientboundLightUpdatePacket() {
    }

    public ClientboundLightUpdatePacket(ChunkPos param0, LevelLightEngine param1, boolean param2) {
        this.x = param0.x;
        this.z = param0.z;
        this.trustEdges = param2;
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();

        for(int var0 = 0; var0 < param1.getLightSectionCount(); ++var0) {
            DataLayer var1 = param1.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + var0));
            DataLayer var2 = param1.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + var0));
            if (var1 != null) {
                if (var1.isEmpty()) {
                    this.emptySkyYMask |= 1L << var0;
                } else {
                    this.skyYMask |= 1L << var0;
                    this.skyUpdates.add((byte[])var1.getData().clone());
                }
            }

            if (var2 != null) {
                if (var2.isEmpty()) {
                    this.emptyBlockYMask |= 1L << var0;
                } else {
                    this.blockYMask |= 1L << var0;
                    this.blockUpdates.add((byte[])var2.getData().clone());
                }
            }
        }

    }

    public ClientboundLightUpdatePacket(ChunkPos param0, LevelLightEngine param1, int param2, int param3, boolean param4) {
        this.x = param0.x;
        this.z = param0.z;
        this.trustEdges = param4;
        this.skyYMask = (long)param2;
        this.blockYMask = (long)param3;
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();

        for(int var0 = 0; var0 < param1.getLightSectionCount(); ++var0) {
            if ((this.skyYMask & 1L << var0) != 0L) {
                DataLayer var1 = param1.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + var0));
                if (var1 != null && !var1.isEmpty()) {
                    this.skyUpdates.add((byte[])var1.getData().clone());
                } else {
                    this.skyYMask &= ~(1L << var0);
                    if (var1 != null) {
                        this.emptySkyYMask |= 1L << var0;
                    }
                }
            }

            if ((this.blockYMask & 1L << var0) != 0L) {
                DataLayer var2 = param1.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + var0));
                if (var2 != null && !var2.isEmpty()) {
                    this.blockUpdates.add((byte[])var2.getData().clone());
                } else {
                    this.blockYMask &= ~(1L << var0);
                    if (var2 != null) {
                        this.emptyBlockYMask |= 1L << var0;
                    }
                }
            }
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = param0.readVarInt();
        this.z = param0.readVarInt();
        this.trustEdges = param0.readBoolean();
        this.skyYMask = param0.readVarLong();
        this.blockYMask = param0.readVarLong();
        this.emptySkyYMask = param0.readVarLong();
        this.emptyBlockYMask = param0.readVarLong();
        this.skyUpdates = Lists.newArrayList();

        for(int var0 = 0; var0 < 64; ++var0) {
            if ((this.skyYMask & 1L << var0) != 0L) {
                this.skyUpdates.add(param0.readByteArray(2048));
            }
        }

        this.blockUpdates = Lists.newArrayList();

        for(int var1 = 0; var1 < 64; ++var1) {
            if ((this.blockYMask & 1L << var1) != 0L) {
                this.blockUpdates.add(param0.readByteArray(2048));
            }
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.x);
        param0.writeVarInt(this.z);
        param0.writeBoolean(this.trustEdges);
        param0.writeVarLong(this.skyYMask);
        param0.writeVarLong(this.blockYMask);
        param0.writeVarLong(this.emptySkyYMask);
        param0.writeVarLong(this.emptyBlockYMask);

        for(byte[] var0 : this.skyUpdates) {
            param0.writeByteArray(var0);
        }

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
    public long getSkyYMask() {
        return this.skyYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public long getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    @OnlyIn(Dist.CLIENT)
    public long getBlockYMask() {
        return this.blockYMask;
    }

    @OnlyIn(Dist.CLIENT)
    public long getEmptyBlockYMask() {
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
