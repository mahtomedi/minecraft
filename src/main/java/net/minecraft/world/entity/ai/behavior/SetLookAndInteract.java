package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetLookAndInteract extends Behavior<LivingEntity> {
    private final EntityType<?> type;
    private final int interactionRangeSqr;
    private final Predicate<LivingEntity> targetFilter;
    private final Predicate<LivingEntity> selfFilter;

    public SetLookAndInteract(EntityType<?> param0, int param1, Predicate<LivingEntity> param2, Predicate<LivingEntity> param3) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.type = param0;
        this.interactionRangeSqr = param1 * param1;
        this.targetFilter = param3;
        this.selfFilter = param2;
    }

    public SetLookAndInteract(EntityType<?> param0, int param1) {
        this(param0, param1, param0x -> true, param0x -> true);
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return this.selfFilter.test(param1) && this.getVisibleEntities(param1).stream().anyMatch(this::isMatchingTarget);
    }

    @Override
    public void start(ServerLevel param0, LivingEntity param1, long param2) {
        super.start(param0, param1, param2);
        Brain<?> var0 = param1.getBrain();
        var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
            .ifPresent(
                param2x -> param2x.stream()
                        .filter(param1x -> param1x.distanceToSqr(param1) <= (double)this.interactionRangeSqr)
                        .filter(this::isMatchingTarget)
                        .findFirst()
                        .ifPresent(param1x -> {
                            var0.setMemory(MemoryModuleType.INTERACTION_TARGET, param1x);
                            var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1x));
                        })
            );
    }

    private boolean isMatchingTarget(LivingEntity param0x) {
        return this.type.equals(param0x.getType()) && this.targetFilter.test(param0x);
    }

    private List<LivingEntity> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get();
    }
}
