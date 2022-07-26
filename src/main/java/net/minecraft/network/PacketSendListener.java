package net.minecraft.network;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;

public interface PacketSendListener {
    static PacketSendListener thenRun(final Runnable param0) {
        return new PacketSendListener() {
            @Override
            public void onSuccess() {
                param0.run();
            }

            @Nullable
            @Override
            public Packet<?> onFailure() {
                param0.run();
                return null;
            }
        };
    }

    static PacketSendListener exceptionallySend(final Supplier<Packet<?>> param0) {
        return new PacketSendListener() {
            @Nullable
            @Override
            public Packet<?> onFailure() {
                return param0.get();
            }
        };
    }

    default void onSuccess() {
    }

    @Nullable
    default Packet<?> onFailure() {
        return null;
    }
}
