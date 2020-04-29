package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Dimension {
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    protected final Level level;
    private final DimensionType type;
    protected boolean ultraWarm;
    protected boolean hasCeiling;
    protected final float[] brightnessRamp = new float[16];
    private final float[] sunriseCol = new float[4];

    public Dimension(Level param0, DimensionType param1, float param2) {
        this.level = param0;
        this.type = param1;

        for(int var0 = 0; var0 <= 15; ++var0) {
            float var1 = (float)var0 / 15.0F;
            float var2 = var1 / (4.0F - 3.0F * var1);
            this.brightnessRamp[var0] = Mth.lerp(param2, var2, 1.0F);
        }

    }

    public int getMoonPhase(long param0) {
        return (int)(param0 / 24000L % 8L + 8L) % 8;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    public float getCloudHeight() {
        return 128.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasGround() {
        return true;
    }

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public double getClearColorScale() {
        return this.level.getLevelData().getGeneratorType() == LevelType.FLAT ? 1.0 : 0.03125;
    }

    public boolean isUltraWarm() {
        return this.ultraWarm;
    }

    public boolean isHasSkyLight() {
        return this.type.hasSkyLight();
    }

    public boolean isHasCeiling() {
        return this.hasCeiling;
    }

    public float getBrightness(int param0) {
        return this.brightnessRamp[param0];
    }

    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }

    public void saveData(ServerLevelData param0) {
    }

    public void tick() {
    }

    public abstract ChunkGenerator<?> createRandomLevelGenerator();

    @Nullable
    public abstract BlockPos getSpawnPosInChunk(ChunkPos var1, boolean var2);

    @Nullable
    public abstract BlockPos getValidSpawnPosition(int var1, int var2, boolean var3);

    public abstract float getTimeOfDay(long var1, float var3);

    public abstract boolean isNaturalDimension();

    @OnlyIn(Dist.CLIENT)
    public abstract Vec3 getBrightnessDependentFogColor(Vec3 var1, float var2);

    public abstract boolean mayRespawn();

    @OnlyIn(Dist.CLIENT)
    public abstract boolean isFoggyAt(int var1, int var2);

    public abstract DimensionType getType();
}
