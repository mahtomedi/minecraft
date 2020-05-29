package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class SecondaryPoiSensor extends Sensor<Villager> {
    public SecondaryPoiSensor() {
        super(40);
    }

    protected void doTick(ServerLevel param0, Villager param1) {
        ResourceKey<Level> var0 = param0.dimension();
        BlockPos var1 = param1.blockPosition();
        List<GlobalPos> var2 = Lists.newArrayList();
        int var3 = 4;

        for(int var4 = -4; var4 <= 4; ++var4) {
            for(int var5 = -2; var5 <= 2; ++var5) {
                for(int var6 = -4; var6 <= 4; ++var6) {
                    BlockPos var7 = var1.offset(var4, var5, var6);
                    if (param1.getVillagerData().getProfession().getSecondaryPoi().contains(param0.getBlockState(var7).getBlock())) {
                        var2.add(GlobalPos.of(var0, var7));
                    }
                }
            }
        }

        Brain<?> var8 = param1.getBrain();
        if (!var2.isEmpty()) {
            var8.setMemory(MemoryModuleType.SECONDARY_JOB_SITE, var2);
        } else {
            var8.eraseMemory(MemoryModuleType.SECONDARY_JOB_SITE);
        }

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.SECONDARY_JOB_SITE);
    }
}
