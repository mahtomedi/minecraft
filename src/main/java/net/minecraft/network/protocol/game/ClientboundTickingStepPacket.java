package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStepPacket(int tickSteps) implements Packet<ClientGamePacketListener> {
    public ClientboundTickingStepPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt());
    }

    public static ClientboundTickingStepPacket from(TickRateManager param0) {
        return new ClientboundTickingStepPacket(param0.frozenTicksToRun());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.tickSteps);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTickingStep(this);
    }
}
