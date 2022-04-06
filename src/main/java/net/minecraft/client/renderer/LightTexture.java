package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
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
        this.blockLightRedFlicker += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.blockLightRedFlicker *= 0.9F;
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

    private float getDarknessGamma(float param0) {
        if (this.minecraft.player.hasEffect(MobEffects.DARKNESS)) {
            MobEffectInstance var0 = this.minecraft.player.getEffect(MobEffects.DARKNESS);
            if (var0 != null && var0.getFactorData().isPresent()) {
                return var0.getFactorData().get().getFactor(param0);
            }
        }

        return 0.0F;
    }

    private float calculateDarknessScale(LivingEntity param0, float param1, float param2) {
        float var0 = 0.45F * param1;
        return Math.max(0.0F, Mth.cos(((float)param0.tickCount - param2) * (float) Math.PI * 0.025F) * var0);
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

                float var4 = this.minecraft.options.darknessEffectScale().get().floatValue();
                float var5 = this.getDarknessGamma(param0) * var4;
                float var6 = this.calculateDarknessScale(this.minecraft.player, var5, param0) * var4;
                float var7 = this.minecraft.player.getWaterVision();
                float var8;
                if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
                    var8 = GameRenderer.getNightVisionScale(this.minecraft.player, param0);
                } else if (var7 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    var8 = var7;
                } else {
                    var8 = 0.0F;
                }

                Vector3f var11 = new Vector3f(var1, var1, 1.0F);
                var11.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                float var12 = this.blockLightRedFlicker + 1.5F;
                Vector3f var13 = new Vector3f();

                for(int var14 = 0; var14 < 16; ++var14) {
                    for(int var15 = 0; var15 < 16; ++var15) {
                        float var16 = getBrightness(var0.dimensionType(), var14) * var2;
                        float var17 = getBrightness(var0.dimensionType(), var15) * var12;
                        float var19 = var17 * ((var17 * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float var20 = var17 * (var17 * var17 * 0.6F + 0.4F);
                        var13.set(var17, var19, var20);
                        boolean var21 = var0.effects().forceBrightLightmap();
                        if (var21) {
                            var13.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                            var13.clamp(0.0F, 1.0F);
                        } else {
                            Vector3f var22 = var11.copy();
                            var22.mul(var16);
                            var13.add(var22);
                            var13.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if (this.renderer.getDarkenWorldAmount(param0) > 0.0F) {
                                float var23 = this.renderer.getDarkenWorldAmount(param0);
                                Vector3f var24 = var13.copy();
                                var24.mul(0.7F, 0.6F, 0.6F);
                                var13.lerp(var24, var23);
                            }
                        }

                        if (var8 > 0.0F) {
                            float var25 = Math.max(var13.x(), Math.max(var13.y(), var13.z()));
                            if (var25 < 1.0F) {
                                float var26 = 1.0F / var25;
                                Vector3f var27 = var13.copy();
                                var27.mul(var26);
                                var13.lerp(var27, var8);
                            }
                        }

                        if (!var21) {
                            if (var6 > 0.0F) {
                                var13.add(-var6, -var6, -var6);
                            }

                            var13.clamp(0.0F, 1.0F);
                        }

                        float var28 = this.minecraft.options.gamma().get().floatValue();
                        Vector3f var29 = var13.copy();
                        var29.map(this::notGamma);
                        var13.lerp(var29, Math.max(0.0F, var28 - var5));
                        var13.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        var13.clamp(0.0F, 1.0F);
                        var13.mul(255.0F);
                        int var30 = 255;
                        int var31 = (int)var13.x();
                        int var32 = (int)var13.y();
                        int var33 = (int)var13.z();
                        this.lightPixels.setPixelRGBA(var15, var14, 0xFF000000 | var33 << 16 | var32 << 8 | var31);
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

    public static float getBrightness(DimensionType param0, int param1) {
        float var0 = (float)param1 / 15.0F;
        float var1 = var0 / (4.0F - 3.0F * var0);
        return Mth.lerp(param0.ambientLight(), var1, 1.0F);
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
