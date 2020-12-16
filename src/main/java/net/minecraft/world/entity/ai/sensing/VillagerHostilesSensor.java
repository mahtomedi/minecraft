package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class VillagerHostilesSensor extends HostilesSensor {
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
    protected Optional<LivingEntity> getNearestHostile(LivingEntity param0) {
        return this.getVisibleEntities(param0)
            .flatMap(
                param1 -> param1.stream()
                        .filter(this::isHostile)
                        .filter(param1x -> this.isClose(param0, param1x))
                        .min(Comparator.comparingDouble(param0::distanceToSqr))
            );
    }

    @Override
    protected boolean isClose(LivingEntity param0, LivingEntity param1) {
        float var0 = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(param1.getType());
        return param1.distanceToSqr(param0) <= (double)(var0 * var0);
    }

    private boolean isHostile(LivingEntity param0) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(param0.getType());
    }
}
