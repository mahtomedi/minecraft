package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconRenderer extends BlockEntityRenderer<BeaconBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

    public void render(BeaconBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        this.renderBeaconBeam(param1, param2, param3, (double)param4, param0.getBeamSections(), param0.getLevel().getGameTime());
    }

    private void renderBeaconBeam(double param0, double param1, double param2, double param3, List<BeaconBlockEntity.BeaconBeamSection> param4, long param5) {
        RenderSystem.alphaFunc(516, 0.1F);
        this.bindTexture(BEAM_LOCATION);
        RenderSystem.disableFog();
        int var0 = 0;

        for(int var1 = 0; var1 < param4.size(); ++var1) {
            BeaconBlockEntity.BeaconBeamSection var2 = param4.get(var1);
            renderBeaconBeam(param0, param1, param2, param3, param5, var0, var1 == param4.size() - 1 ? 1024 : var2.getHeight(), var2.getColor());
            var0 += var2.getHeight();
        }

        RenderSystem.enableFog();
    }

    private static void renderBeaconBeam(double param0, double param1, double param2, double param3, long param4, int param5, int param6, float[] param7) {
        renderBeaconBeam(param0, param1, param2, param3, 1.0, param4, param5, param6, param7, 0.2, 0.25);
    }

    public static void renderBeaconBeam(
        double param0,
        double param1,
        double param2,
        double param3,
        double param4,
        long param5,
        int param6,
        int param7,
        float[] param8,
        double param9,
        double param10
    ) {
        int var0 = param6 + param7;
        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);
        RenderSystem.disableLighting();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.pushMatrix();
        RenderSystem.translated(param0 + 0.5, param1, param2 + 0.5);
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        double var3 = (double)Math.floorMod(param5, 40L) + param3;
        double var4 = param7 < 0 ? var3 : -var3;
        double var5 = Mth.frac(var4 * 0.2 - (double)Mth.floor(var4 * 0.1));
        float var6 = param8[0];
        float var7 = param8[1];
        float var8 = param8[2];
        RenderSystem.pushMatrix();
        RenderSystem.rotated(var3 * 2.25 - 45.0, 0.0, 1.0, 0.0);
        double var9 = 0.0;
        double var12 = 0.0;
        double var13 = -param9;
        double var14 = 0.0;
        double var15 = 0.0;
        double var16 = -param9;
        double var17 = 0.0;
        double var18 = 1.0;
        double var19 = -1.0 + var5;
        double var20 = (double)param7 * param4 * (0.5 / param9) + var19;
        var2.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var2.vertex(0.0, (double)var0, param9).uv(1.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)param6, param9).uv(1.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(param9, (double)param6, 0.0).uv(0.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(param9, (double)var0, 0.0).uv(0.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)var0, var16).uv(1.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)param6, var16).uv(1.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(var13, (double)param6, 0.0).uv(0.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(var13, (double)var0, 0.0).uv(0.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(param9, (double)var0, 0.0).uv(1.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(param9, (double)param6, 0.0).uv(1.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)param6, var16).uv(0.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)var0, var16).uv(0.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(var13, (double)var0, 0.0).uv(1.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(var13, (double)param6, 0.0).uv(1.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)param6, param9).uv(0.0, var19).color(var6, var7, var8, 1.0F).endVertex();
        var2.vertex(0.0, (double)var0, param9).uv(0.0, var20).color(var6, var7, var8, 1.0F).endVertex();
        var1.end();
        RenderSystem.popMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.depthMask(false);
        var9 = -param10;
        double var22 = -param10;
        var12 = -param10;
        var13 = -param10;
        var17 = 0.0;
        var18 = 1.0;
        var19 = -1.0 + var5;
        var20 = (double)param7 * param4 + var19;
        var2.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var2.vertex(var9, (double)var0, var22).uv(1.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var9, (double)param6, var22).uv(1.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)param6, var12).uv(0.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)var0, var12).uv(0.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)var0, param10).uv(1.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)param6, param10).uv(1.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var13, (double)param6, param10).uv(0.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var13, (double)var0, param10).uv(0.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)var0, var12).uv(1.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)param6, var12).uv(1.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)param6, param10).uv(0.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(param10, (double)var0, param10).uv(0.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var13, (double)var0, param10).uv(1.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var13, (double)param6, param10).uv(1.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var9, (double)param6, var22).uv(0.0, var19).color(var6, var7, var8, 0.125F).endVertex();
        var2.vertex(var9, (double)var0, var22).uv(0.0, var20).color(var6, var7, var8, 0.125F).endVertex();
        var1.end();
        RenderSystem.popMatrix();
        RenderSystem.enableLighting();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity param0) {
        return true;
    }
}
