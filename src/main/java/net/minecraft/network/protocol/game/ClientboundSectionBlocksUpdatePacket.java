package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
    private final SectionPos sectionPos;
    private final short[] positions;
    private final BlockState[] states;
    private final boolean suppressLightUpdates;

    public ClientboundSectionBlocksUpdatePacket(SectionPos param0, ShortSet param1, LevelChunkSection param2, boolean param3) {
        this.sectionPos = param0;
        this.suppressLightUpdates = param3;
        int var0 = param1.size();
        this.positions = new short[var0];
        this.states = new BlockState[var0];
        int var1 = 0;

        for(short var2 : param1) {
            this.positions[var1] = var2;
            this.states[var1] = param2.getBlockState(SectionPos.sectionRelativeX(var2), SectionPos.sectionRelativeY(var2), SectionPos.sectionRelativeZ(var2));
            ++var1;
        }

    }

    public ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf param0) {
        this.sectionPos = SectionPos.of(param0.readLong());
        this.suppressLightUpdates = param0.readBoolean();
        int var0 = param0.readVarInt();
        this.positions = new short[var0];
        this.states = new BlockState[var0];

        for(int var1 = 0; var1 < var0; ++var1) {
            long var2 = param0.readVarLong();
            this.positions[var1] = (short)((int)(var2 & 4095L));
            this.states[var1] = Block.BLOCK_STATE_REGISTRY.byId((int)(var2 >>> 12));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.sectionPos.asLong());
        param0.writeBoolean(this.suppressLightUpdates);
        param0.writeVarInt(this.positions.length);

        for(int var0 = 0; var0 < this.positions.length; ++var0) {
            param0.writeVarLong((long)(Block.getId(this.states[var0]) << 12 | this.positions[var0]));
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunkBlocksUpdate(this);
    }

    public void runUpdates(BiConsumer<BlockPos, BlockState> param0) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < this.positions.length; ++var1) {
            short var2 = this.positions[var1];
            var0.set(this.sectionPos.relativeToBlockX(var2), this.sectionPos.relativeToBlockY(var2), this.sectionPos.relativeToBlockZ(var2));
            param0.accept(var0, this.states[var1]);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldSuppressLightUpdates() {
        return this.suppressLightUpdates;
    }
}
