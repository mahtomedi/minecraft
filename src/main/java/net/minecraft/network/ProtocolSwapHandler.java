package net.minecraft.network;

import io.netty.util.Attribute;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
    static void swapProtocolIfNeeded(Attribute<ConnectionProtocol.CodecData<?>> param0, Packet<?> param1) {
        ConnectionProtocol var0 = param1.nextProtocol();
        if (var0 != null) {
            ConnectionProtocol.CodecData<?> var1 = param0.get();
            ConnectionProtocol var2 = var1.protocol();
            if (var0 != var2) {
                ConnectionProtocol.CodecData<?> var3 = var0.codec(var1.flow());
                param0.set(var3);
            }
        }

    }
}
