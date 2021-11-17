package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightTexture implements AutoCloseable {
    public static final int FULL_BRIGHT = 15728880;
    public static final int FULL_SKY = 15728640;
    public static final int FULL_BLOCK = 240;
    private final DynamicTexture lightTexture;
    private final NativeImage lightPixels;
    private final ResourceLocation lightTextureLocation;
    private boolean updateLightTexture;
    private float blockLightRedFlicker;
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
        this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker * 0.9);
        this.updateLightTexture = true;
    }

    public void turnOffLightLayer() {
        RenderSystem.setShaderTexture(2, 0);
    }

    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.lightTextureLocation);
        this.minecraft.getTextureManager().bindForSetup(this.lightTextureLocation);
        RenderSystem.texParameter(3553, 10241, 9729);
        RenderSystem.texParameter(3553, 10240, 9729);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void updateLightTexture(float param0) {
        if (this.updateLightTexture) {
            this.updateLightTexture = false;
            this.minecraft.getProfiler().push("lightTex");
            ClientLevel var0 = this.minecraft.level;
            if (var0 != null) {
                float var1 = var0.getSkyDarken(1.0F);
                float var2;
                if (var0.getSkyFlashTime() > 0) {
                    var2 = 1.0F;
                } else {
                    var2 = var1 * 0.95F + 0.05F;
                }

                float var4 = this.minecraft.player.getWaterVision();
                float var5;
                if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
                    var5 = GameRenderer.getNightVisionScale(this.minecraft.player, param0);
                } else if (var4 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    var5 = var4;
                } else {
                    var5 = 0.0F;
                }

                Vector3f var8 = new Vector3f(var1, var1, 1.0F);
                var8.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                float var9 = this.blockLightRedFlicker + 1.5F;
                Vector3f var10 = new Vector3f();

                for(int var11 = 0; var11 < 16; ++var11) {
                    for(int var12 = 0; var12 < 16; ++var12) {
                        float var13 = this.getBrightness(var0, var11) * var2;
                        float var14 = this.getBrightness(var0, var12) * var9;
                        float var16 = var14 * ((var14 * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float var17 = var14 * (var14 * var14 * 0.6F + 0.4F);
                        var10.set(var14, var16, var17);
                        if (var0.effects().forceBrightLightmap()) {
                            var10.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                        } else {
                            Vector3f var18 = var8.copy();
                            var18.mul(var13);
                            var10.add(var18);
                            var10.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if (this.renderer.getDarkenWorldAmount(param0) > 0.0F) {
                                float var19 = this.renderer.getDarkenWorldAmount(param0);
                                Vector3f var20 = var10.copy();
                                var20.mul(0.7F, 0.6F, 0.6F);
                                var10.lerp(var20, var19);
                            }
                        }

                        var10.clamp(0.0F, 1.0F);
                        if (var5 > 0.0F) {
                            float var21 = Math.max(var10.x(), Math.max(var10.y(), var10.z()));
                            if (var21 < 1.0F) {
                                float var22 = 1.0F / var21;
                                Vector3f var23 = var10.copy();
                                var23.mul(var22);
                                var10.lerp(var23, var5);
                            }
                        }

                        float var24 = (float)this.minecraft.options.gamma;
                        Vector3f var25 = var10.copy();
                        var25.map(this::notGamma);
                        var10.lerp(var25, var24);
                        var10.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        var10.clamp(0.0F, 1.0F);
                        var10.mul(255.0F);
                        int var26 = 255;
                        int var27 = (int)var10.x();
                        int var28 = (int)var10.y();
                        int var29 = (int)var10.z();
                        this.lightPixels.setPixelRGBA(var12, var11, 0xFF000000 | var29 << 16 | var28 << 8 | var27);
                    }
                }

                this.lightTexture.upload();
                this.minecraft.getProfiler().pop();
            }
        }
    }

    private float notGamma(float param0x) {
        float var0x = 1.0F - param0x;
        return 1.0F - var0x * var0x * var0x * var0x;
    }

    private float getBrightness(Level param0, int param1) {
        return param0.dimensionType().brightness(param1);
    }

    public static int pack(int param0, int param1) {
        return param0 << 4 | param1 << 20;
    }

    public static int block(int param0) {
        return param0 >> 4 & 65535;
    }

    public static int sky(int param0) {
        return param0 >> 20 & 65535;
    }
}
