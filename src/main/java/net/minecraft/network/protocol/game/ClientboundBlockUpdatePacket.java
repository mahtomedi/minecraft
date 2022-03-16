package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final BlockState blockState;

    public ClientboundBlockUpdatePacket(BlockPos param0, BlockState param1) {
        this.pos = param0;
        this.blockState = param1;
    }

    public ClientboundBlockUpdatePacket(BlockGetter param0, BlockPos param1) {
        this(param1, param0.getBlockState(param1));
    }

    public ClientboundBlockUpdatePacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.blockState = param0.readById(Block.BLOCK_STATE_REGISTRY);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeId(Block.BLOCK_STATE_REGISTRY, this.blockState);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockUpdate(this);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
