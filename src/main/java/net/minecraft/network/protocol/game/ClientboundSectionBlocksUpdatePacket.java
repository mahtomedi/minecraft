package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.io.IOException;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
    private SectionPos sectionPos;
    private short[] positions;
    private BlockState[] states;

    public ClientboundSectionBlocksUpdatePacket() {
    }

    public ClientboundSectionBlocksUpdatePacket(SectionPos param0, ShortSet param1, LevelChunkSection param2) {
        this.sectionPos = param0;
        this.initFields(param1.size());
        int var0 = 0;

        for(short var1 : param1) {
            this.positions[var0] = var1;
            this.states[var0] = param2.getBlockState(SectionPos.sectionRelativeX(var1), SectionPos.sectionRelativeY(var1), SectionPos.sectionRelativeZ(var1));
            ++var0;
        }

    }

    private void initFields(int param0) {
        this.positions = new short[param0];
        this.states = new BlockState[param0];
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.sectionPos = SectionPos.of(param0.readLong());
        int var0 = param0.readVarInt();
        this.initFields(var0);

        for(int var1 = 0; var1 < this.positions.length; ++var1) {
            long var2 = param0.readVarLong();
            this.positions[var1] = (short)((int)(var2 & 4095L));
            this.states[var1] = Block.BLOCK_STATE_REGISTRY.byId((int)(var2 >>> 12));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeLong(this.sectionPos.asLong());
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
}
