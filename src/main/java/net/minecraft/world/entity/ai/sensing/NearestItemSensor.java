package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class NearestItemSensor extends Sensor<Mob> {
    private static final long XZ_RANGE = 8L;
    private static final long Y_RANGE = 4L;
    public static final int MAX_DISTANCE_TO_WANTED_ITEM = 9;

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    protected void doTick(ServerLevel param0, Mob param1) {
        Brain<?> var0 = param1.getBrain();
        List<ItemEntity> var1 = param0.getEntitiesOfClass(ItemEntity.class, param1.getBoundingBox().inflate(8.0, 4.0, 8.0), param0x -> true);
        var1.sort(Comparator.comparingDouble(param1::distanceToSqr));
        Optional<ItemEntity> var2 = var1.stream()
            .filter(param1x -> param1.wantsToPickUp(param1x.getItem()))
            .filter(param1x -> param1x.closerThan(param1, 9.0))
            .filter(param1::hasLineOfSight)
            .findFirst();
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, var2);
    }
}
