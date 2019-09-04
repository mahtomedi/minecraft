package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.entity.layers.EnderDragonDeathLayer;
import net.minecraft.client.renderer.entity.layers.EnderDragonEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderDragonRenderer extends MobRenderer<EnderDragon, DragonModel> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");

    public EnderDragonRenderer(EntityRenderDispatcher param0) {
        super(param0, new DragonModel(0.0F), 0.5F);
        this.addLayer(new EnderDragonEyesLayer(this));
        this.addLayer(new EnderDragonDeathLayer(this));
    }

    protected void setupRotations(EnderDragon param0, float param1, float param2, float param3) {
        float var0 = (float)param0.getLatencyPos(7, param3)[0];
        float var1 = (float)(param0.getLatencyPos(5, param3)[1] - param0.getLatencyPos(10, param3)[1]);
        RenderSystem.rotatef(-var0, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(var1 * 10.0F, 1.0F, 0.0F, 0.0F);
        RenderSystem.translatef(0.0F, 0.0F, 1.0F);
        if (param0.deathTime > 0) {
            float var2 = ((float)param0.deathTime + param3 - 1.0F) / 20.0F * 1.6F;
            var2 = Mth.sqrt(var2);
            if (var2 > 1.0F) {
                var2 = 1.0F;
            }

            RenderSystem.rotatef(var2 * this.getFlipDegrees(param0), 0.0F, 0.0F, 1.0F);
        }

    }

    protected void renderModel(EnderDragon param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        if (param0.dragonDeathTime > 0) {
            float var0 = (float)param0.dragonDeathTime / 200.0F;
            RenderSystem.depthFunc(515);
            RenderSystem.enableAlphaTest();
            RenderSystem.alphaFunc(516, var0);
            this.bindTexture(DRAGON_EXPLODING_LOCATION);
            this.model.render(param0, param1, param2, param3, param4, param5, param6);
            RenderSystem.alphaFunc(516, 0.1F);
            RenderSystem.depthFunc(514);
        }

        this.bindTexture(param0);
        this.model.render(param0, param1, param2, param3, param4, param5, param6);
        if (param0.hurtTime > 0) {
            RenderSystem.depthFunc(514);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1.0F, 0.0F, 0.0F, 0.5F);
            this.model.render(param0, param1, param2, param3, param4, param5, param6);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.depthFunc(515);
        }

    }

    public void render(EnderDragon param0, double param1, double param2, double param3, float param4, float param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        if (param0.nearestCrystal != null) {
            this.bindTexture(CRYSTAL_BEAM_LOCATION);
            float var0 = Mth.sin(((float)param0.nearestCrystal.tickCount + param5) * 0.2F) / 2.0F + 0.5F;
            var0 = (var0 * var0 + var0) * 0.2F;
            renderCrystalBeams(
                param1,
                param2,
                param3,
                param5,
                Mth.lerp((double)(1.0F - param5), param0.x, param0.xo),
                Mth.lerp((double)(1.0F - param5), param0.y, param0.yo),
                Mth.lerp((double)(1.0F - param5), param0.z, param0.zo),
                param0.tickCount,
                param0.nearestCrystal.x,
                (double)var0 + param0.nearestCrystal.y,
                param0.nearestCrystal.z
            );
        }

    }

    public static void renderCrystalBeams(
        double param0,
        double param1,
        double param2,
        float param3,
        double param4,
        double param5,
        double param6,
        int param7,
        double param8,
        double param9,
        double param10
    ) {
        float var0 = (float)(param8 - param4);
        float var1 = (float)(param9 - 1.0 - param5);
        float var2 = (float)(param10 - param6);
        float var3 = Mth.sqrt(var0 * var0 + var2 * var2);
        float var4 = Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param0, (float)param1 + 2.0F, (float)param2);
        RenderSystem.rotatef((float)(-Math.atan2((double)var2, (double)var0)) * (180.0F / (float)Math.PI) - 90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef((float)(-Math.atan2((double)var3, (double)var1)) * (180.0F / (float)Math.PI) - 90.0F, 1.0F, 0.0F, 0.0F);
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        Lighting.turnOff();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
        float var7 = 0.0F - ((float)param7 + param3) * 0.01F;
        float var8 = Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2) / 32.0F - ((float)param7 + param3) * 0.01F;
        var6.begin(5, DefaultVertexFormat.POSITION_TEX_COLOR);
        int var9 = 8;

        for(int var10 = 0; var10 <= 8; ++var10) {
            float var11 = Mth.sin((float)(var10 % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var12 = Mth.cos((float)(var10 % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var13 = (float)(var10 % 8) / 8.0F;
            var6.vertex((double)(var11 * 0.2F), (double)(var12 * 0.2F), 0.0).uv((double)var13, (double)var7).color(0, 0, 0, 255).endVertex();
            var6.vertex((double)var11, (double)var12, (double)var4).uv((double)var13, (double)var8).color(255, 255, 255, 255).endVertex();
        }

        var5.end();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7424);
        Lighting.turnOn();
        RenderSystem.popMatrix();
    }

    protected ResourceLocation getTextureLocation(EnderDragon param0) {
        return DRAGON_LOCATION;
    }
}
