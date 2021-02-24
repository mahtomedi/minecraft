package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
    private final BlockHitResult blockHit;
    private final InteractionHand hand;

    @OnlyIn(Dist.CLIENT)
    public ServerboundUseItemOnPacket(InteractionHand param0, BlockHitResult param1) {
        this.hand = param0;
        this.blockHit = param1;
    }

    public ServerboundUseItemOnPacket(FriendlyByteBuf param0) {
        this.hand = param0.readEnum(InteractionHand.class);
        this.blockHit = param0.readBlockHitResult();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.hand);
        param0.writeBlockHitResult(this.blockHit);
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
}
