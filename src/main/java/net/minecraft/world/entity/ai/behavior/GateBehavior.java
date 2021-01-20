package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> extends Behavior<E> {
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final GateBehavior.OrderPolicy orderPolicy;
    private final GateBehavior.RunningPolicy runningPolicy;
    private final WeightedList<Behavior<? super E>> behaviors = new WeightedList<>();

    public GateBehavior(
        Map<MemoryModuleType<?>, MemoryStatus> param0,
        Set<MemoryModuleType<?>> param1,
        GateBehavior.OrderPolicy param2,
        GateBehavior.RunningPolicy param3,
        List<Pair<Behavior<? super E>, Integer>> param4
    ) {
        super(param0);
        this.exitErasedMemories = param1;
        this.orderPolicy = param2;
        this.runningPolicy = param3;
        param4.forEach(param0x -> this.behaviors.add(param0x.getFirst(), param0x.getSecond()));
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return this.behaviors
            .stream()
            .filter(param0x -> param0x.getStatus() == Behavior.Status.RUNNING)
            .anyMatch(param3 -> param3.canStillUse(param0, param1, param2));
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        this.orderPolicy.apply(this.behaviors);
        this.runningPolicy.apply(this.behaviors, param0, param1, param2);
    }

    @Override
    protected void tick(ServerLevel param0, E param1, long param2) {
        this.behaviors.stream().filter(param0x -> param0x.getStatus() == Behavior.Status.RUNNING).forEach(param3 -> param3.tickOrStop(param0, param1, param2));
    }

    @Override
    protected void stop(ServerLevel param0, E param1, long param2) {
        this.behaviors.stream().filter(param0x -> param0x.getStatus() == Behavior.Status.RUNNING).forEach(param3 -> param3.doStop(param0, param1, param2));
        this.exitErasedMemories.forEach(param1.getBrain()::eraseMemory);
    }

    @Override
    public String toString() {
        Set<? extends Behavior<? super E>> var0 = this.behaviors
            .stream()
            .filter(param0 -> param0.getStatus() == Behavior.Status.RUNNING)
            .collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + var0;
    }

    public static enum OrderPolicy {
        ORDERED(param0 -> {
        }),
        SHUFFLED(WeightedList::shuffle);

        private final Consumer<WeightedList<?>> consumer;

        private OrderPolicy(Consumer<WeightedList<?>> param0) {
            this.consumer = param0;
        }

        public void apply(WeightedList<?> param0) {
            this.consumer.accept(param0);
        }
    }

    public static enum RunningPolicy {
        RUN_ONE {
            @Override
            public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> param0, ServerLevel param1, E param2, long param3) {
                param0.stream()
                    .filter(param0x -> param0x.getStatus() == Behavior.Status.STOPPED)
                    .filter(param3x -> param3x.tryStart(param1, param2, param3))
                    .findFirst();
            }
        },
        TRY_ALL {
            @Override
            public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> param0, ServerLevel param1, E param2, long param3) {
                param0.stream().filter(param0x -> param0x.getStatus() == Behavior.Status.STOPPED).forEach(param3x -> param3x.tryStart(param1, param2, param3));
            }
        };

        private RunningPolicy() {
        }

        public abstract <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> var1, ServerLevel var2, E var3, long var4);
    }
}
