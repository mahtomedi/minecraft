package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
    private final int warningBlocks;

    public ClientboundSetBorderWarningDistancePacket(WorldBorder param0) {
        this.warningBlocks = param0.getWarningBlocks();
    }

    public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf param0) {
        this.warningBlocks = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.warningBlocks);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderWarningDistance(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}
