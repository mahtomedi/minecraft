package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final BlockEntityType<?> type;
    @Nullable
    private final CompoundTag tag;

    public static ClientboundBlockEntityDataPacket create(BlockEntity param0) {
        return new ClientboundBlockEntityDataPacket(param0.getBlockPos(), param0.getType(), param0.getUpdateTag());
    }

    private ClientboundBlockEntityDataPacket(BlockPos param0, BlockEntityType<?> param1, CompoundTag param2) {
        this.pos = param0;
        this.type = param1;
        this.tag = param2.isEmpty() ? null : param2;
    }

    public ClientboundBlockEntityDataPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.type = Registry.BLOCK_ENTITY_TYPE.byId(param0.readVarInt());
        this.tag = param0.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(this.type));
        param0.writeNbt(this.tag);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockEntityData(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }
}
