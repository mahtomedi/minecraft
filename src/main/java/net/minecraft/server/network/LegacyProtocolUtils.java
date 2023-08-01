package net.minecraft.server.network;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class LegacyProtocolUtils {
    public static final int CUSTOM_PAYLOAD_PACKET_ID = 250;
    public static final String CUSTOM_PAYLOAD_PACKET_PING_CHANNEL = "MC|PingHost";
    public static final int GET_INFO_PACKET_ID = 254;
    public static final int GET_INFO_PACKET_VERSION_1 = 1;
    public static final int DISCONNECT_PACKET_ID = 255;
    public static final int FAKE_PROTOCOL_VERSION = 127;

    public static void writeLegacyString(ByteBuf param0, String param1) {
        param0.writeShort(param1.length());
        param0.writeCharSequence(param1, StandardCharsets.UTF_16BE);
    }

    public static String readLegacyString(ByteBuf param0) {
        int var0 = param0.readShort();
        int var1 = var0 * 2;
        String var2 = param0.toString(param0.readerIndex(), var1, StandardCharsets.UTF_16BE);
        param0.skipBytes(var1);
        return var2;
    }
}
