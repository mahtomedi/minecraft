package net.minecraft.network;

import net.minecraft.network.chat.Component;

public interface PacketListener {
    void onDisconnect(Component var1);

    boolean isAcceptingMessages();

    default boolean shouldPropagateHandlingExceptions() {
        return true;
    }
}
