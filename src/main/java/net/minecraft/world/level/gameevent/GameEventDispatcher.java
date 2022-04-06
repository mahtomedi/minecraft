package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface GameEventDispatcher {
    GameEventDispatcher NOOP = new GameEventDispatcher() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void register(GameEventListener param0) {
        }

        @Override
        public void unregister(GameEventListener param0) {
        }

        @Override
        public void post(GameEvent param0, @Nullable Entity param1, Vec3 param2) {
        }
    };

    boolean isEmpty();

    void register(GameEventListener var1);

    void unregister(GameEventListener var1);

    void post(GameEvent var1, @Nullable Entity var2, Vec3 var3);
}
