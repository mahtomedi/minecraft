package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightTexture implements AutoCloseable {
    private final DynamicTexture lightTexture;
    private final NativeImage lightPixels;
    private final ResourceLocation lightTextureLocation;
    private boolean updateLightTexture;
    private float blockLightRed;
    private float blockLightRedTotal;
    private final GameRenderer renderer;
    private final Minecraft minecraft;

    public LightTexture(GameRenderer param0, Minecraft param1) {
        this.renderer = param0;
        this.minecraft = param1;
        this.lightTexture = new DynamicTexture(16, 16, false);
        this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
        this.lightPixels = this.lightTexture.getPixels();

        for(int var0 = 0; var0 < 16; ++var0) {
            for(int var1 = 0; var1 < 16; ++var1) {
                this.lightPixels.setPixelRGBA(var1, var0, -1);
            }
        }

        this.lightTexture.upload();
    }

    @Override
    public void close() {
        this.lightTexture.close();
    }

    public void tick() {
        this.blockLightRedTotal = (float)((double)this.blockLightRedTotal + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.blockLightRedTotal = (float)((double)this.blockLightRedTotal * 0.9);
        this.blockLightRed += this.blockLightRedTotal - this.blockLightRed;
        this.updateLightTexture = true;
    }

    public void turnOffLightLayer() {
        RenderSystem.activeTexture(33986);
        RenderSystem.disableTexture();
        RenderSystem.activeTexture(33984);
    }

    public void turnOnLightLayer() {
        RenderSystem.activeTexture(33986);
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float var0 = 0.00390625F;
        RenderSystem.scalef(0.00390625F, 0.00390625F, 0.00390625F);
        RenderSystem.translatef(8.0F, 8.0F, 8.0F);
        RenderSystem.matrixMode(5888);
        this.minecraft.getTextureManager().bind(this.lightTextureLocation);
        RenderSystem.texParameter(3553, 10241, 9729);
        RenderSystem.texParameter(3553, 10240, 9729);
        RenderSystem.texParameter(3553, 10242, 10496);
        RenderSystem.texParameter(3553, 10243, 10496);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableTexture();
        RenderSystem.activeTexture(33984);
    }

    public void updateLightTexture(float param0) {
        if (this.updateLightTexture) {
            this.minecraft.getProfiler().push("lightTex");
            Level var0 = this.minecraft.level;
            if (var0 != null) {
                float var1 = var0.getSkyDarken(1.0F);
                float var2 = var1 * 0.95F + 0.05F;
                float var3 = this.minecraft.player.getWaterVision();
                float var4;
                if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
                    var4 = GameRenderer.getNightVisionScale(this.minecraft.player, param0);
                } else if (var3 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    var4 = var3;
                } else {
                    var4 = 0.0F;
                }

                for(int var7 = 0; var7 < 16; ++var7) {
                    for(int var8 = 0; var8 < 16; ++var8) {
                        float var9 = this.getBrightness(var0, var7) * var2;
                        float var10 = this.getBrightness(var0, var8) * (this.blockLightRed * 0.1F + 1.5F);
                        if (var0.getSkyFlashTime() > 0) {
                            var9 = this.getBrightness(var0, var7);
                        }

                        float var11 = var9 * (var1 * 0.65F + 0.35F);
                        float var12 = var9 * (var1 * 0.65F + 0.35F);
                        float var15 = var10 * ((var10 * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float var16 = var10 * (var10 * var10 * 0.6F + 0.4F);
                        float var17 = var11 + var10;
                        float var18 = var12 + var15;
                        float var19 = var9 + var16;
                        var17 = var17 * 0.96F + 0.03F;
                        var18 = var18 * 0.96F + 0.03F;
                        var19 = var19 * 0.96F + 0.03F;
                        if (this.renderer.getDarkenWorldAmount(param0) > 0.0F) {
                            float var20 = this.renderer.getDarkenWorldAmount(param0);
                            var17 = var17 * (1.0F - var20) + var17 * 0.7F * var20;
                            var18 = var18 * (1.0F - var20) + var18 * 0.6F * var20;
                            var19 = var19 * (1.0F - var20) + var19 * 0.6F * var20;
                        }

                        if (var0.dimension.getType() == DimensionType.THE_END) {
                            var17 = 0.22F + var10 * 0.75F;
                            var18 = 0.28F + var15 * 0.75F;
                            var19 = 0.25F + var16 * 0.75F;
                        }

                        if (var4 > 0.0F) {
                            float var21 = Math.min(1.0F / var17, Math.min(1.0F / var18, 1.0F / var19));
                            var17 = var17 * (1.0F - var4) + var17 * var21 * var4;
                            var18 = var18 * (1.0F - var4) + var18 * var21 * var4;
                            var19 = var19 * (1.0F - var4) + var19 * var21 * var4;
                        }

                        var17 = Mth.clamp(var17, 0.0F, 1.0F);
                        var18 = Mth.clamp(var18, 0.0F, 1.0F);
                        var19 = Mth.clamp(var19, 0.0F, 1.0F);
                        float var22 = (float)this.minecraft.options.gamma;
                        float var23 = 1.0F - var17;
                        float var24 = 1.0F - var18;
                        float var25 = 1.0F - var19;
                        var23 = 1.0F - var23 * var23 * var23 * var23;
                        var24 = 1.0F - var24 * var24 * var24 * var24;
                        var25 = 1.0F - var25 * var25 * var25 * var25;
                        var17 = var17 * (1.0F - var22) + var23 * var22;
                        var18 = var18 * (1.0F - var22) + var24 * var22;
                        var19 = var19 * (1.0F - var22) + var25 * var22;
                        var17 = var17 * 0.96F + 0.03F;
                        var18 = var18 * 0.96F + 0.03F;
                        var19 = var19 * 0.96F + 0.03F;
                        var17 = Mth.clamp(var17, 0.0F, 1.0F);
                        var18 = Mth.clamp(var18, 0.0F, 1.0F);
                        var19 = Mth.clamp(var19, 0.0F, 1.0F);
                        int var26 = 255;
                        int var27 = (int)(var17 * 255.0F);
                        int var28 = (int)(var18 * 255.0F);
                        int var29 = (int)(var19 * 255.0F);
                        this.lightPixels.setPixelRGBA(var8, var7, 0xFF000000 | var29 << 16 | var28 << 8 | var27);
                    }
                }

                this.lightTexture.upload();
                this.updateLightTexture = false;
                this.minecraft.getProfiler().pop();
            }
        }
    }

    private float getBrightness(Level param0, int param1) {
        return param0.dimension.getBrightnessRamp()[param1];
    }

    public static int pack(int param0, int param1) {
        return param0 | param1 << 16;
    }

    public static int block(int param0) {
        return param0 & 65535;
    }

    public static int sky(int param0) {
        return param0 >> 16 & 65535;
    }
}
