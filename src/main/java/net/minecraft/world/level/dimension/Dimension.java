package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;

public abstract class Dimension {
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    protected final Level level;
    private final DimensionType type;
    protected final float[] brightnessRamp = new float[16];

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

    public float getBrightness(int param0) {
        return this.brightnessRamp[param0];
    }

    public abstract float getTimeOfDay(long var1, float var3);

    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }

    public abstract DimensionType getType();

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return null;
    }

    public void saveData(ServerLevelData param0) {
    }

    public void tick() {
    }

    @Nullable
    public abstract BlockPos getSpawnPosInChunk(long var1, ChunkPos var3, boolean var4);

    @Nullable
    public abstract BlockPos getValidSpawnPosition(long var1, int var3, int var4, boolean var5);

    public abstract boolean isNaturalDimension();

    public abstract boolean mayRespawn();
}
