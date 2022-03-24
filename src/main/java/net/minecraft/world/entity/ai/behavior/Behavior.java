package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class Behavior<E extends LivingEntity> {
    public static final int DEFAULT_DURATION = 60;
    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Behavior.Status status = Behavior.Status.STOPPED;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> param0) {
        this(param0, 60);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> param0, int param1) {
        this(param0, param1, param1);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> param0, int param1, int param2) {
        this.minDuration = param1;
        this.maxDuration = param2;
        this.entryCondition = param0;
    }

    public Behavior.Status getStatus() {
        return this.status;
    }

    public final boolean tryStart(ServerLevel param0, E param1, long param2) {
        if (this.hasRequiredMemories(param1) && this.checkExtraStartConditions(param0, param1)) {
            this.status = Behavior.Status.RUNNING;
            int var0 = this.minDuration + param0.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = param2 + (long)var0;
            this.start(param0, param1, param2);
            return true;
        } else {
            return false;
        }
    }

    protected void start(ServerLevel param0, E param1, long param2) {
    }

    public final void tickOrStop(ServerLevel param0, E param1, long param2) {
        if (!this.timedOut(param2) && this.canStillUse(param0, param1, param2)) {
            this.tick(param0, param1, param2);
        } else {
            this.doStop(param0, param1, param2);
        }

    }

    protected void tick(ServerLevel param0, E param1, long param2) {
    }

    public final void doStop(ServerLevel param0, E param1, long param2) {
        this.status = Behavior.Status.STOPPED;
        this.stop(param0, param1, param2);
    }

    protected void stop(ServerLevel param0, E param1, long param2) {
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return false;
    }

    protected boolean timedOut(long param0) {
        return param0 > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    private boolean hasRequiredMemories(E param0) {
        for(Entry<MemoryModuleType<?>, MemoryStatus> var0 : this.entryCondition.entrySet()) {
            MemoryModuleType<?> var1 = var0.getKey();
            MemoryStatus var2 = var0.getValue();
            if (!param0.getBrain().checkMemory(var1, var2)) {
                return false;
            }
        }

        return true;
    }

    public static enum Status {
        STOPPED,
        RUNNING;
    }
}
