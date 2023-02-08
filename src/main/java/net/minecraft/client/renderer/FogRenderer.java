package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.newArrayList(
        new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction()
    );
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
            float var11 = (float)Mth.lerp(var10, var7, var4);
            float var12 = (float)Mth.lerp(var10, var8, var5);
            float var13 = (float)Mth.lerp(var10, var9, var6);
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
        FogRenderer.MobEffectFogFunction var33 = getPriorityFogFunction(var1, param1);
        if (var33 != null) {
            LivingEntity var34 = (LivingEntity)var1;
            var32 = var33.getModifiedVoidDarkness(var34, var34.getEffect(var33.getMobEffect()), var32, param1);
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

        float var35;
        if (var0 == FogType.WATER) {
            if (var1 instanceof LocalPlayer) {
                var35 = ((LocalPlayer)var1).getWaterVision();
            } else {
                var35 = 1.0F;
            }
        } else {
            label86: {
                if (var1 instanceof LivingEntity var37 && var37.hasEffect(MobEffects.NIGHT_VISION) && !var37.hasEffect(MobEffects.DARKNESS)) {
                    var35 = GameRenderer.getNightVisionScale(var37, param1);
                    break label86;
                }

                var35 = 0.0F;
            }
        }

        if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
            float var40 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - var35) + fogRed * var40 * var35;
            fogGreen = fogGreen * (1.0F - var35) + fogGreen * var40 * var35;
            fogBlue = fogBlue * (1.0F - var35) + fogBlue * var40 * var35;
        }

        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    public static void setupNoFog() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    @Nullable
    private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity param0, float param1) {
        return param0 instanceof LivingEntity var0 ? MOB_EFFECT_FOG.stream().filter(param2 -> param2.isEnabled(var0, param1)).findFirst().orElse(null) : null;
    }

    public static void setupFog(Camera param0, FogRenderer.FogMode param1, float param2, boolean param3, float param4) {
        FogType var0 = param0.getFluidInCamera();
        Entity var1 = param0.getEntity();
        FogRenderer.FogData var2 = new FogRenderer.FogData(param1);
        FogRenderer.MobEffectFogFunction var3 = getPriorityFogFunction(var1, param4);
        if (var0 == FogType.LAVA) {
            if (var1.isSpectator()) {
                var2.start = -8.0F;
                var2.end = param2 * 0.5F;
            } else if (var1 instanceof LivingEntity && ((LivingEntity)var1).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                var2.start = 0.0F;
                var2.end = 3.0F;
            } else {
                var2.start = 0.25F;
                var2.end = 1.0F;
            }
        } else if (var0 == FogType.POWDER_SNOW) {
            if (var1.isSpectator()) {
                var2.start = -8.0F;
                var2.end = param2 * 0.5F;
            } else {
                var2.start = 0.0F;
                var2.end = 2.0F;
            }
        } else if (var3 != null) {
            LivingEntity var4 = (LivingEntity)var1;
            MobEffectInstance var5 = var4.getEffect(var3.getMobEffect());
            if (var5 != null) {
                var3.setupFog(var2, var4, var5, param2, param4);
            }
        } else if (var0 == FogType.WATER) {
            var2.start = -8.0F;
            var2.end = 96.0F;
            if (var1 instanceof LocalPlayer var6) {
                var2.end *= Math.max(0.25F, var6.getWaterVision());
                Holder<Biome> var7 = var6.level.getBiome(var6.blockPosition());
                if (var7.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    var2.end *= 0.85F;
                }
            }

            if (var2.end > param2) {
                var2.end = param2;
                var2.shape = FogShape.CYLINDER;
            }
        } else if (param3) {
            var2.start = param2 * 0.05F;
            var2.end = Math.min(param2, 192.0F) * 0.5F;
        } else if (param1 == FogRenderer.FogMode.FOG_SKY) {
            var2.start = 0.0F;
            var2.end = param2;
            var2.shape = FogShape.CYLINDER;
        } else {
            float var8 = Mth.clamp(param2 / 10.0F, 4.0F, 64.0F);
            var2.start = param2 - var8;
            var2.end = param2;
            var2.shape = FogShape.CYLINDER;
        }

        RenderSystem.setShaderFogStart(var2.start);
        RenderSystem.setShaderFogEnd(var2.end);
        RenderSystem.setShaderFogShape(var2.shape);
    }

    public static void levelFogColor() {
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
    }

    @OnlyIn(Dist.CLIENT)
    static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
        @Override
        public MobEffect getMobEffect() {
            return MobEffects.BLINDNESS;
        }

        @Override
        public void setupFog(FogRenderer.FogData param0, LivingEntity param1, MobEffectInstance param2, float param3, float param4) {
            float var0 = param2.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)param2.getDuration() / 20.0F), param3, 5.0F);
            if (param0.mode == FogRenderer.FogMode.FOG_SKY) {
                param0.start = 0.0F;
                param0.end = var0 * 0.8F;
            } else {
                param0.start = var0 * 0.25F;
                param0.end = var0;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
        @Override
        public MobEffect getMobEffect() {
            return MobEffects.DARKNESS;
        }

        @Override
        public void setupFog(FogRenderer.FogData param0, LivingEntity param1, MobEffectInstance param2, float param3, float param4) {
            if (!param2.getFactorData().isEmpty()) {
                float var0 = Mth.lerp(param2.getFactorData().get().getFactor(param1, param4), param3, 15.0F);
                param0.start = param0.mode == FogRenderer.FogMode.FOG_SKY ? 0.0F : var0 * 0.75F;
                param0.end = var0;
            }
        }

        @Override
        public float getModifiedVoidDarkness(LivingEntity param0, MobEffectInstance param1, float param2, float param3) {
            return param1.getFactorData().isEmpty() ? 0.0F : 1.0F - param1.getFactorData().get().getFactor(param0, param3);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class FogData {
        public final FogRenderer.FogMode mode;
        public float start;
        public float end;
        public FogShape shape = FogShape.SPHERE;

        public FogData(FogRenderer.FogMode param0) {
            this.mode = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;
    }

    @OnlyIn(Dist.CLIENT)
    interface MobEffectFogFunction {
        MobEffect getMobEffect();

        void setupFog(FogRenderer.FogData var1, LivingEntity var2, MobEffectInstance var3, float var4, float var5);

        default boolean isEnabled(LivingEntity param0, float param1) {
            return param0.hasEffect(this.getMobEffect());
        }

        default float getModifiedVoidDarkness(LivingEntity param0, MobEffectInstance param1, float param2, float param3) {
            MobEffectInstance var0 = param0.getEffect(this.getMobEffect());
            if (var0 != null) {
                if (var0.endsWithin(19)) {
                    param2 = 1.0F - (float)var0.getDuration() / 20.0F;
                } else {
                    param2 = 0.0F;
                }
            }

            return param2;
        }
    }
}
