package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<BlockPos, BlockPos> tunnelsList = Maps.newHashMap();
    private final Map<BlockPos, Float> thicknessMap = Maps.newHashMap();
    private final List<BlockPos> startPoses = Lists.newArrayList();

    public CaveDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addTunnel(BlockPos param0, List<BlockPos> param1, List<Float> param2) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            this.tunnelsList.put(param1.get(var0), param0);
            this.thicknessMap.put(param1.get(var0), param2.get(var0));
        }

        this.startPoses.add(param0);
    }
}
