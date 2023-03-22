package net.minecraft.util.profiling.jfr.stats;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public final class NetworkPacketSummary {
    private final NetworkPacketSummary.PacketCountAndSize totalPacketCountAndSize;
    private final List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors;
    private final Duration recordingDuration;

    public NetworkPacketSummary(Duration param0, List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> param1) {
        this.recordingDuration = param0;
        this.totalPacketCountAndSize = param1.stream()
            .map(Pair::getSecond)
            .reduce(NetworkPacketSummary.PacketCountAndSize::add)
            .orElseGet(() -> new NetworkPacketSummary.PacketCountAndSize(0L, 0L));
        this.largestSizeContributors = param1.stream()
            .sorted(Comparator.comparing(Pair::getSecond, NetworkPacketSummary.PacketCountAndSize.SIZE_THEN_COUNT))
            .limit(10L)
            .toList();
    }

    public double getCountsPerSecond() {
        return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
    }

    public double getSizePerSecond() {
        return (double)this.totalPacketCountAndSize.totalSize / (double)this.recordingDuration.getSeconds();
    }

    public long getTotalCount() {
        return this.totalPacketCountAndSize.totalCount;
    }

    public long getTotalSize() {
        return this.totalPacketCountAndSize.totalSize;
    }

    public List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors() {
        return this.largestSizeContributors;
    }

    public static record PacketCountAndSize(long totalCount, long totalSize) {
        static final Comparator<NetworkPacketSummary.PacketCountAndSize> SIZE_THEN_COUNT = Comparator.comparing(
                NetworkPacketSummary.PacketCountAndSize::totalSize
            )
            .thenComparing(NetworkPacketSummary.PacketCountAndSize::totalCount)
            .reversed();

        NetworkPacketSummary.PacketCountAndSize add(NetworkPacketSummary.PacketCountAndSize param0) {
            return new NetworkPacketSummary.PacketCountAndSize(this.totalCount + param0.totalCount, this.totalSize + param0.totalSize);
        }
    }

    public static record PacketIdentification(PacketFlow direction, int protocolId, int packetId) {
        private static final Map<NetworkPacketSummary.PacketIdentification, String> PACKET_NAME_BY_ID;

        public String packetName() {
            return PACKET_NAME_BY_ID.getOrDefault(this, "unknown");
        }

        public static NetworkPacketSummary.PacketIdentification from(RecordedEvent param0) {
            return new NetworkPacketSummary.PacketIdentification(
                param0.getEventType().getName().equals("minecraft.PacketSent") ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND,
                param0.getInt("protocolId"),
                param0.getInt("packetId")
            );
        }

        static {
            Builder<NetworkPacketSummary.PacketIdentification, String> var0 = ImmutableMap.builder();

            for(ConnectionProtocol var1 : ConnectionProtocol.values()) {
                for(PacketFlow var2 : PacketFlow.values()) {
                    Int2ObjectMap<Class<? extends Packet<?>>> var3 = var1.getPacketsByIds(var2);
                    var3.forEach(
                        (param3, param4) -> var0.put(new NetworkPacketSummary.PacketIdentification(var2, var1.getId(), param3), param4.getSimpleName())
                    );
                }
            }

            PACKET_NAME_BY_ID = var0.build();
        }
    }
}
