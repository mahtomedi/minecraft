package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunIf<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> predicate;
    private final Behavior<? super E> wrappedBehavior;
    private final boolean checkWhileRunningAlso;

    public RunIf(Map<MemoryModuleType<?>, MemoryStatus> param0, Predicate<E> param1, Behavior<? super E> param2, boolean param3) {
        super(mergeMaps(param0, param2.entryCondition));
        this.predicate = param1;
        this.wrappedBehavior = param2;
        this.checkWhileRunningAlso = param3;
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> mergeMaps(
        Map<MemoryModuleType<?>, MemoryStatus> param0, Map<MemoryModuleType<?>, MemoryStatus> param1
    ) {
        Map<MemoryModuleType<?>, MemoryStatus> var0 = Maps.newHashMap();
        var0.putAll(param0);
        var0.putAll(param1);
        return var0;
    }

    public RunIf(Predicate<E> param0, Behavior<? super E> param1) {
        this(ImmutableMap.of(), param0, param1, false);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.predicate.test(param1) && this.wrappedBehavior.checkExtraStartConditions(param0, param1);
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return this.checkWhileRunningAlso && this.predicate.test(param1) && this.wrappedBehavior.canStillUse(param0, param1, param2);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        this.wrappedBehavior.start(param0, param1, param2);
    }

    @Override
    protected void tick(ServerLevel param0, E param1, long param2) {
        this.wrappedBehavior.tick(param0, param1, param2);
    }

    @Override
    protected void stop(ServerLevel param0, E param1, long param2) {
        this.wrappedBehavior.stop(param0, param1, param2);
    }

    @Override
    public String toString() {
        return "RunIf: " + this.wrappedBehavior;
    }
}
