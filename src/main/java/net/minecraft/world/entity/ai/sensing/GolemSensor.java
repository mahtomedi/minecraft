package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
    private static final int GOLEM_SCAN_RATE = 200;
    private static final int MEMORY_TIME_TO_LIVE = 600;

    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int param0) {
        super(param0);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        checkForNearbyGolem(param1);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(LivingEntity param0) {
        Optional<List<LivingEntity>> var0 = param0.getBrain().getMemory(MemoryModuleType.LIVING_ENTITIES);
        if (var0.isPresent()) {
            boolean var1 = var0.get().stream().anyMatch(param0x -> param0x.getType().equals(EntityType.IRON_GOLEM));
            if (var1) {
                golemDetected(param0);
            }

        }
    }

    public static void golemDetected(LivingEntity param0) {
        param0.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 600L);
    }
}
