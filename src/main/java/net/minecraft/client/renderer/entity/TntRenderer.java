package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt> {
    public TntRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(PrimedTnt param0, double param1, double param2, double param3, float param4, float param5) {
        BlockRenderDispatcher var0 = Minecraft.getInstance().getBlockRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)param1, (float)param2 + 0.5F, (float)param3);
        if ((float)param0.getLife() - param5 + 1.0F < 10.0F) {
            float var1 = 1.0F - ((float)param0.getLife() - param5 + 1.0F) / 10.0F;
            var1 = Mth.clamp(var1, 0.0F, 1.0F);
            var1 *= var1;
            var1 *= var1;
            float var2 = 1.0F + var1 * 0.3F;
            GlStateManager.scalef(var2, var2, var2);
        }

        float var3 = (1.0F - ((float)param0.getLife() - param5 + 1.0F) / 100.0F) * 0.8F;
        this.bindTexture(param0);
        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
        var0.renderSingleBlock(Blocks.TNT.defaultBlockState(), param0.getBrightness());
        GlStateManager.translatef(0.0F, 0.0F, 1.0F);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
            var0.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        } else if (param0.getLife() / 5 % 2 == 0) {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, var3);
            GlStateManager.polygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            var0.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
            GlStateManager.polygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
        }

        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(PrimedTnt param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
