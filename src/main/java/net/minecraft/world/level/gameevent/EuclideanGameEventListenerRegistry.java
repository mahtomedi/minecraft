package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry implements GameEventListenerRegistry {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing;
    private final ServerLevel level;
    private final int sectionY;
    private final EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction;

    public EuclideanGameEventListenerRegistry(ServerLevel param0, int param1, EuclideanGameEventListenerRegistry.OnEmptyAction param2) {
        this.level = param0;
        this.sectionY = param1;
        this.onEmptyAction = param2;
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

        if (this.listeners.isEmpty()) {
            this.onEmptyAction.apply(this.sectionY);
        }

    }

    @Override
    public boolean visitInRangeListeners(GameEvent param0, Vec3 param1, GameEvent.Context param2, GameEventListenerRegistry.ListenerVisitor param3) {
        this.processing = true;
        boolean var0 = false;

        try {
            Iterator<GameEventListener> var1 = this.listeners.iterator();

            while(var1.hasNext()) {
                GameEventListener var2 = var1.next();
                if (this.listenersToRemove.remove(var2)) {
                    var1.remove();
                } else {
                    Optional<Vec3> var3 = getPostableListenerPosition(this.level, param1, var2);
                    if (var3.isPresent()) {
                        param3.visit(var2, var3.get());
                        var0 = true;
                    }
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

        return var0;
    }

    private static Optional<Vec3> getPostableListenerPosition(ServerLevel param0, Vec3 param1, GameEventListener param2) {
        Optional<Vec3> var0 = param2.getListenerSource().getPosition(param0);
        if (var0.isEmpty()) {
            return Optional.empty();
        } else {
            double var1 = var0.get().distanceToSqr(param1);
            int var2 = param2.getListenerRadius() * param2.getListenerRadius();
            return var1 > (double)var2 ? Optional.empty() : var0;
        }
    }

    @FunctionalInterface
    public interface OnEmptyAction {
        void apply(int var1);
    }
}
