package net.minecraft.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog = -1;
    private static int previousBiomeFog = -1;
    private static long biomeChangedTime = -1L;

    public static void setupColor(Camera param0, float param1, ClientLevel param2, int param3, float param4) {
        FogType var0 = param0.getFluidInCamera();
        Entity var1 = param0.getEntity();
        if (var0 == FogType.WATER) {
            long var2 = Util.getMillis();
            int var3 = param2.getBiome(new BlockPos(param0.getPosition())).value().getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = var3;
                previousBiomeFog = var3;
                biomeChangedTime = var2;
            }

            int var4 = targetBiomeFog >> 16 & 0xFF;
            int var5 = targetBiomeFog >> 8 & 0xFF;
            int var6 = targetBiomeFog & 0xFF;
            int var7 = previousBiomeFog >> 16 & 0xFF;
            int var8 = previousBiomeFog >> 8 & 0xFF;
            int var9 = previousBiomeFog & 0xFF;
            float var10 = Mth.clamp((float)(var2 - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            float var11 = Mth.lerp(var10, (float)var7, (float)var4);
            float var12 = Mth.lerp(var10, (float)var8, (float)var5);
            float var13 = Mth.lerp(var10, (float)var9, (float)var6);
            fogRed = var11 / 255.0F;
            fogGreen = var12 / 255.0F;
            fogBlue = var13 / 255.0F;
            if (targetBiomeFog != var3) {
                targetBiomeFog = var3;
                previousBiomeFog = Mth.floor(var11) << 16 | Mth.floor(var12) << 8 | Mth.floor(var13);
                biomeChangedTime = var2;
            }
        } else if (var0 == FogType.LAVA) {
            fogRed = 0.6F;
            fogGreen = 0.1F;
            fogBlue = 0.0F;
            biomeChangedTime = -1L;
        } else if (var0 == FogType.POWDER_SNOW) {
            fogRed = 0.623F;
            fogGreen = 0.734F;
            fogBlue = 0.785F;
            biomeChangedTime = -1L;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        } else {
            float var14 = 0.25F + 0.75F * (float)param3 / 32.0F;
            var14 = 1.0F - (float)Math.pow((double)var14, 0.25);
            Vec3 var15 = param2.getSkyColor(param0.getPosition(), param1);
            float var16 = (float)var15.x;
            float var17 = (float)var15.y;
            float var18 = (float)var15.z;
            float var19 = Mth.clamp(Mth.cos(param2.getTimeOfDay(param1) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager var20 = param2.getBiomeManager();
            Vec3 var21 = param0.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 var22 = CubicSampler.gaussianSampleVec3(
                var21,
                (param3x, param4x, param5) -> param2.effects()
                        .getBrightnessDependentFogColor(Vec3.fromRGB24(var20.getNoiseBiomeAtQuart(param3x, param4x, param5).value().getFogColor()), var19)
            );
            fogRed = (float)var22.x();
            fogGreen = (float)var22.y();
            fogBlue = (float)var22.z();
            if (param3 >= 4) {
                float var23 = Mth.sin(param2.getSunAngle(param1)) > 0.0F ? -1.0F : 1.0F;
                Vector3f var24 = new Vector3f(var23, 0.0F, 0.0F);
                float var25 = param0.getLookVector().dot(var24);
                if (var25 < 0.0F) {
                    var25 = 0.0F;
                }

                if (var25 > 0.0F) {
                    float[] var26 = param2.effects().getSunriseColor(param2.getTimeOfDay(param1), param1);
                    if (var26 != null) {
                        var25 *= var26[3];
                        fogRed = fogRed * (1.0F - var25) + var26[0] * var25;
                        fogGreen = fogGreen * (1.0F - var25) + var26[1] * var25;
                        fogBlue = fogBlue * (1.0F - var25) + var26[2] * var25;
                    }
                }
            }

            fogRed += (var16 - fogRed) * var14;
            fogGreen += (var17 - fogGreen) * var14;
            fogBlue += (var18 - fogBlue) * var14;
            float var27 = param2.getRainLevel(param1);
            if (var27 > 0.0F) {
                float var28 = 1.0F - var27 * 0.5F;
                float var29 = 1.0F - var27 * 0.4F;
                fogRed *= var28;
                fogGreen *= var28;
                fogBlue *= var29;
            }

            float var30 = param2.getThunderLevel(param1);
            if (var30 > 0.0F) {
                float var31 = 1.0F - var30 * 0.5F;
                fogRed *= var31;
                fogGreen *= var31;
                fogBlue *= var31;
            }

            biomeChangedTime = -1L;
        }

        float var32 = ((float)param0.getPosition().y - (float)param2.getMinBuildHeight()) * param2.getLevelData().getClearColorScale();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int var33 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var33 < 20) {
                var32 = 1.0F - (float)var33 / 20.0F;
            } else {
                var32 = 0.0F;
            }
        }

        if (var32 < 1.0F && var0 != FogType.LAVA && var0 != FogType.POWDER_SNOW) {
            if (var32 < 0.0F) {
                var32 = 0.0F;
            }

            var32 *= var32;
            fogRed *= var32;
            fogGreen *= var32;
            fogBlue *= var32;
        }

        if (param4 > 0.0F) {
            fogRed = fogRed * (1.0F - param4) + fogRed * 0.7F * param4;
            fogGreen = fogGreen * (1.0F - param4) + fogGreen * 0.6F * param4;
            fogBlue = fogBlue * (1.0F - param4) + fogBlue * 0.6F * param4;
        }

        float var34;
        if (var0 == FogType.WATER) {
            if (var1 instanceof LocalPlayer) {
                var34 = ((LocalPlayer)var1).getWaterVision();
            } else {
                var34 = 1.0F;
            }
        } else if (var1 instanceof LivingEntity && ((LivingEntity)var1).hasEffect(MobEffects.NIGHT_VISION)) {
            var34 = GameRenderer.getNightVisionScale((LivingEntity)var1, param1);
        } else {
            var34 = 0.0F;
        }

        if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
            float var38 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - var34) + fogRed * var38 * var34;
            fogGreen = fogGreen * (1.0F - var34) + fogGreen * var38 * var34;
            fogBlue = fogBlue * (1.0F - var34) + fogBlue * var38 * var34;
        }

        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    public static void setupNoFog() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    public static void setupFog(Camera param0, FogRenderer.FogMode param1, float param2, boolean param3) {
        FogType var0 = param0.getFluidInCamera();
        Entity var1 = param0.getEntity();
        FogShape var2 = FogShape.SPHERE;
        float var3;
        float var4;
        if (var0 == FogType.LAVA) {
            if (var1.isSpectator()) {
                var3 = -8.0F;
                var4 = param2 * 0.5F;
            } else if (var1 instanceof LivingEntity && ((LivingEntity)var1).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                var3 = 0.0F;
                var4 = 3.0F;
            } else {
                var3 = 0.25F;
                var4 = 1.0F;
            }
        } else if (var0 == FogType.POWDER_SNOW) {
            if (var1.isSpectator()) {
                var3 = -8.0F;
                var4 = param2 * 0.5F;
            } else {
                var3 = 0.0F;
                var4 = 2.0F;
            }
        } else if (var1 instanceof LivingEntity && ((LivingEntity)var1).hasEffect(MobEffects.BLINDNESS)) {
            int var13 = ((LivingEntity)var1).getEffect(MobEffects.BLINDNESS).getDuration();
            float var14 = Mth.lerp(Math.min(1.0F, (float)var13 / 20.0F), param2, 5.0F);
            if (param1 == FogRenderer.FogMode.FOG_SKY) {
                var3 = 0.0F;
                var4 = var14 * 0.8F;
            } else {
                var3 = var0 == FogType.WATER ? -4.0F : var14 * 0.25F;
                var4 = var14;
            }
        } else if (var0 == FogType.WATER) {
            var3 = -8.0F;
            var4 = 96.0F;
            if (var1 instanceof LocalPlayer var21) {
                var4 *= Math.max(0.25F, var21.getWaterVision());
                Holder<Biome> var22 = var21.level.getBiome(var21.blockPosition());
                if (Biome.getBiomeCategory(var22) == Biome.BiomeCategory.SWAMP) {
                    var4 *= 0.85F;
                }
            }

            if (var4 > param2) {
                var4 = param2;
                var2 = FogShape.CYLINDER;
            }
        } else if (param3) {
            var3 = param2 * 0.05F;
            var4 = Math.min(param2, 192.0F) * 0.5F;
        } else if (param1 == FogRenderer.FogMode.FOG_SKY) {
            var3 = 0.0F;
            var4 = param2;
            var2 = FogShape.CYLINDER;
        } else {
            float var27 = Mth.clamp(param2 / 10.0F, 4.0F, 64.0F);
            var3 = param2 - var27;
            var4 = param2;
            var2 = FogShape.CYLINDER;
        }

        RenderSystem.setShaderFogStart(var3);
        RenderSystem.setShaderFogEnd(var4);
        RenderSystem.setShaderFogShape(var2);
    }

    public static void levelFogColor() {
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;
    }
}
