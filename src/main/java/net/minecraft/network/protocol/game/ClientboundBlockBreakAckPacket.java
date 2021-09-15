package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record ClientboundBlockBreakAckPacket(BlockPos pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean allGood)
    implements Packet<ClientGamePacketListener> {
    private static final Logger LOGGER = LogManager.getLogger();

    public ClientboundBlockBreakAckPacket(BlockPos param0, BlockState param1, ServerboundPlayerActionPacket.Action param2, boolean param3, String param4) {
        this(param0, param1, param2, param3);
    }

    public ClientboundBlockBreakAckPacket(BlockPos param0, BlockState param1, ServerboundPlayerActionPacket.Action param2, boolean param3) {
        param0 = param0.immutable();
        this.pos = param0;
        this.state = param1;
        this.action = param2;
        this.allGood = param3;
    }

    public ClientboundBlockBreakAckPacket(FriendlyByteBuf param0) {
        this(
            param0.readBlockPos(),
            Block.BLOCK_STATE_REGISTRY.byId(param0.readVarInt()),
            param0.readEnum(ServerboundPlayerActionPacket.Action.class),
            param0.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeVarInt(Block.getId(this.state));
        param0.writeEnum(this.action);
        param0.writeBoolean(this.allGood);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockBreakAck(this);
    }
}
