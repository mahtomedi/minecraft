package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> implements BehaviorControl<E> {
    private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final GateBehavior.OrderPolicy orderPolicy;
    private final GateBehavior.RunningPolicy runningPolicy;
    private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
    private Behavior.Status status = Behavior.Status.STOPPED;

    public GateBehavior(
        Map<MemoryModuleType<?>, MemoryStatus> param0,
        Set<MemoryModuleType<?>> param1,
        GateBehavior.OrderPolicy param2,
        GateBehavior.RunningPolicy param3,
        List<Pair<? extends BehaviorControl<? super E>, Integer>> param4
    ) {
        this.entryCondition = param0;
        this.exitErasedMemories = param1;
        this.orderPolicy = param2;
        this.runningPolicy = param3;
        param4.forEach(param0x -> this.behaviors.add(param0x.getFirst(), param0x.getSecond()));
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
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

    @Override
    public final boolean tryStart(ServerLevel param0, E param1, long param2) {
        if (this.hasRequiredMemories(param1)) {
            this.status = Behavior.Status.RUNNING;
            this.orderPolicy.apply(this.behaviors);
            this.runningPolicy.apply(this.behaviors.stream(), param0, param1, param2);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tickOrStop(ServerLevel param0, E param1, long param2) {
        this.behaviors.stream().filter(param0x -> param0x.getStatus() == Behavior.Status.RUNNING).forEach(param3 -> param3.tickOrStop(param0, param1, param2));
        if (this.behaviors.stream().noneMatch(param0x -> param0x.getStatus() == Behavior.Status.RUNNING)) {
            this.doStop(param0, param1, param2);
        }

    }

    @Override
    public final void doStop(ServerLevel param0, E param1, long param2) {
        this.status = Behavior.Status.STOPPED;
        this.behaviors.stream().filter(param0x -> param0x.getStatus() == Behavior.Status.RUNNING).forEach(param3 -> param3.doStop(param0, param1, param2));
        this.exitErasedMemories.forEach(param1.getBrain()::eraseMemory);
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        Set<? extends BehaviorControl<? super E>> var0 = this.behaviors
            .stream()
            .filter(param0 -> param0.getStatus() == Behavior.Status.RUNNING)
            .collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + var0;
    }

    public static enum OrderPolicy {
        ORDERED(param0 -> {
        }),
        SHUFFLED(ShufflingList::shuffle);

        private final Consumer<ShufflingList<?>> consumer;

        private OrderPolicy(Consumer<ShufflingList<?>> param0) {
            this.consumer = param0;
        }

        public void apply(ShufflingList<?> param0) {
            this.consumer.accept(param0);
        }
    }

    public static enum RunningPolicy {
        RUN_ONE {
            @Override
            public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> param0, ServerLevel param1, E param2, long param3) {
                param0.filter(param0x -> param0x.getStatus() == Behavior.Status.STOPPED)
                    .filter(param3x -> param3x.tryStart(param1, param2, param3))
                    .findFirst();
            }
        },
        TRY_ALL {
            @Override
            public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> param0, ServerLevel param1, E param2, long param3) {
                param0.filter(param0x -> param0x.getStatus() == Behavior.Status.STOPPED).forEach(param3x -> param3x.tryStart(param1, param2, param3));
            }
        };

        public abstract <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> var1, ServerLevel var2, E var3, long var4);
    }
}
