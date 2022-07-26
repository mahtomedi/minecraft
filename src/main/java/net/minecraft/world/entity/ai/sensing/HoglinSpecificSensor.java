package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor extends Sensor<Hoglin> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_REPELLENT,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
            MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
            MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT
        );
    }

    protected void doTick(ServerLevel param0, Hoglin param1) {
        Brain<?> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(param0, param1));
        Optional<Piglin> var1 = Optional.empty();
        int var2 = 0;
        List<Hoglin> var3 = Lists.newArrayList();
        NearestVisibleLivingEntities var4 = var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for(LivingEntity var5 : var4.findAll(param0x -> !param0x.isBaby() && (param0x instanceof Piglin || param0x instanceof Hoglin))) {
            if (var5 instanceof Piglin var6) {
                ++var2;
                if (var1.isEmpty()) {
                    var1 = Optional.of(var6);
                }
            }

            if (var5 instanceof Hoglin var7) {
                var3.add(var7);
            }
        }

        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, var1);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, var3);
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, var2);
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, var3.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel param0, Hoglin param1) {
        return BlockPos.findClosestMatch(param1.blockPosition(), 8, 4, param1x -> param0.getBlockState(param1x).is(BlockTags.HOGLIN_REPELLENTS));
    }
}
