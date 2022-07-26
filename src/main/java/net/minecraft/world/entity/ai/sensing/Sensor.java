package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private static final int DEFAULT_SCAN_RATE = 20;
    protected static final int TARGETING_RANGE = 16;
    private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0);
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat()
        .range(16.0)
        .ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0);
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat()
        .range(16.0)
        .ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat()
        .range(16.0)
        .ignoreLineOfSight()
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

    public static boolean isEntityTargetable(LivingEntity param0, LivingEntity param1) {
        return param0.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, param1)
            ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(param0, param1)
            : TARGET_CONDITIONS.test(param0, param1);
    }

    public static boolean isEntityAttackable(LivingEntity param0, LivingEntity param1) {
        return param0.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, param1)
            ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(param0, param1)
            : ATTACK_TARGET_CONDITIONS.test(param0, param1);
    }

    public static boolean isEntityAttackableIgnoringLineOfSight(LivingEntity param0, LivingEntity param1) {
        return param0.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, param1)
            ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(param0, param1)
            : ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(param0, param1);
    }
}
