package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private static final FloatBuffer BLACK_BUFFER = Util.make(
        MemoryTracker.createFloatBuffer(16), param0 -> ((Buffer)param0.put(0.0F).put(0.0F).put(0.0F).put(1.0F)).flip()
    );
    private static final FloatBuffer COLOR_BUFFER = MemoryTracker.createFloatBuffer(16);
    private float fogRed;
    private float fogGreen;
    private float fogBlue;
    private int targetBiomeFog = -1;
    private int previousBiomeFog = -1;
    private long biomeChangedTime = -1L;

    public void setupClearColor(Camera param0, float param1, Level param2, int param3, float param4) {
        FluidState var0 = param0.getFluidInCamera();
        float var13;
        float var14;
        float var15;
        if (var0.is(FluidTags.WATER)) {
            long var1 = Util.getMillis();
            int var2 = param2.getBiome(new BlockPos(param0.getPosition())).getWaterFogColor();
            if (this.biomeChangedTime < 0L) {
                this.targetBiomeFog = var2;
                this.previousBiomeFog = var2;
                this.biomeChangedTime = var1;
            }

            int var3 = this.targetBiomeFog >> 16 & 0xFF;
            int var4 = this.targetBiomeFog >> 8 & 0xFF;
            int var5 = this.targetBiomeFog & 0xFF;
            int var6 = this.previousBiomeFog >> 16 & 0xFF;
            int var7 = this.previousBiomeFog >> 8 & 0xFF;
            int var8 = this.previousBiomeFog & 0xFF;
            float var9 = Mth.clamp((float)(var1 - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            float var10 = Mth.lerp(var9, (float)var6, (float)var3);
            float var11 = Mth.lerp(var9, (float)var7, (float)var4);
            float var12 = Mth.lerp(var9, (float)var8, (float)var5);
            var13 = var10 / 255.0F;
            var14 = var11 / 255.0F;
            var15 = var12 / 255.0F;
            if (this.targetBiomeFog != var2) {
                this.targetBiomeFog = var2;
                this.previousBiomeFog = Mth.floor(var10) << 16 | Mth.floor(var11) << 8 | Mth.floor(var12);
                this.biomeChangedTime = var1;
            }
        } else if (var0.is(FluidTags.LAVA)) {
            var13 = 0.6F;
            var14 = 0.1F;
            var15 = 0.0F;
            this.biomeChangedTime = -1L;
        } else {
            float var19 = 0.25F + 0.75F * (float)param3 / 32.0F;
            var19 = 1.0F - (float)Math.pow((double)var19, 0.25);
            Vec3 var20 = param2.getSkyColor(param0.getBlockPosition(), param1);
            float var21 = (float)var20.x;
            float var22 = (float)var20.y;
            float var23 = (float)var20.z;
            Vec3 var24 = param2.getFogColor(param1);
            var13 = (float)var24.x;
            var14 = (float)var24.y;
            var15 = (float)var24.z;
            if (param3 >= 4) {
                double var28 = Mth.sin(param2.getSunAngle(param1)) > 0.0F ? -1.0 : 1.0;
                Vec3 var29 = new Vec3(var28, 0.0, 0.0);
                float var30 = (float)param0.getLookVector().dot(var29);
                if (var30 < 0.0F) {
                    var30 = 0.0F;
                }

                if (var30 > 0.0F) {
                    float[] var31 = param2.dimension.getSunriseColor(param2.getTimeOfDay(param1), param1);
                    if (var31 != null) {
                        var30 *= var31[3];
                        var13 = var13 * (1.0F - var30) + var31[0] * var30;
                        var14 = var14 * (1.0F - var30) + var31[1] * var30;
                        var15 = var15 * (1.0F - var30) + var31[2] * var30;
                    }
                }
            }

            var13 += (var21 - var13) * var19;
            var14 += (var22 - var14) * var19;
            var15 += (var23 - var15) * var19;
            float var32 = param2.getRainLevel(param1);
            if (var32 > 0.0F) {
                float var33 = 1.0F - var32 * 0.5F;
                float var34 = 1.0F - var32 * 0.4F;
                var13 *= var33;
                var14 *= var33;
                var15 *= var34;
            }

            float var35 = param2.getThunderLevel(param1);
            if (var35 > 0.0F) {
                float var36 = 1.0F - var35 * 0.5F;
                var13 *= var36;
                var14 *= var36;
                var15 *= var36;
            }

            this.biomeChangedTime = -1L;
        }

        double var37 = param0.getPosition().y * param2.dimension.getClearColorScale();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int var38 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var38 < 20) {
                var37 *= (double)(1.0F - (float)var38 / 20.0F);
            } else {
                var37 = 0.0;
            }
        }

        if (var37 < 1.0) {
            if (var37 < 0.0) {
                var37 = 0.0;
            }

            var37 *= var37;
            var13 = (float)((double)var13 * var37);
            var14 = (float)((double)var14 * var37);
            var15 = (float)((double)var15 * var37);
        }

        if (param4 > 0.0F) {
            var13 = var13 * (1.0F - param4) + var13 * 0.7F * param4;
            var14 = var14 * (1.0F - param4) + var14 * 0.6F * param4;
            var15 = var15 * (1.0F - param4) + var15 * 0.6F * param4;
        }

        if (var0.is(FluidTags.WATER)) {
            float var39 = 0.0F;
            if (param0.getEntity() instanceof LocalPlayer) {
                LocalPlayer var40 = (LocalPlayer)param0.getEntity();
                var39 = var40.getWaterVision();
            }

            float var41 = Math.min(1.0F / var13, Math.min(1.0F / var14, 1.0F / var15));
            var13 = var13 * (1.0F - var39) + var13 * var41 * var39;
            var14 = var14 * (1.0F - var39) + var14 * var41 * var39;
            var15 = var15 * (1.0F - var39) + var15 * var41 * var39;
        } else if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float var42 = GameRenderer.getNightVisionScale((LivingEntity)param0.getEntity(), param1);
            float var43 = Math.min(1.0F / var13, Math.min(1.0F / var14, 1.0F / var15));
            var13 = var13 * (1.0F - var42) + var13 * var43 * var42;
            var14 = var14 * (1.0F - var42) + var14 * var43 * var42;
            var15 = var15 * (1.0F - var42) + var15 * var43 * var42;
        }

        RenderSystem.clearColor(var13, var14, var15, 0.0F);
        if (this.fogRed != var13 || this.fogGreen != var14 || this.fogBlue != var15) {
            ((Buffer)COLOR_BUFFER).clear();
            COLOR_BUFFER.put(var13).put(var14).put(var15).put(1.0F);
            ((Buffer)COLOR_BUFFER).flip();
            this.fogRed = var13;
            this.fogGreen = var14;
            this.fogBlue = var15;
        }

    }

    public static void setupFog(Camera param0, FogRenderer.FogMode param1, float param2, boolean param3) {
        resetFogColor(false);
        RenderSystem.normal3f(0.0F, -1.0F, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        FluidState var0 = param0.getFluidInCamera();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            float var1 = 5.0F;
            int var2 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var2 < 20) {
                var1 = Mth.lerp(1.0F - (float)var2 / 20.0F, 5.0F, param2);
            }

            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (param1 == FogRenderer.FogMode.FOG_SKY) {
                RenderSystem.fogStart(0.0F);
                RenderSystem.fogEnd(var1 * 0.8F);
            } else {
                RenderSystem.fogStart(var1 * 0.25F);
                RenderSystem.fogEnd(var1);
            }

            RenderSystem.setupNvFogDistance();
        } else if (var0.is(FluidTags.WATER)) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
            if (param0.getEntity() instanceof LivingEntity) {
                if (param0.getEntity() instanceof LocalPlayer) {
                    LocalPlayer var3 = (LocalPlayer)param0.getEntity();
                    float var4 = 0.05F - var3.getWaterVision() * var3.getWaterVision() * 0.03F;
                    Biome var5 = var3.level.getBiome(new BlockPos(var3));
                    if (var5 == Biomes.SWAMP || var5 == Biomes.SWAMP_HILLS) {
                        var4 += 0.005F;
                    }

                    RenderSystem.fogDensity(var4);
                } else {
                    RenderSystem.fogDensity(0.05F);
                }
            } else {
                RenderSystem.fogDensity(0.1F);
            }
        } else if (var0.is(FluidTags.LAVA)) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP);
            RenderSystem.fogDensity(2.0F);
        } else {
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (param1 == FogRenderer.FogMode.FOG_SKY) {
                RenderSystem.fogStart(0.0F);
                RenderSystem.fogEnd(param2);
            } else {
                RenderSystem.fogStart(param2 * 0.75F);
                RenderSystem.fogEnd(param2);
            }

            RenderSystem.setupNvFogDistance();
            if (param3) {
                RenderSystem.fogStart(param2 * 0.05F);
                RenderSystem.fogEnd(Math.min(param2, 192.0F) * 0.5F);
            }
        }

        RenderSystem.enableFog();
        RenderSystem.colorMaterial(1028, 4608);
    }

    public static void resetFogColor(boolean param0) {
        RenderSystem.fog(2918, param0 ? BLACK_BUFFER : COLOR_BUFFER);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;
    }
}
