package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for(int var2 = 0; var2 < this.toRender.size(); ++var2) {
            BlockPos var3 = this.toRender.get(var2);
            Float var4 = this.scales.get(var2);
            float var5 = var4 / 2.0F;
            LevelRenderer.addChainedFilledBoxVertices(
                var1,
                (double)((float)var3.getX() + 0.5F - var5) - param2,
                (double)((float)var3.getY() + 0.5F - var5) - param3,
                (double)((float)var3.getZ() + 0.5F - var5) - param4,
                (double)((float)var3.getX() + 0.5F + var5) - param2,
                (double)((float)var3.getY() + 0.5F + var5) - param3,
                (double)((float)var3.getZ() + 0.5F + var5) - param4,
                this.reds.get(var2),
                this.greens.get(var2),
                this.blues.get(var2),
                this.alphas.get(var2)
            );
        }

        var0.end();
    }
}
