package net.minecraft.world.level.gameevent;

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
        public void post(GameEvent param0, Vec3 param1, GameEvent.Context param2) {
        }
    };

    boolean isEmpty();

    void register(GameEventListener var1);

    void unregister(GameEventListener var1);

    void post(GameEvent var1, Vec3 var2, GameEvent.Context var3);
}
