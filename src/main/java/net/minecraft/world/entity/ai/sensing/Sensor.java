package net.minecraft.world.entity.ai.sensing;

import java.util.Random;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public abstract class Sensor<E extends LivingEntity> {
    private static final Random RANDOM = new Random();
    private final int scanRate;
    private long timeToTick;

    public Sensor(int param0) {
        this.scanRate = param0;
        this.timeToTick = (long)RANDOM.nextInt(param0);
    }

    public Sensor() {
        this(20);
    }

    public final void tick(ServerLevel param0, E param1) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = (long)this.scanRate;
            this.doTick(param0, param1);
        }

    }

    protected abstract void doTick(ServerLevel var1, E var2);

    public abstract Set<MemoryModuleType<?>> requires();
}
