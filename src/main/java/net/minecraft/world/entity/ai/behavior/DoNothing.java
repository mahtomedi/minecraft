package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DoNothing implements BehaviorControl<LivingEntity> {
    private final int minDuration;
    private final int maxDuration;
    private Behavior.Status status = Behavior.Status.STOPPED;
    private long endTimestamp;

    public DoNothing(int param0, int param1) {
        this.minDuration = param0;
        this.maxDuration = param1;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel param0, LivingEntity param1, long param2) {
        this.status = Behavior.Status.RUNNING;
        int var0 = this.minDuration + param0.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
        this.endTimestamp = param2 + (long)var0;
        return true;
    }

    @Override
    public final void tickOrStop(ServerLevel param0, LivingEntity param1, long param2) {
        if (param2 > this.endTimestamp) {
            this.doStop(param0, param1, param2);
        }

    }

    @Override
    public final void doStop(ServerLevel param0, LivingEntity param1, long param2) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }
}
