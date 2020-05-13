package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.dimension.DimensionType;

public class InteractableDoorsSensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        DimensionType var0 = param0.dimensionType();
        BlockPos var1 = param1.blockPosition();
        List<GlobalPos> var2 = Lists.newArrayList();

        for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4 = -1; var4 <= 1; ++var4) {
                for(int var5 = -1; var5 <= 1; ++var5) {
                    BlockPos var6 = var1.offset(var3, var4, var5);
                    if (param0.getBlockState(var6).is(BlockTags.WOODEN_DOORS)) {
                        var2.add(GlobalPos.of(var0, var6));
                    }
                }
            }
        }

        Brain<?> var7 = param1.getBrain();
        if (!var2.isEmpty()) {
            var7.setMemory(MemoryModuleType.INTERACTABLE_DOORS, var2);
        } else {
            var7.eraseMemory(MemoryModuleType.INTERACTABLE_DOORS);
        }

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.INTERACTABLE_DOORS);
    }
}
