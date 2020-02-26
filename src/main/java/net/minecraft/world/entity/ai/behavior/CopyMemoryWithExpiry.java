package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CopyMemoryWithExpiry<E extends Mob, T> extends Behavior<E> {
    private final Predicate<E> predicate;
    private final MemoryModuleType<? extends T> sourceMemory;
    private final MemoryModuleType<T> targetMemory;
    private final IntRange durationOfCopy;

    public CopyMemoryWithExpiry(Predicate<E> param0, MemoryModuleType<? extends T> param1, MemoryModuleType<T> param2, IntRange param3) {
        super(ImmutableMap.of(param1, MemoryStatus.VALUE_PRESENT, param2, MemoryStatus.VALUE_ABSENT));
        this.predicate = param0;
        this.sourceMemory = param1;
        this.targetMemory = param2;
        this.durationOfCopy = param3;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.predicate.test(param1);
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.setMemoryWithExpiry(this.targetMemory, var0.getMemory(this.sourceMemory).get(), param2, (long)this.durationOfCopy.randomValue(param0.random));
    }
}
