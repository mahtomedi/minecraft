package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddVibrationSignalPacket implements Packet<ClientGamePacketListener> {
    private VibrationPath vibrationPath;

    public ClientboundAddVibrationSignalPacket() {
    }

    public ClientboundAddVibrationSignalPacket(VibrationPath param0) {
        this.vibrationPath = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.vibrationPath = VibrationPath.read(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        VibrationPath.write(param0, this.vibrationPath);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddVibrationSignal(this);
    }

    @OnlyIn(Dist.CLIENT)
    public VibrationPath getVibrationPath() {
        return this.vibrationPath;
    }
}
