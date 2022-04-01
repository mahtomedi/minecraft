package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity> extends Behavior<E> {
    private final int maxDist;
    private final float speedModifier;
    private final EntityType<? extends T> type;
    private final int interactionRangeSqr;
    private final Predicate<T> targetFilter;
    private final Predicate<E> selfFilter;
    private final MemoryModuleType<T> memory;

    public InteractWith(
        EntityType<? extends T> param0, int param1, Predicate<E> param2, Predicate<T> param3, MemoryModuleType<T> param4, float param5, int param6
    ) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.type = param0;
        this.speedModifier = param5;
        this.interactionRangeSqr = param1 * param1;
        this.maxDist = param6;
        this.targetFilter = param3;
        this.selfFilter = param2;
        this.memory = param4;
    }

    public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(
        EntityType<? extends T> param0, int param1, MemoryModuleType<T> param2, float param3, int param4
    ) {
        return new InteractWith<>(param0, param1, param0x -> true, param0x -> true, param2, param3, param4);
    }

    public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(
        EntityType<? extends T> param0, int param1, Predicate<T> param2, MemoryModuleType<T> param3, float param4, int param5
    ) {
        return new InteractWith<>(param0, param1, param0x -> true, param2, param3, param4, param5);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.selfFilter.test(param1) && this.seesAtLeastOneValidTarget(param1);
    }

    private boolean seesAtLeastOneValidTarget(E param0) {
        NearestVisibleLivingEntities var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        return var0.contains(this::isTargetValid);
    }

    private boolean isTargetValid(LivingEntity param0x) {
        return this.type.equals(param0x.getType()) && this.targetFilter.test((T)param0x);
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        Optional<NearestVisibleLivingEntities> var1 = var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (!var1.isEmpty()) {
            NearestVisibleLivingEntities var2 = var1.get();
            var2.findClosest(param1x -> this.canInteract(param1, param1x)).ifPresent(param1x -> {
                var0.setMemory(this.memory, (T)param1x);
                var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1x, true));
                var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(param1x, false), this.speedModifier, this.maxDist));
            });
        }
    }

    private boolean canInteract(E param0, LivingEntity param1) {
        return this.type.equals(param1.getType()) && param1.distanceToSqr(param0) <= (double)this.interactionRangeSqr && this.targetFilter.test((T)param1);
    }
}
