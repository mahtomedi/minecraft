package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
    private final BlockHitResult blockHit;
    private final InteractionHand hand;
    private final int sequence;

    public ServerboundUseItemOnPacket(InteractionHand param0, BlockHitResult param1, int param2) {
        this.hand = param0;
        this.blockHit = param1;
        this.sequence = param2;
    }

    public ServerboundUseItemOnPacket(FriendlyByteBuf param0) {
        this.hand = param0.readEnum(InteractionHand.class);
        this.blockHit = param0.readBlockHitResult();
        this.sequence = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.hand);
        param0.writeBlockHitResult(this.blockHit);
        param0.writeVarInt(this.sequence);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleUseItemOn(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public BlockHitResult getHitResult() {
        return this.blockHit;
    }

    public int getSequence() {
        return this.sequence;
    }
}
