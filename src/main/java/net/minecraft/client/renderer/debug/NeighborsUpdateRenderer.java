package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

    NeighborsUpdateRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addUpdate(long param0, BlockPos param1) {
        Map<BlockPos, Integer> var0 = this.lastUpdate.get(param0);
        if (var0 == null) {
            var0 = Maps.newHashMap();
            this.lastUpdate.put(param0, var0);
        }

        Integer var1 = var0.get(param1);
        if (var1 == null) {
            var1 = 0;
        }

        var0.put(param1, var1 + 1);
    }
}
