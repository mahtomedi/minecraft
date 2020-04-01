package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.math.Vector3f;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Dimension {
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final Vector3f NO_CHANGE = new Vector3f(1.0F, 1.0F, 1.0F);
    protected final Level level;
    private final DimensionType type;
    protected boolean ultraWarm;
    protected boolean hasCeiling;
    protected final float[] brightnessRamp = new float[16];
    private final float[] sunriseCol = new float[4];
    private static final Vector3f ONES = new Vector3f(1.0F, 1.0F, 1.0F);

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

    public void saveData() {
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

    @OnlyIn(Dist.CLIENT)
    public void modifyLightmapColor(int param0, int param1, Vector3f param2) {
    }

    public float getBlockShade(Direction param0, boolean param1) {
        if (!param1) {
            return 1.0F;
        } else {
            switch(param0) {
                case DOWN:
                    return 0.5F;
                case UP:
                    return 1.0F;
                case NORTH:
                case SOUTH:
                    return 0.8F;
                case WEST:
                case EAST:
                    return 0.6F;
                default:
                    return 1.0F;
            }
        }
    }

    public final DimensionType getType() {
        return this.type;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("type"),
                    param0.createString(Registry.DIMENSION_TYPE.getKey(this.getType()).toString()),
                    param0.createString("generator"),
                    this.createRandomLevelGenerator().serialize(param0).getValue()
                )
            )
        );
    }

    public Stream<Biome> getKnownBiomes() {
        return this.createRandomLevelGenerator().getBiomeSource().getKnownBiomes();
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return NO_CHANGE;
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return NO_CHANGE;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isEndSky() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSunSize() {
        return 30.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getMoonSize() {
        return 20.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getSunTint() {
        return ONES;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getMoonTint() {
        return ONES;
    }
}
