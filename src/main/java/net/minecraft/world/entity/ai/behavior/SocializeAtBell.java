package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell extends Behavior<LivingEntity> {
    private static final float SPEED_MODIFIER = 0.3F;

    public SocializeAtBell() {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.MEETING_POINT,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryStatus.VALUE_ABSENT
            )
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        Optional<GlobalPos> var1 = var0.getMemory(MemoryModuleType.MEETING_POINT);
        return param0.getRandom().nextInt(100) == 0
            && var1.isPresent()
            && param0.dimension() == var1.get().dimension()
            && var1.get().pos().closerThan(param1.position(), 4.0)
            && var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().stream().anyMatch(param0x -> EntityType.VILLAGER.equals(param0x.getType()));
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
            .ifPresent(
                param2x -> param2x.stream()
                        .filter(param0x -> EntityType.VILLAGER.equals(param0x.getType()))
                        .filter(param1x -> param1x.distanceToSqr(param1) <= 32.0)
                        .findFirst()
                        .ifPresent(param1x -> {
                            var0.setMemory(MemoryModuleType.INTERACTION_TARGET, param1x);
                            var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1x, true));
                            var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(param1x, false), 0.3F, 1));
                        })
            );
    }
}
