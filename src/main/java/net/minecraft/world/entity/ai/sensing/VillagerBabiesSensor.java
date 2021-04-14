package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        param1.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(param1));
    }

    private List<LivingEntity> getNearestVillagerBabies(LivingEntity param0) {
        return this.getVisibleEntities(param0).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
    }

    private boolean isVillagerBaby(LivingEntity param0x) {
        return param0x.getType() == EntityType.VILLAGER && param0x.isBaby();
    }

    private List<LivingEntity> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
    }
}
