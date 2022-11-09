package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public abstract class OneShot<E extends LivingEntity> implements BehaviorControl<E>, Trigger<E> {
    private Behavior.Status status = Behavior.Status.STOPPED;

    @Override
    public final Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel param0, E param1, long param2) {
        if (this.trigger(param0, param1, param2)) {
            this.status = Behavior.Status.RUNNING;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tickOrStop(ServerLevel param0, E param1, long param2) {
        this.doStop(param0, param1, param2);
    }

    @Override
    public final void doStop(ServerLevel param0, E param1, long param2) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }
}
