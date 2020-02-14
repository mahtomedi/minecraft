package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class EraseMemoryIf<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> predicate;
    private final MemoryModuleType<?> memoryType;

    public EraseMemoryIf(Predicate<E> param0, MemoryModuleType<?> param1) {
        super(ImmutableMap.of(param1, MemoryStatus.VALUE_PRESENT));
        this.predicate = param0;
        this.memoryType = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.predicate.test(param1);
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        param1.getBrain().eraseMemory(this.memoryType);
    }
}
