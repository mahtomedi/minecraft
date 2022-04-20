package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import org.slf4j.Logger;

public class RateKickingConnection extends Connection {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component EXCEED_REASON = Component.translatable("disconnect.exceeded_packet_rate");
    private final int rateLimitPacketsPerSecond;

    public RateKickingConnection(int param0) {
        super(PacketFlow.SERVERBOUND);
        this.rateLimitPacketsPerSecond = param0;
    }

    @Override
    protected void tickSecond() {
        super.tickSecond();
        float var0 = this.getAverageReceivedPackets();
        if (var0 > (float)this.rateLimitPacketsPerSecond) {
            LOGGER.warn("Player exceeded rate-limit (sent {} packets per second)", var0);
            this.send(new ClientboundDisconnectPacket(EXCEED_REASON), param0 -> this.disconnect(EXCEED_REASON));
            this.setReadOnly();
        }

    }
}
