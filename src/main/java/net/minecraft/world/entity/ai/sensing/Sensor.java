package net.minecraft.world.entity.ai.sensing;

import java.util.Random;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
    private static final Random RANDOM = new Random();
    private static final TargetingConditions TARGET_CONDITIONS = new TargetingConditions().range(16.0).allowSameTeam().allowNonAttackable();
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = new TargetingConditions()
        .range(16.0)
        .allowSameTeam()
        .allowNonAttackable()
        .ignoreInvisibilityTesting();
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

    protected static boolean isEntityTargetable(LivingEntity param0, LivingEntity param1) {
        return param0.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, param1)
            ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(param0, param1)
            : TARGET_CONDITIONS.test(param0, param1);
    }
}
