package net.minecraft.util.profiling.jfr.event;

import java.net.SocketAddress;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Category({"Minecraft", "Network"})
@StackTrace(false)
@Enabled(false)
public abstract class PacketEvent extends Event {
    @Name("protocolId")
    @Label("Protocol Id")
    public final int protocolId;
    @Name("packetId")
    @Label("Packet Id")
    public final int packetId;
    @Name("remoteAddress")
    @Label("Remote Address")
    public final String remoteAddress;
    @Name("bytes")
    @Label("Bytes")
    @DataAmount
    public final int bytes;

    public PacketEvent(int param0, int param1, SocketAddress param2, int param3) {
        this.protocolId = param0;
        this.packetId = param1;
        this.remoteAddress = param2.toString();
        this.bytes = param3;
    }

    public static final class Fields {
        public static final String REMOTE_ADDRESS = "remoteAddress";
        public static final String PROTOCOL_ID = "protocolId";
        public static final String PACKET_ID = "packetId";
        public static final String BYTES = "bytes";

        private Fields() {
        }
    }
}
