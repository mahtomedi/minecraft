package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FogRenderer {
    private final FloatBuffer blackBuffer = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer colorBuffer = MemoryTracker.createFloatBuffer(16);
    private float fogRed;
    private float fogGreen;
    private float fogBlue;
    private float oldRed = -1.0F;
    private float oldGreen = -1.0F;
    private float oldBlue = -1.0F;
    private int targetBiomeFog = -1;
    private int previousBiomeFog = -1;
    private long biomeChangedTime = -1L;
    private final GameRenderer renderer;
    private final Minecraft minecraft;

    public FogRenderer(GameRenderer param0) {
        this.renderer = param0;
        this.minecraft = param0.getMinecraft();
        ((Buffer)this.blackBuffer.put(0.0F).put(0.0F).put(0.0F).put(1.0F)).flip();
    }

    public void setupClearColor(Camera param0, float param1) {
        Level var0 = this.minecraft.level;
        FluidState var1 = param0.getFluidInCamera();
        if (var1.is(FluidTags.WATER)) {
            this.setWaterFogColor(param0, var0);
        } else if (var1.is(FluidTags.LAVA)) {
            this.fogRed = 0.6F;
            this.fogGreen = 0.1F;
            this.fogBlue = 0.0F;
            this.biomeChangedTime = -1L;
        } else {
            this.setLandFogColor(param0, var0, param1);
            this.biomeChangedTime = -1L;
        }

        double var2 = param0.getPosition().y * var0.dimension.getClearColorScale();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int var3 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var3 < 20) {
                var2 *= (double)(1.0F - (float)var3 / 20.0F);
            } else {
                var2 = 0.0;
            }
        }

        if (var2 < 1.0) {
            if (var2 < 0.0) {
                var2 = 0.0;
            }

            var2 *= var2;
            this.fogRed = (float)((double)this.fogRed * var2);
            this.fogGreen = (float)((double)this.fogGreen * var2);
            this.fogBlue = (float)((double)this.fogBlue * var2);
        }

        if (this.renderer.getDarkenWorldAmount(param1) > 0.0F) {
            float var4 = this.renderer.getDarkenWorldAmount(param1);
            this.fogRed = this.fogRed * (1.0F - var4) + this.fogRed * 0.7F * var4;
            this.fogGreen = this.fogGreen * (1.0F - var4) + this.fogGreen * 0.6F * var4;
            this.fogBlue = this.fogBlue * (1.0F - var4) + this.fogBlue * 0.6F * var4;
        }

        if (var1.is(FluidTags.WATER)) {
            float var5 = 0.0F;
            if (param0.getEntity() instanceof LocalPlayer) {
                LocalPlayer var6 = (LocalPlayer)param0.getEntity();
                var5 = var6.getWaterVision();
            }

            float var7 = 1.0F / this.fogRed;
            if (var7 > 1.0F / this.fogGreen) {
                var7 = 1.0F / this.fogGreen;
            }

            if (var7 > 1.0F / this.fogBlue) {
                var7 = 1.0F / this.fogBlue;
            }

            this.fogRed = this.fogRed * (1.0F - var5) + this.fogRed * var7 * var5;
            this.fogGreen = this.fogGreen * (1.0F - var5) + this.fogGreen * var7 * var5;
            this.fogBlue = this.fogBlue * (1.0F - var5) + this.fogBlue * var7 * var5;
        } else if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float var8 = this.renderer.getNightVisionScale((LivingEntity)param0.getEntity(), param1);
            float var9 = 1.0F / this.fogRed;
            if (var9 > 1.0F / this.fogGreen) {
                var9 = 1.0F / this.fogGreen;
            }

            if (var9 > 1.0F / this.fogBlue) {
                var9 = 1.0F / this.fogBlue;
            }

            this.fogRed = this.fogRed * (1.0F - var8) + this.fogRed * var9 * var8;
            this.fogGreen = this.fogGreen * (1.0F - var8) + this.fogGreen * var9 * var8;
            this.fogBlue = this.fogBlue * (1.0F - var8) + this.fogBlue * var9 * var8;
        }

        RenderSystem.clearColor(this.fogRed, this.fogGreen, this.fogBlue, 0.0F);
    }

    private void setLandFogColor(Camera param0, Level param1, float param2) {
        float var0 = 0.25F + 0.75F * (float)this.minecraft.options.renderDistance / 32.0F;
        var0 = 1.0F - (float)Math.pow((double)var0, 0.25);
        Vec3 var1 = param1.getSkyColor(param0.getBlockPosition(), param2);
        float var2 = (float)var1.x;
        float var3 = (float)var1.y;
        float var4 = (float)var1.z;
        Vec3 var5 = param1.getFogColor(param2);
        this.fogRed = (float)var5.x;
        this.fogGreen = (float)var5.y;
        this.fogBlue = (float)var5.z;
        if (this.minecraft.options.renderDistance >= 4) {
            double var6 = Mth.sin(param1.getSunAngle(param2)) > 0.0F ? -1.0 : 1.0;
            Vec3 var7 = new Vec3(var6, 0.0, 0.0);
            float var8 = (float)param0.getLookVector().dot(var7);
            if (var8 < 0.0F) {
                var8 = 0.0F;
            }

            if (var8 > 0.0F) {
                float[] var9 = param1.dimension.getSunriseColor(param1.getTimeOfDay(param2), param2);
                if (var9 != null) {
                    var8 *= var9[3];
                    this.fogRed = this.fogRed * (1.0F - var8) + var9[0] * var8;
                    this.fogGreen = this.fogGreen * (1.0F - var8) + var9[1] * var8;
                    this.fogBlue = this.fogBlue * (1.0F - var8) + var9[2] * var8;
                }
            }
        }

        this.fogRed += (var2 - this.fogRed) * var0;
        this.fogGreen += (var3 - this.fogGreen) * var0;
        this.fogBlue += (var4 - this.fogBlue) * var0;
        float var10 = param1.getRainLevel(param2);
        if (var10 > 0.0F) {
            float var11 = 1.0F - var10 * 0.5F;
            float var12 = 1.0F - var10 * 0.4F;
            this.fogRed *= var11;
            this.fogGreen *= var11;
            this.fogBlue *= var12;
        }

        float var13 = param1.getThunderLevel(param2);
        if (var13 > 0.0F) {
            float var14 = 1.0F - var13 * 0.5F;
            this.fogRed *= var14;
            this.fogGreen *= var14;
            this.fogBlue *= var14;
        }

    }

    private void setWaterFogColor(Camera param0, LevelReader param1) {
        long var0 = Util.getMillis();
        int var1 = param1.getBiome(new BlockPos(param0.getPosition())).getWaterFogColor();
        if (this.biomeChangedTime < 0L) {
            this.targetBiomeFog = var1;
            this.previousBiomeFog = var1;
            this.biomeChangedTime = var0;
        }

        int var2 = this.targetBiomeFog >> 16 & 0xFF;
        int var3 = this.targetBiomeFog >> 8 & 0xFF;
        int var4 = this.targetBiomeFog & 0xFF;
        int var5 = this.previousBiomeFog >> 16 & 0xFF;
        int var6 = this.previousBiomeFog >> 8 & 0xFF;
        int var7 = this.previousBiomeFog & 0xFF;
        float var8 = Mth.clamp((float)(var0 - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
        float var9 = Mth.lerp(var8, (float)var5, (float)var2);
        float var10 = Mth.lerp(var8, (float)var6, (float)var3);
        float var11 = Mth.lerp(var8, (float)var7, (float)var4);
        this.fogRed = var9 / 255.0F;
        this.fogGreen = var10 / 255.0F;
        this.fogBlue = var11 / 255.0F;
        if (this.targetBiomeFog != var1) {
            this.targetBiomeFog = var1;
            this.previousBiomeFog = Mth.floor(var9) << 16 | Mth.floor(var10) << 8 | Mth.floor(var11);
            this.biomeChangedTime = var0;
        }

    }

    public void setupFog(Camera param0, int param1) {
        this.resetFogColor(false);
        RenderSystem.normal3f(0.0F, -1.0F, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        FluidState var0 = param0.getFluidInCamera();
        if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            float var1 = 5.0F;
            int var2 = ((LivingEntity)param0.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (var2 < 20) {
                var1 = Mth.lerp(1.0F - (float)var2 / 20.0F, 5.0F, this.renderer.getRenderDistance());
            }

            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (param1 == -1) {
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
            float var6 = this.renderer.getRenderDistance();
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (param1 == -1) {
                RenderSystem.fogStart(0.0F);
                RenderSystem.fogEnd(var6);
            } else {
                RenderSystem.fogStart(var6 * 0.75F);
                RenderSystem.fogEnd(var6);
            }

            RenderSystem.setupNvFogDistance();
            if (this.minecraft.level.dimension.isFoggyAt(Mth.floor(param0.getPosition().x), Mth.floor(param0.getPosition().z))
                || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog()) {
                RenderSystem.fogStart(var6 * 0.05F);
                RenderSystem.fogEnd(Math.min(var6, 192.0F) * 0.5F);
            }
        }

        RenderSystem.enableColorMaterial();
        RenderSystem.enableFog();
        RenderSystem.colorMaterial(1028, 4608);
    }

    public void resetFogColor(boolean param0) {
        if (param0) {
            RenderSystem.fog(2918, this.blackBuffer);
        } else {
            RenderSystem.fog(2918, this.updateColorBuffer());
        }

    }

    private FloatBuffer updateColorBuffer() {
        if (this.oldRed != this.fogRed || this.oldGreen != this.fogGreen || this.oldBlue != this.fogBlue) {
            ((Buffer)this.colorBuffer).clear();
            this.colorBuffer.put(this.fogRed).put(this.fogGreen).put(this.fogBlue).put(1.0F);
            ((Buffer)this.colorBuffer).flip();
            this.oldRed = this.fogRed;
            this.oldGreen = this.fogGreen;
            this.oldBlue = this.fogBlue;
        }

        return this.colorBuffer;
    }
}
