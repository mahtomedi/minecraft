package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinBruteSpecificSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        Optional<Mob> var1 = Optional.empty();
        List<AbstractPiglin> var2 = Lists.newArrayList();

        for(LivingEntity var4 : var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (var4 instanceof WitherSkeleton || var4 instanceof WitherBoss) {
                var1 = Optional.of((Mob)var4);
                break;
            }
        }

        for(LivingEntity var6 : var0.getMemory(MemoryModuleType.LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (var6 instanceof AbstractPiglin && ((AbstractPiglin)var6).isAdult()) {
                var2.add((AbstractPiglin)var6);
            }
        }

        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, var1);
        var0.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, var2);
    }
}
