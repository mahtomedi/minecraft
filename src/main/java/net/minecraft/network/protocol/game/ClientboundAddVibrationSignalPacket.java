package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class ClientboundAddVibrationSignalPacket implements Packet<ClientGamePacketListener> {
    private final VibrationPath vibrationPath;

    public ClientboundAddVibrationSignalPacket(VibrationPath param0) {
        this.vibrationPath = param0;
    }

    public ClientboundAddVibrationSignalPacket(FriendlyByteBuf param0) {
        this.vibrationPath = VibrationPath.read(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        VibrationPath.write(param0, this.vibrationPath);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddVibrationSignal(this);
    }

    public VibrationPath getVibrationPath() {
        return this.vibrationPath;
    }
}
