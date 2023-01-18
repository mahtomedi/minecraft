package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SolidFaceRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public SolidFaceRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        BlockGetter var0 = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BlockPos var1 = new BlockPos(param2, param3, param4);

        for(BlockPos var2 : BlockPos.betweenClosed(var1.offset(-6, -6, -6), var1.offset(6, 6, 6))) {
            BlockState var3 = var0.getBlockState(var2);
            if (!var3.is(Blocks.AIR)) {
                VoxelShape var4 = var3.getShape(var0, var2);

                for(AABB var5 : var4.toAabbs()) {
                    AABB var6 = var5.move(var2).inflate(0.002).move(-param2, -param3, -param4);
                    double var7 = var6.minX;
                    double var8 = var6.minY;
                    double var9 = var6.minZ;
                    double var10 = var6.maxX;
                    double var11 = var6.maxY;
                    double var12 = var6.maxZ;
                    float var13 = 1.0F;
                    float var14 = 0.0F;
                    float var15 = 0.0F;
                    float var16 = 0.5F;
                    if (var3.isFaceSturdy(var0, var2, Direction.WEST)) {
                        Tesselator var17 = Tesselator.getInstance();
                        BufferBuilder var18 = var17.getBuilder();
                        var18.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var18.vertex(var7, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var7, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var7, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var7, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var17.end();
                    }

                    if (var3.isFaceSturdy(var0, var2, Direction.SOUTH)) {
                        Tesselator var19 = Tesselator.getInstance();
                        BufferBuilder var20 = var19.getBuilder();
                        var20.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var20.vertex(var7, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var7, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var10, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var10, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var19.end();
                    }

                    if (var3.isFaceSturdy(var0, var2, Direction.EAST)) {
                        Tesselator var21 = Tesselator.getInstance();
                        BufferBuilder var22 = var21.getBuilder();
                        var22.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var22.vertex(var10, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var10, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var10, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var10, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var21.end();
                    }

                    if (var3.isFaceSturdy(var0, var2, Direction.NORTH)) {
                        Tesselator var23 = Tesselator.getInstance();
                        BufferBuilder var24 = var23.getBuilder();
                        var24.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var24.vertex(var10, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var10, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var7, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var7, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var23.end();
                    }

                    if (var3.isFaceSturdy(var0, var2, Direction.DOWN)) {
                        Tesselator var25 = Tesselator.getInstance();
                        BufferBuilder var26 = var25.getBuilder();
                        var26.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var26.vertex(var7, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var10, var8, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var7, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var10, var8, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var25.end();
                    }

                    if (var3.isFaceSturdy(var0, var2, Direction.UP)) {
                        Tesselator var27 = Tesselator.getInstance();
                        BufferBuilder var28 = var27.getBuilder();
                        var28.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                        var28.vertex(var7, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var7, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var10, var11, var9).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var10, var11, var12).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var27.end();
                    }
                }
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
