package net.minecraft.util.profiling.jfr.event.network;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.PacketSent")
@Label("Network packet sent")
@DontObfuscate
public class PacketSentEvent extends PacketEvent {
    public static final String NAME = "minecraft.PacketSent";
    public static final EventType TYPE = EventType.getEventType(PacketSentEvent.class);

    public PacketSentEvent(String param0, SocketAddress param1, int param2) {
        super(param0, param1, param2);
    }
}
