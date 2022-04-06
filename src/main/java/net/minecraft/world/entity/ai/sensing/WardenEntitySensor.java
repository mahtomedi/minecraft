package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends NearestLivingEntitySensor<Warden> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    protected void doTick(ServerLevel param0, Warden param1) {
        super.doTick(param0, param1);
        getClosest(param1, param0x -> param0x.getType() == EntityType.PLAYER)
            .or(() -> getClosest(param1, param0x -> param0x.getType() != EntityType.PLAYER))
            .ifPresentOrElse(
                param1x -> param1.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, param1x),
                () -> param1.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE)
            );
    }

    private static Optional<LivingEntity> getClosest(Warden param0, Predicate<LivingEntity> param1) {
        return param0.getBrain()
            .getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
            .stream()
            .flatMap(Collection::stream)
            .filter(param0::canTargetEntity)
            .filter(param1)
            .findFirst();
    }

    @Override
    protected int radiusXZ() {
        return 24;
    }
}
