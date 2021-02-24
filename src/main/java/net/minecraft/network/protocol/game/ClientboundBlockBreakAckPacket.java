package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientboundBlockBreakAckPacket implements Packet<ClientGamePacketListener> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockPos pos;
    private final BlockState state;
    private final ServerboundPlayerActionPacket.Action action;
    private final boolean allGood;

    public ClientboundBlockBreakAckPacket(BlockPos param0, BlockState param1, ServerboundPlayerActionPacket.Action param2, boolean param3, String param4) {
        this.pos = param0.immutable();
        this.state = param1;
        this.action = param2;
        this.allGood = param3;
    }

    public ClientboundBlockBreakAckPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.state = Block.BLOCK_STATE_REGISTRY.byId(param0.readVarInt());
        this.action = param0.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.allGood = param0.readBoolean();
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

    @OnlyIn(Dist.CLIENT)
    public BlockState getState() {
        return this.state;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean allGood() {
        return this.allGood;
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlayerActionPacket.Action action() {
        return this.action;
    }
}
