package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EuclideanGameEventDispatcher implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Level level;

    public EuclideanGameEventDispatcher(Level param0) {
        this.level = param0;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void register(GameEventListener param0) {
        this.listeners.add(param0);
        DebugPackets.sendGameEventListenerInfo(this.level, param0);
    }

    @Override
    public void unregister(GameEventListener param0) {
        this.listeners.remove(param0);
    }

    @Override
    public void post(GameEvent param0, @Nullable Entity param1, BlockPos param2) {
        boolean var0 = false;

        for(GameEventListener var1 : this.listeners) {
            if (this.postToListener(this.level, param0, param1, param2, var1)) {
                var0 = true;
            }
        }

        if (var0) {
            DebugPackets.sendGameEventInfo(this.level, param0, param2);
        }

    }

    private boolean postToListener(Level param0, GameEvent param1, @Nullable Entity param2, BlockPos param3, GameEventListener param4) {
        Optional<BlockPos> var0 = param4.getListenerSource().getPosition(param0);
        if (!var0.isPresent()) {
            return false;
        } else {
            double var1 = var0.get().distSqr(param3, false);
            int var2 = param4.getListenerRadius() * param4.getListenerRadius();
            return var1 <= (double)var2 && param4.handleGameEvent(param0, param1, param2, param3);
        }
    }
}
