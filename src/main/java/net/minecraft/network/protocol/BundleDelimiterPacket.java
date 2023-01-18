package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public class BundleDelimiterPacket<T extends PacketListener> implements Packet<T> {
    @Override
    public final void write(FriendlyByteBuf param0) {
    }

    @Override
    public final void handle(T param0) {
        throw new AssertionError("This packet should be handled by pipeline");
    }
}
