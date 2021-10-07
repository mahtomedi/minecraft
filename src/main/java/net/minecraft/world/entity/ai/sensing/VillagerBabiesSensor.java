package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

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
        return ImmutableList.copyOf(this.getVisibleEntities(param0).findAll(this::isVillagerBaby));
    }

    private boolean isVillagerBaby(LivingEntity param0x) {
        return param0x.getType() == EntityType.VILLAGER && param0x.isBaby();
    }

    private NearestVisibleLivingEntities getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
    }
}
