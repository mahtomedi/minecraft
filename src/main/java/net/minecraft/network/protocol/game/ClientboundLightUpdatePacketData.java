package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacketData {
    private final BitSet skyYMask;
    private final BitSet blockYMask;
    private final BitSet emptySkyYMask;
    private final BitSet emptyBlockYMask;
    private final List<byte[]> skyUpdates;
    private final List<byte[]> blockUpdates;
    private final boolean trustEdges;

    public ClientboundLightUpdatePacketData(ChunkPos param0, LevelLightEngine param1, @Nullable BitSet param2, @Nullable BitSet param3, boolean param4) {
        this.trustEdges = param4;
        this.skyYMask = new BitSet();
        this.blockYMask = new BitSet();
        this.emptySkyYMask = new BitSet();
        this.emptyBlockYMask = new BitSet();
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();

        for(int var0 = 0; var0 < param1.getLightSectionCount(); ++var0) {
            if (param2 == null || param2.get(var0)) {
                this.prepareSectionData(param0, param1, LightLayer.SKY, var0, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }

            if (param3 == null || param3.get(var0)) {
                this.prepareSectionData(param0, param1, LightLayer.BLOCK, var0, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
            }
        }

    }

    public ClientboundLightUpdatePacketData(FriendlyByteBuf param0, int param1, int param2) {
        this.trustEdges = param0.readBoolean();
        this.skyYMask = param0.readBitSet();
        this.blockYMask = param0.readBitSet();
        this.emptySkyYMask = param0.readBitSet();
        this.emptyBlockYMask = param0.readBitSet();
        this.skyUpdates = param0.readList(param0x -> param0x.readByteArray(2048));
        this.blockUpdates = param0.readList(param0x -> param0x.readByteArray(2048));
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.trustEdges);
        param0.writeBitSet(this.skyYMask);
        param0.writeBitSet(this.blockYMask);
        param0.writeBitSet(this.emptySkyYMask);
        param0.writeBitSet(this.emptyBlockYMask);
        param0.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
        param0.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
    }

    private void prepareSectionData(ChunkPos param0, LevelLightEngine param1, LightLayer param2, int param3, BitSet param4, BitSet param5, List<byte[]> param6) {
        DataLayer var0 = param1.getLayerListener(param2).getDataLayerData(SectionPos.of(param0, param1.getMinLightSection() + param3));
        if (var0 != null) {
            if (var0.isEmpty()) {
                param5.set(param3);
            } else {
                param4.set(param3);
                param6.add(var0.copy().getData());
            }
        }

    }

    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }

    public boolean getTrustEdges() {
        return this.trustEdges;
    }
}
