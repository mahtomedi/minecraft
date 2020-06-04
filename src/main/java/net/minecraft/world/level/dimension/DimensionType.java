package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
    private static final Codec<ResourceKey<DimensionType>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC
        .xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location);
    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.LONG
                        .optionalFieldOf("fixed_time")
                        .xmap(
                            param0x -> param0x.map(OptionalLong::of).orElseGet(OptionalLong::empty),
                            param0x -> param0x.isPresent() ? Optional.of(param0x.getAsLong()) : Optional.empty()
                        )
                        .forGetter(param0x -> param0x.fixedTime),
                    Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight),
                    Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling),
                    Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm),
                    Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural),
                    Codec.BOOL.fieldOf("shrunk").forGetter(DimensionType::shrunk),
                    Codec.FLOAT.fieldOf("ambient_light").forGetter(param0x -> param0x.ambientLight)
                )
                .apply(param0, DimensionType::new)
    );
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    private static final DimensionType DEFAULT_OVERWORLD = new DimensionType(
        "", OptionalLong.empty(), true, false, false, true, false, false, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, Optional.of(OVERWORLD_LOCATION), 0.0F
    );
    private static final DimensionType DEFAULT_NETHER = new DimensionType(
        "_nether", OptionalLong.of(18000L), false, true, true, false, true, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(NETHER_LOCATION), 0.1F
    );
    private static final DimensionType DEFAULT_END = new DimensionType(
        "_end", OptionalLong.of(6000L), false, false, false, false, false, true, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(END_LOCATION), 0.0F
    );
    private static final Map<ResourceKey<DimensionType>, DimensionType> BUILTIN = ImmutableMap.of(
        OVERWORLD_LOCATION, defaultOverworld(), NETHER_LOCATION, DEFAULT_NETHER, END_LOCATION, DEFAULT_END
    );
    private static final Codec<DimensionType> BUILTIN_CODEC = RESOURCE_KEY_CODEC.flatXmap(
            param0 -> Optional.ofNullable(BUILTIN.get(param0))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error("Unknown builtin dimension: " + param0)),
            param0 -> param0.builtinKey.map(DataResult::success).orElseGet(() -> DataResult.error("Unknown builtin dimension: " + param0))
        )
        .stable();
    private static final Codec<DimensionType> BUILTIN_OR_DIRECT_CODEC = Codec.either(BUILTIN_CODEC, DIRECT_CODEC)
        .flatXmap(
            param0 -> param0.map(param0x -> DataResult.success(param0x, Lifecycle.stable()), DataResult::success),
            param0 -> param0.builtinKey.isPresent() ? DataResult.success(Either.left(param0), Lifecycle.stable()) : DataResult.success(Either.right(param0))
        );
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, BUILTIN_OR_DIRECT_CODEC);
    private final String fileSuffix;
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final boolean shrunk;
    private final boolean createDragonFight;
    private final BiomeZoomer biomeZoomer;
    private final Optional<ResourceKey<DimensionType>> builtinKey;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    public static DimensionType defaultOverworld() {
        return DEFAULT_OVERWORLD;
    }

    protected DimensionType(OptionalLong param0, boolean param1, boolean param2, boolean param3, boolean param4, boolean param5, float param6) {
        this("", param0, param1, param2, param3, param4, param5, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.empty(), param6);
    }

    protected DimensionType(
        String param0,
        OptionalLong param1,
        boolean param2,
        boolean param3,
        boolean param4,
        boolean param5,
        boolean param6,
        boolean param7,
        BiomeZoomer param8,
        Optional<ResourceKey<DimensionType>> param9,
        float param10
    ) {
        this.fileSuffix = param0;
        this.fixedTime = param1;
        this.hasSkylight = param2;
        this.hasCeiling = param3;
        this.ultraWarm = param4;
        this.natural = param5;
        this.shrunk = param6;
        this.createDragonFight = param7;
        this.biomeZoomer = param8;
        this.builtinKey = param9;
        this.ambientLight = param10;
        this.brightnessRamp = fillBrightnessRamp(param10);
    }

    private static float[] fillBrightnessRamp(float param0) {
        float[] var0 = new float[16];

        for(int var1 = 0; var1 <= 15; ++var1) {
            float var2 = (float)var1 / 15.0F;
            float var3 = var2 / (4.0F - 3.0F * var2);
            var0[var1] = Mth.lerp(param0, var3, 1.0F);
        }

        return var0;
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> param0) {
        DataResult<Number> var0 = param0.asNumber();
        if (var0.result().equals(Optional.of(-1))) {
            return DataResult.success(Level.NETHER);
        } else if (var0.result().equals(Optional.of(0))) {
            return DataResult.success(Level.OVERWORLD);
        } else {
            return var0.result().equals(Optional.of(1)) ? DataResult.success(Level.END) : Level.RESOURCE_KEY_CODEC.parse(param0);
        }
    }

    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder param0) {
        param0.registerDimension(OVERWORLD_LOCATION, defaultOverworld());
        param0.registerDimension(NETHER_LOCATION, DEFAULT_NETHER);
        param0.registerDimension(END_LOCATION, DEFAULT_END);
        return param0;
    }

    private static ChunkGenerator defaultEndGenerator(long param0) {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(param0), param0, NoiseGeneratorSettings.Preset.END.settings());
    }

    private static ChunkGenerator defaultNetherGenerator(long param0) {
        return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(param0), param0, NoiseGeneratorSettings.Preset.NETHER.settings());
    }

    public static MappedRegistry<LevelStem> defaultDimensions(long param0) {
        MappedRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        var0.register(LevelStem.NETHER, new LevelStem(() -> DEFAULT_NETHER, defaultNetherGenerator(param0)));
        var0.register(LevelStem.END, new LevelStem(() -> DEFAULT_END, defaultEndGenerator(param0)));
        return var0;
    }

    public String getFileSuffix() {
        return this.fileSuffix;
    }

    public static File getStorageFolder(ResourceKey<Level> param0, File param1) {
        if (param0 == Level.OVERWORLD) {
            return param1;
        } else if (param0 == Level.END) {
            return new File(param1, "DIM1");
        } else {
            return param0 == Level.NETHER
                ? new File(param1, "DIM-1")
                : new File(param1, "dimensions/" + param0.location().getNamespace() + "/" + param0.location().getPath());
        }
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }

    public boolean hasCeiling() {
        return this.hasCeiling;
    }

    public boolean ultraWarm() {
        return this.ultraWarm;
    }

    public boolean natural() {
        return this.natural;
    }

    public boolean shrunk() {
        return this.shrunk;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
    }

    public float timeOfDay(long param0) {
        double var0 = Mth.frac((double)this.fixedTime.orElse(param0) / 24000.0 - 0.25);
        double var1 = 0.5 - Math.cos(var0 * Math.PI) / 2.0;
        return (float)(var0 * 2.0 + var1) / 3.0F;
    }

    public int moonPhase(long param0) {
        return (int)(param0 / 24000L % 8L + 8L) % 8;
    }

    public float brightness(int param0) {
        return this.brightnessRamp[param0];
    }

    public boolean isOverworld() {
        return this.builtinKey.equals(Optional.of(OVERWORLD_LOCATION));
    }

    public boolean isNether() {
        return this.builtinKey.equals(Optional.of(NETHER_LOCATION));
    }

    public boolean isEnd() {
        return this.builtinKey.equals(Optional.of(END_LOCATION));
    }
}
