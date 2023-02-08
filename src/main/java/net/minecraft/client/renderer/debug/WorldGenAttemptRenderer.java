package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenAttemptRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public void addPos(BlockPos param0, float param1, float param2, float param3, float param4, float param5) {
        this.toRender.add(param0);
        this.scales.add(param1);
        this.alphas.add(param5);
        this.reds.add(param2);
        this.greens.add(param3);
        this.blues.add(param4);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        VertexConsumer var0 = param1.getBuffer(RenderType.debugFilledBox());

        for(int var1 = 0; var1 < this.toRender.size(); ++var1) {
            BlockPos var2 = this.toRender.get(var1);
            Float var3 = this.scales.get(var1);
            float var4 = var3 / 2.0F;
            LevelRenderer.addChainedFilledBoxVertices(
                param0,
                var0,
                (double)((float)var2.getX() + 0.5F - var4) - param2,
                (double)((float)var2.getY() + 0.5F - var4) - param3,
                (double)((float)var2.getZ() + 0.5F - var4) - param4,
                (double)((float)var2.getX() + 0.5F + var4) - param2,
                (double)((float)var2.getY() + 0.5F + var4) - param3,
                (double)((float)var2.getZ() + 0.5F + var4) - param4,
                this.reds.get(var1),
                this.greens.get(var1),
                this.blues.get(var1),
                this.alphas.get(var1)
            );
        }

    }
}
