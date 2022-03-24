package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventDispatcher implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing;
    private final ServerLevel level;

    public EuclideanGameEventDispatcher(ServerLevel param0) {
        this.level = param0;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void register(GameEventListener param0) {
        if (this.processing) {
            this.listenersToAdd.add(param0);
        } else {
            this.listeners.add(param0);
        }

        DebugPackets.sendGameEventListenerInfo(this.level, param0);
    }

    @Override
    public void unregister(GameEventListener param0) {
        if (this.processing) {
            this.listenersToRemove.add(param0);
        } else {
            this.listeners.remove(param0);
        }

    }

    @Override
    public void post(GameEvent param0, @Nullable Entity param1, Vec3 param2) {
        boolean var0 = false;
        this.processing = true;

        try {
            Iterator<GameEventListener> var1 = this.listeners.iterator();

            while(var1.hasNext()) {
                GameEventListener var2 = var1.next();
                if (this.listenersToRemove.remove(var2)) {
                    var1.remove();
                } else if (postToListener(this.level, param0, param1, param2, var2)) {
                    var0 = true;
                }
            }
        } finally {
            this.processing = false;
        }

        if (!this.listenersToAdd.isEmpty()) {
            this.listeners.addAll(this.listenersToAdd);
            this.listenersToAdd.clear();
        }

        if (!this.listenersToRemove.isEmpty()) {
            this.listeners.removeAll(this.listenersToRemove);
            this.listenersToRemove.clear();
        }

        if (var0) {
            DebugPackets.sendGameEventInfo(this.level, param0, param2);
        }

    }

    private static boolean postToListener(ServerLevel param0, GameEvent param1, @Nullable Entity param2, Vec3 param3, GameEventListener param4) {
        Optional<Vec3> var0 = param4.getListenerSource().getPosition(param0);
        if (var0.isEmpty()) {
            return false;
        } else {
            double var1 = var0.get().distanceToSqr(param3);
            int var2 = param4.getListenerRadius() * param4.getListenerRadius();
            return var1 <= (double)var2 && param4.handleGameEvent(param0, param1, param2, param3);
        }
    }
}
