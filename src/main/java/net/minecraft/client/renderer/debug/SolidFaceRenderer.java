package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SolidFaceRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public SolidFaceRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Matrix4f var0 = param0.last().pose();
        BlockGetter var1 = this.minecraft.player.level;
        BlockPos var2 = new BlockPos(param2, param3, param4);

        for(BlockPos var3 : BlockPos.betweenClosed(var2.offset(-6, -6, -6), var2.offset(6, 6, 6))) {
            BlockState var4 = var1.getBlockState(var3);
            if (!var4.is(Blocks.AIR)) {
                VoxelShape var5 = var4.getShape(var1, var3);

                for(AABB var6 : var5.toAabbs()) {
                    AABB var7 = var6.move(var3).inflate(0.002);
                    float var8 = (float)(var7.minX - param2);
                    float var9 = (float)(var7.minY - param3);
                    float var10 = (float)(var7.minZ - param4);
                    float var11 = (float)(var7.maxX - param2);
                    float var12 = (float)(var7.maxY - param3);
                    float var13 = (float)(var7.maxZ - param4);
                    float var14 = 1.0F;
                    float var15 = 0.0F;
                    float var16 = 0.0F;
                    float var17 = 0.5F;
                    if (var4.isFaceSturdy(var1, var3, Direction.WEST)) {
                        VertexConsumer var18 = param1.getBuffer(RenderType.debugFilledBox());
                        var18.vertex(var0, var8, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var0, var8, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var0, var8, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var18.vertex(var0, var8, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }

                    if (var4.isFaceSturdy(var1, var3, Direction.SOUTH)) {
                        VertexConsumer var19 = param1.getBuffer(RenderType.debugFilledBox());
                        var19.vertex(var0, var8, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var19.vertex(var0, var8, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var19.vertex(var0, var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var19.vertex(var0, var11, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }

                    if (var4.isFaceSturdy(var1, var3, Direction.EAST)) {
                        VertexConsumer var20 = param1.getBuffer(RenderType.debugFilledBox());
                        var20.vertex(var0, var11, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var0, var11, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var0, var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var20.vertex(var0, var11, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }

                    if (var4.isFaceSturdy(var1, var3, Direction.NORTH)) {
                        VertexConsumer var21 = param1.getBuffer(RenderType.debugFilledBox());
                        var21.vertex(var0, var11, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var21.vertex(var0, var11, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var21.vertex(var0, var8, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var21.vertex(var0, var8, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }

                    if (var4.isFaceSturdy(var1, var3, Direction.DOWN)) {
                        VertexConsumer var22 = param1.getBuffer(RenderType.debugFilledBox());
                        var22.vertex(var0, var8, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var0, var11, var9, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var0, var8, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var22.vertex(var0, var11, var9, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }

                    if (var4.isFaceSturdy(var1, var3, Direction.UP)) {
                        VertexConsumer var23 = param1.getBuffer(RenderType.debugFilledBox());
                        var23.vertex(var0, var8, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var23.vertex(var0, var8, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var23.vertex(var0, var11, var12, var10).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                        var23.vertex(var0, var11, var12, var13).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                    }
                }
            }
        }

    }
}
