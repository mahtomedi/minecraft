package net.minecraft.client.multiplayer;

import net.minecraft.Util;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.util.SampleLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PingDebugMonitor {
    private final ClientPacketListener connection;
    private final SampleLogger delayTimer;

    public PingDebugMonitor(ClientPacketListener param0, SampleLogger param1) {
        this.connection = param0;
        this.delayTimer = param1;
    }

    public void tick() {
        this.connection.send(new ServerboundPingRequestPacket(Util.getMillis()));
    }

    public void onPongReceived(ClientboundPongResponsePacket param0) {
        this.delayTimer.logSample(Util.getMillis() - param0.getTime());
    }
}
