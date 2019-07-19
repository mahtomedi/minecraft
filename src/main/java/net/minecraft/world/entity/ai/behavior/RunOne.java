package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunOne<E extends LivingEntity> extends GateBehavior<E> {
    public RunOne(List<Pair<Behavior<? super E>, Integer>> param0) {
        this(ImmutableMap.of(), param0);
    }

    public RunOne(Map<MemoryModuleType<?>, MemoryStatus> param0, List<Pair<Behavior<? super E>, Integer>> param1) {
        super(param0, ImmutableSet.of(), GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE, param1);
    }
}
