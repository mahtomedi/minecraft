package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends Sensor<LivingEntity> {
    private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder()
        .put(EntityType.DROWNED, 8.0F)
        .put(EntityType.EVOKER, 12.0F)
        .put(EntityType.HUSK, 8.0F)
        .put(EntityType.ILLUSIONER, 12.0F)
        .put(EntityType.PILLAGER, 15.0F)
        .put(EntityType.RAVAGER, 12.0F)
        .put(EntityType.VEX, 8.0F)
        .put(EntityType.VINDICATOR, 10.0F)
        .put(EntityType.ZOGLIN, 10.0F)
        .put(EntityType.ZOMBIE, 8.0F)
        .put(EntityType.ZOMBIE_VILLAGER, 8.0F)
        .build();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        param1.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(param1));
    }

    private Optional<LivingEntity> getNearestHostile(LivingEntity param0) {
        return this.getVisibleEntities(param0)
            .flatMap(
                param1 -> param1.stream()
                        .filter(this::isHostile)
                        .filter(param1x -> this.isClose(param0, param1x))
                        .min((param1x, param2) -> this.compareMobDistance(param0, param1x, param2))
            );
    }

    private Optional<List<LivingEntity>> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }

    private int compareMobDistance(LivingEntity param0, LivingEntity param1, LivingEntity param2) {
        return Mth.floor(param1.distanceToSqr(param0) - param2.distanceToSqr(param0));
    }

    private boolean isClose(LivingEntity param0, LivingEntity param1) {
        float var0 = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(param1.getType());
        return param1.distanceToSqr(param0) <= (double)(var0 * var0);
    }

    private boolean isHostile(LivingEntity param0) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(param0.getType());
    }
}
