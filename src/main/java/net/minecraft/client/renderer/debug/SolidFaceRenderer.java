package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        double var1 = var0.getPosition().x;
        double var2 = var0.getPosition().y;
        double var3 = var0.getPosition().z;
        BlockGetter var4 = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        BlockPos var5 = new BlockPos(var0.getPosition());

        for(BlockPos var6 : BlockPos.betweenClosed(var5.offset(-6, -6, -6), var5.offset(6, 6, 6))) {
            BlockState var7 = var4.getBlockState(var6);
            if (var7.getBlock() != Blocks.AIR) {
                VoxelShape var8 = var7.getShape(var4, var6);

                for(AABB var9 : var8.toAabbs()) {
                    AABB var10 = var9.move(var6).inflate(0.002).move(-var1, -var2, -var3);
                    double var11 = var10.minX;
                    double var12 = var10.minY;
                    double var13 = var10.minZ;
                    double var14 = var10.maxX;
                    double var15 = var10.maxY;
                    double var16 = var10.maxZ;
                    float var17 = 1.0F;
                    float var18 = 0.0F;
                    float var19 = 0.0F;
                    float var20 = 0.5F;
                    if (var7.isFaceSturdy(var4, var6, Direction.WEST)) {
                        Tesselator var21 = Tesselator.getInstance();
                        BufferBuilder var22 = var21.getBuilder();
                        var22.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var22.vertex(var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var11, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var11, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var11, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var21.end();
                    }

                    if (var7.isFaceSturdy(var4, var6, Direction.SOUTH)) {
                        Tesselator var23 = Tesselator.getInstance();
                        BufferBuilder var24 = var23.getBuilder();
                        var24.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var24.vertex(var11, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var11, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var14, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var24.vertex(var14, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var23.end();
                    }

                    if (var7.isFaceSturdy(var4, var6, Direction.EAST)) {
                        Tesselator var25 = Tesselator.getInstance();
                        BufferBuilder var26 = var25.getBuilder();
                        var26.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var26.vertex(var14, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var14, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var14, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var26.vertex(var14, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var25.end();
                    }

                    if (var7.isFaceSturdy(var4, var6, Direction.NORTH)) {
                        Tesselator var27 = Tesselator.getInstance();
                        BufferBuilder var28 = var27.getBuilder();
                        var28.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var28.vertex(var14, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var14, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var11, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var28.vertex(var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var27.end();
                    }

                    if (var7.isFaceSturdy(var4, var6, Direction.DOWN)) {
                        Tesselator var29 = Tesselator.getInstance();
                        BufferBuilder var30 = var29.getBuilder();
                        var30.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var30.vertex(var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var30.vertex(var14, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var30.vertex(var11, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var30.vertex(var14, var12, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var29.end();
                    }

                    if (var7.isFaceSturdy(var4, var6, Direction.UP)) {
                        Tesselator var31 = Tesselator.getInstance();
                        BufferBuilder var32 = var31.getBuilder();
                        var32.begin(5, DefaultVertexFormat.POSITION_COLOR);
                        var32.vertex(var11, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var32.vertex(var11, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var32.vertex(var14, var15, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var32.vertex(var14, var15, var16).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var31.end();
                    }
                }
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
