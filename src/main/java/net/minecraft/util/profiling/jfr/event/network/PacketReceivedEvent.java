package net.minecraft.util.profiling.jfr.event.network;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.PacketRead")
@Label("Network packet read")
@DontObfuscate
public class PacketReceivedEvent extends PacketEvent {
    public static final String NAME = "minecraft.PacketRead";
    public static final EventType TYPE = EventType.getEventType(PacketReceivedEvent.class);

    public PacketReceivedEvent(String param0, SocketAddress param1, int param2) {
        super(param0, param1, param2);
    }
}
