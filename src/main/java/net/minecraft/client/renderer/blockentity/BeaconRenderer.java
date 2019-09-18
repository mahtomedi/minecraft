package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconRenderer extends BlockEntityRenderer<BeaconBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

    public void render(BeaconBlockEntity param0, double param1, double param2, double param3, float param4, int param5, RenderType param6) {
        this.renderBeaconBeam(param1, param2, param3, (double)param4, param0.getBeamSections(), param0.getLevel().getGameTime());
    }

    private void renderBeaconBeam(double param0, double param1, double param2, double param3, List<BeaconBlockEntity.BeaconBeamSection> param4, long param5) {
        RenderSystem.defaultAlphaFunc();
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
        renderPart(var2, var6, var7, var8, 1.0F, param6, var0, 0.0, param9, param9, 0.0, var13, 0.0, 0.0, var16, 0.0, 1.0, var20, var19);
        var1.end();
        RenderSystem.popMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        renderPart(var2, var6, var7, var8, 0.125F, param6, var0, var9, var22, param10, var12, var13, param10, param10, param10, 0.0, 1.0, var20, var19);
        var1.end();
        RenderSystem.popMatrix();
        RenderSystem.enableLighting();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }

    private static void renderPart(
        BufferBuilder param0,
        float param1,
        float param2,
        float param3,
        float param4,
        int param5,
        int param6,
        double param7,
        double param8,
        double param9,
        double param10,
        double param11,
        double param12,
        double param13,
        double param14,
        double param15,
        double param16,
        double param17,
        double param18
    ) {
        renderQuad(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param15, param16, param17, param18);
        renderQuad(param0, param1, param2, param3, param4, param5, param6, param13, param14, param11, param12, param15, param16, param17, param18);
        renderQuad(param0, param1, param2, param3, param4, param5, param6, param9, param10, param13, param14, param15, param16, param17, param18);
        renderQuad(param0, param1, param2, param3, param4, param5, param6, param11, param12, param7, param8, param15, param16, param17, param18);
    }

    private static void renderQuad(
        BufferBuilder param0,
        float param1,
        float param2,
        float param3,
        float param4,
        int param5,
        int param6,
        double param7,
        double param8,
        double param9,
        double param10,
        double param11,
        double param12,
        double param13,
        double param14
    ) {
        param0.vertex(param7, (double)param6, param8).uv(param12, param13).color(param1, param2, param3, param4).endVertex();
        param0.vertex(param7, (double)param5, param8).uv(param12, param14).color(param1, param2, param3, param4).endVertex();
        param0.vertex(param9, (double)param5, param10).uv(param11, param14).color(param1, param2, param3, param4).endVertex();
        param0.vertex(param9, (double)param6, param10).uv(param11, param13).color(param1, param2, param3, param4).endVertex();
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity param0) {
        return true;
    }
}
