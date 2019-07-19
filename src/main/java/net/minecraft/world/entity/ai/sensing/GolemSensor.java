package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int param0) {
        super(param0);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        checkForNearbyGolem(param0.getGameTime(), param1);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(long param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        Optional<List<LivingEntity>> var1 = var0.getMemory(MemoryModuleType.LIVING_ENTITIES);
        if (var1.isPresent()) {
            boolean var2 = var1.get().stream().anyMatch(param0x -> param0x.getType().equals(EntityType.IRON_GOLEM));
            if (var2) {
                var0.setMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME, param0);
            }

        }
    }
}
