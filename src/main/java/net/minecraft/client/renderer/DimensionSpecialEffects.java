package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DimensionSpecialEffects {
    private static final Object2ObjectMap<ResourceKey<DimensionType>, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap<>(), param0 -> {
        DimensionSpecialEffects.OverworldEffects var0 = new DimensionSpecialEffects.OverworldEffects();
        param0.defaultReturnValue(var0);
        param0.put(DimensionType.OVERWORLD_LOCATION, var0);
        param0.put(DimensionType.NETHER_LOCATION, new DimensionSpecialEffects.NetherEffects());
        param0.put(DimensionType.END_LOCATION, new DimensionSpecialEffects.EndEffects());
    });
    private final float[] sunriseCol = new float[4];
    private final float cloudLevel;
    private final boolean hasGround;
    private final boolean renderNormalSky;

    public DimensionSpecialEffects(float param0, boolean param1, boolean param2) {
        this.cloudLevel = param0;
        this.hasGround = param1;
        this.renderNormalSky = param2;
    }

    public static DimensionSpecialEffects forType(@Nullable ResourceKey<DimensionType> param0) {
        return EFFECTS.get(param0);
    }

    @Nullable
    public float[] getSunriseColor(float param0, float param1) {
        float var0 = 0.4F;
        float var1 = Mth.cos(param0 * (float) (Math.PI * 2)) - 0.0F;
        float var2 = -0.0F;
        if (var1 >= -0.4F && var1 <= 0.4F) {
            float var3 = (var1 - -0.0F) / 0.4F * 0.5F + 0.5F;
            float var4 = 1.0F - (1.0F - Mth.sin(var3 * (float) Math.PI)) * 0.99F;
            var4 *= var4;
            this.sunriseCol[0] = var3 * 0.3F + 0.7F;
            this.sunriseCol[1] = var3 * var3 * 0.7F + 0.2F;
            this.sunriseCol[2] = var3 * var3 * 0.0F + 0.2F;
            this.sunriseCol[3] = var4;
            return this.sunriseCol;
        } else {
            return null;
        }
    }

    public float getCloudHeight() {
        return this.cloudLevel;
    }

    public boolean hasGround() {
        return this.hasGround;
    }

    public abstract Vec3 getBrightnessDependentFogColor(Vec3 var1, float var2);

    public abstract boolean isFoggyAt(int var1, int var2);

    public boolean renderNormalSky() {
        return this.renderNormalSky;
    }

    @OnlyIn(Dist.CLIENT)
    public static class EndEffects extends DimensionSpecialEffects {
        public EndEffects() {
            super(Float.NaN, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
            return param0.scale(0.15F);
        }

        @Override
        public boolean isFoggyAt(int param0, int param1) {
            return false;
        }

        @Nullable
        @Override
        public float[] getSunriseColor(float param0, float param1) {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NetherEffects extends DimensionSpecialEffects {
        public NetherEffects() {
            super(Float.NaN, true, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
            return param0;
        }

        @Override
        public boolean isFoggyAt(int param0, int param1) {
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OverworldEffects extends DimensionSpecialEffects {
        public OverworldEffects() {
            super(128.0F, true, true);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
            return param0.multiply((double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.91F + 0.09F));
        }

        @Override
        public boolean isFoggyAt(int param0, int param1) {
            return false;
        }
    }
}
