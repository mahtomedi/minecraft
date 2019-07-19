package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
    public FallingBlockRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(FallingBlockEntity param0, double param1, double param2, double param3, float param4, float param5) {
        BlockState var0 = param0.getBlockState();
        if (var0.getRenderShape() == RenderShape.MODEL) {
            Level var1 = param0.getLevel();
            if (var0 != var1.getBlockState(new BlockPos(param0)) && var0.getRenderShape() != RenderShape.INVISIBLE) {
                this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                Tesselator var2 = Tesselator.getInstance();
                BufferBuilder var3 = var2.getBuilder();
                if (this.solidRender) {
                    GlStateManager.enableColorMaterial();
                    GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
                }

                var3.begin(7, DefaultVertexFormat.BLOCK);
                BlockPos var4 = new BlockPos(param0.x, param0.getBoundingBox().maxY, param0.z);
                GlStateManager.translatef(
                    (float)(param1 - (double)var4.getX() - 0.5), (float)(param2 - (double)var4.getY()), (float)(param3 - (double)var4.getZ() - 0.5)
                );
                BlockRenderDispatcher var5 = Minecraft.getInstance().getBlockRenderer();
                var5.getModelRenderer()
                    .tesselateBlock(var1, var5.getBlockModel(var0), var0, var4, var3, false, new Random(), var0.getSeed(param0.getStartPos()));
                var2.end();
                if (this.solidRender) {
                    GlStateManager.tearDownSolidRenderingTextureCombine();
                    GlStateManager.disableColorMaterial();
                }

                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
                super.render(param0, param1, param2, param3, param4, param5);
            }
        }
    }

    protected ResourceLocation getTextureLocation(FallingBlockEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
