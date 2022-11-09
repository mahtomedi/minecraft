package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public final class MemoryAccessor<F extends K1, Value> {
    private final Brain<?> brain;
    private final MemoryModuleType<Value> memoryType;
    private final App<F, Value> value;

    public MemoryAccessor(Brain<?> param0, MemoryModuleType<Value> param1, App<F, Value> param2) {
        this.brain = param0;
        this.memoryType = param1;
        this.value = param2;
    }

    public App<F, Value> value() {
        return this.value;
    }

    public void set(Value param0) {
        this.brain.setMemory(this.memoryType, Optional.of(param0));
    }

    public void setOrErase(Optional<Value> param0) {
        this.brain.setMemory(this.memoryType, param0);
    }

    public void setWithExpiry(Value param0, long param1) {
        this.brain.setMemoryWithExpiry(this.memoryType, param0, param1);
    }

    public void erase() {
        this.brain.eraseMemory(this.memoryType);
    }
}
