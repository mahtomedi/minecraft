package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface GameEventListener {
    PositionSource getListenerSource();

    int getListenerRadius();

    boolean handleGameEvent(ServerLevel var1, GameEvent var2, @Nullable Entity var3, Vec3 var4);
}
