package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenAttemptRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public WorldGenAttemptRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addPos(BlockPos param0, float param1, float param2, float param3, float param4, float param5) {
        this.toRender.add(param0);
        this.scales.add(param1);
        this.alphas.add(param5);
        this.reds.add(param2);
        this.greens.add(param3);
        this.blues.add(param4);
    }
}
