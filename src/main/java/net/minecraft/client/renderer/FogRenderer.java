package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog = -1;
    private static int previousBiomeFog = -1;
    private static long biomeChangedTime = -1L;

    public static void setupColor(Camera param0, float param1, ClientLevel param2, int param3, float param4) {
        FluidState var0 = param0.getFluidInCamera();
        if (var0.is(FluidTags.WATER)) {
            long var1 = Util.getMillis();
            int var2 = param2.getBiome(new BlockPos(param0.getPosition())).getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = var2;
                previousBiomeFog = var2;
                biomeChangedTime = var1;
            }

            int var3 = targetBiomeFog >> 16 & 0xFF;
            int var4 = targetBiomeFog >> 8 & 0xFF;
            int var5 = targetBiomeFog & 0xFF;
            int var6 = previousBiomeFog >> 16 & 0xFF;
            int var7 = previousBiomeFog >> 8 & 0xFF;
            int var8 = previousBiomeFog & 0xFF;
            float var9 = Mth.clamp((float)(var1 - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            float var10 = Mth.lerp(var9, (float)var6, (float)var3);
            float var11 = Mth.lerp(var9, (float)var7, (float)var4);
            float var12 = Mth.lerp(var9, (float)var8, (float)var5);
            fogRed = var10 / 255.0F;
            fogGreen = var11 / 255.0F;
            fogBlue = var12 / 255.0F;
            if (targetBiomeFog != var2) {
                targetBiomeFog = var2;
                previousBiomeFog = Mth.floor(var10) << 16 | Mth.floor(var11) << 8 | Mth.floor(var12);
                biomeChangedTime = var1;
            }
        } else if (var0.is(FluidTags.LAVA)) {
            fogRed = 0.6F;
            fogGreen = 0.1F;
            fogBlue = 0.0F;
            biomeChangedTime = -1L;
        } else {
            float var13 = 0.25F + 0.75F * (float)param3 / 32.0F;
            var13 = 1.0F - (float)Math.pow((double)var13, 0.25);
            Vec3 var14 = param2.getSkyColor(param0.getBlockPosition(), param1);
            float var15 = (float)var14.x;
            float var16 = (float)var14.y;
            float var17 = (float)var14.z;
            float var18 = Mth.clamp(Mth.cos(param2.getTimeOfDay(param1) * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager var19 = param2.getBiomeManager();
            Dimension var20 = param2.getDimension();
            Vec3 var21 = param0.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 var22 = CubicSampler.gaussianSampleVec3(
                var21,
                (param3x, param4x, param5) -> var20.getBrightnessDependentFogColor(
                        Vec3.fromRGB24(var19.getNoiseBiome(param3x, param4x, param5).getFogColor()), var18
                    )
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
                    float[] var26 = param2.dimension.getSunriseColor(param2.getTimeOfDay(param1), param1);
                    if (var26 != null) {
                        var25 *= var26[3];
                        fogRed = fogRed * (1.0F - var25) + var26[0] * var25;
                        fogGreen = fogGreen * (1.0F - var25) + var26[1] * var25;
                        fogBlue = fogBlue * (1.0F - var25) + var26[2] * var25;
                    }
                }
            }

            fogRed += (var15 - fogRed) * var13;
            fogGreen += (var16 - fogGreen) * var13;
            fogBlue += (var17 - fogBlue) * var13;
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

        double var32 = param0.getPosition().y * param2.dimension.getClearColorScale();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int var33 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var33 < 20) {
                var32 *= (double)(1.0F - (float)var33 / 20.0F);
            } else {
                var32 = 0.0;
            }
        }

        if (var32 < 1.0) {
            if (var32 < 0.0) {
                var32 = 0.0;
            }

            var32 *= var32;
            fogRed = (float)((double)fogRed * var32);
            fogGreen = (float)((double)fogGreen * var32);
            fogBlue = (float)((double)fogBlue * var32);
        }

        if (param4 > 0.0F) {
            fogRed = fogRed * (1.0F - param4) + fogRed * 0.7F * param4;
            fogGreen = fogGreen * (1.0F - param4) + fogGreen * 0.6F * param4;
            fogBlue = fogBlue * (1.0F - param4) + fogBlue * 0.6F * param4;
        }

        if (var0.is(FluidTags.WATER)) {
            float var34 = 0.0F;
            if (param0.getEntity() instanceof LocalPlayer) {
                LocalPlayer var35 = (LocalPlayer)param0.getEntity();
                var34 = var35.getWaterVision();
            }

            float var36 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - var34) + fogRed * var36 * var34;
            fogGreen = fogGreen * (1.0F - var34) + fogGreen * var36 * var34;
            fogBlue = fogBlue * (1.0F - var34) + fogBlue * var36 * var34;
        } else if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float var37 = GameRenderer.getNightVisionScale((LivingEntity)param0.getEntity(), param1);
            float var38 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - var37) + fogRed * var38 * var37;
            fogGreen = fogGreen * (1.0F - var37) + fogGreen * var38 * var37;
            fogBlue = fogBlue * (1.0F - var37) + fogBlue * var38 * var37;
        }

        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    public static void setupNoFog() {
        RenderSystem.fogDensity(0.0F);
        RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
    }

    public static void setupFog(Camera param0, FogRenderer.FogMode param1, float param2, boolean param3) {
        FluidState var0 = param0.getFluidInCamera();
        Entity var1 = param0.getEntity();
        boolean var2 = var0.getType() != Fluids.EMPTY;
        if (var2) {
            float var3 = 1.0F;
            if (var0.is(FluidTags.WATER)) {
                var3 = 0.05F;
                if (var1 instanceof LocalPlayer) {
                    LocalPlayer var4 = (LocalPlayer)var1;
                    var3 -= var4.getWaterVision() * var4.getWaterVision() * 0.03F;
                    Biome var5 = var4.level.getBiome(new BlockPos(var4));
                    if (var5 == Biomes.SWAMP || var5 == Biomes.SWAMP_HILLS) {
                        var3 += 0.005F;
                    }
                }
            } else if (var0.is(FluidTags.LAVA)) {
                var3 = 2.0F;
            }

            RenderSystem.fogDensity(var3);
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
        } else {
            float var8;
            float var9;
            if (var1 instanceof LivingEntity && ((LivingEntity)var1).hasEffect(MobEffects.BLINDNESS)) {
                int var6 = ((LivingEntity)var1).getEffect(MobEffects.BLINDNESS).getDuration();
                float var7 = Mth.lerp(Math.min(1.0F, (float)var6 / 20.0F), param2, 5.0F);
                if (param1 == FogRenderer.FogMode.FOG_SKY) {
                    var8 = 0.0F;
                    var9 = var7 * 0.8F;
                } else {
                    var8 = var7 * 0.25F;
                    var9 = var7;
                }
            } else if (param3) {
                var8 = param2 * 0.05F;
                var9 = Math.min(param2, 192.0F) * 0.5F;
            } else if (param1 == FogRenderer.FogMode.FOG_SKY) {
                var8 = 0.0F;
                var9 = param2;
            } else {
                var8 = param2 * 0.75F;
                var9 = param2;
            }

            RenderSystem.fogStart(var8);
            RenderSystem.fogEnd(var9);
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            RenderSystem.setupNvFogDistance();
        }

    }

    public static void levelFogColor() {
        RenderSystem.fog(2918, fogRed, fogGreen, fogBlue, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;
    }
}
