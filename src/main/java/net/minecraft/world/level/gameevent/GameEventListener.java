package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface GameEventListener {
    PositionSource getListenerSource();

    int getListenerRadius();

    boolean handleGameEvent(ServerLevel var1, GameEvent var2, GameEvent.Context var3, Vec3 var4);

    default GameEventListener.DeliveryMode getDeliveryMode() {
        return GameEventListener.DeliveryMode.UNSPECIFIED;
    }

    public static enum DeliveryMode {
        UNSPECIFIED,
        BY_DISTANCE;
    }

    public interface Holder<T extends GameEventListener> {
        T getListener();
    }
}
