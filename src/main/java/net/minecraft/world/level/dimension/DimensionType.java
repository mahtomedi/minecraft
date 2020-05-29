package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DimensionType {
    public static final Codec<ResourceKey<DimensionType>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC
        .xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location);
    private static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(
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
    private static final LinkedHashSet<ResourceKey<Level>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(Level.OVERWORLD, Level.NETHER, Level.END));
    private static final Map<ResourceKey<DimensionType>, DimensionType> BUILTIN = ImmutableMap.of(
        OVERWORLD_LOCATION, makeDefaultOverworld(), NETHER_LOCATION, makeDefaultNether(), END_LOCATION, makeDefaultEnd()
    );
    private static final Codec<DimensionType> BUILTIN_CODEC = RESOURCE_KEY_CODEC.flatXmap(
            param0 -> Optional.ofNullable(BUILTIN.get(param0))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error("Unknown builtin dimension: " + param0)),
            param0 -> param0.builtinKey.map(DataResult::success).orElseGet(() -> DataResult.error("Unknown builtin dimension: " + param0))
        )
        .stable();
    public static final Codec<DimensionType> CODEC = Codec.either(BUILTIN_CODEC, DIRECT_CODEC)
        .flatXmap(
            param0 -> param0.map(param0x -> DataResult.success(param0x, Lifecycle.stable()), DataResult::success),
            param0 -> param0.builtinKey.isPresent() ? DataResult.success(Either.left(param0), Lifecycle.stable()) : DataResult.success(Either.right(param0))
        );
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

    public static DimensionType makeDefaultOverworld() {
        return new DimensionType(
            "",
            OptionalLong.empty(),
            true,
            false,
            false,
            true,
            false,
            false,
            FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
            Optional.of(OVERWORLD_LOCATION),
            0.0F
        );
    }

    private static DimensionType makeDefaultNether() {
        return new DimensionType(
            "_nether", OptionalLong.of(18000L), false, true, true, false, true, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(NETHER_LOCATION), 0.1F
        );
    }

    private static DimensionType makeDefaultEnd() {
        return new DimensionType(
            "_end", OptionalLong.of(6000L), false, false, false, false, false, true, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(END_LOCATION), 0.0F
        );
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

    @OnlyIn(Dist.CLIENT)
    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder param0) {
        param0.registerDimension(OVERWORLD_LOCATION, makeDefaultOverworld());
        param0.registerDimension(NETHER_LOCATION, makeDefaultNether());
        param0.registerDimension(END_LOCATION, makeDefaultEnd());
        return param0;
    }

    private static ChunkGenerator defaultEndGenerator(long param0) {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(param0), param0, NoiseGeneratorSettings.Preset.END.settings());
    }

    private static ChunkGenerator defaultNetherGenerator(long param0) {
        return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(param0), param0, NoiseGeneratorSettings.Preset.NETHER.settings());
    }

    public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> defaultDimensions(long param0) {
        LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var0 = Maps.newLinkedHashMap();
        var0.put(Level.NETHER, Pair.of(makeDefaultNether(), defaultNetherGenerator(param0)));
        var0.put(Level.END, Pair.of(makeDefaultEnd(), defaultEndGenerator(param0)));
        return var0;
    }

    public static boolean stable(long param0, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> param1) {
        List<Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>> var0 = Lists.newArrayList(param1.entrySet());
        if (var0.size() != 3) {
            return false;
        } else {
            Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var1 = var0.get(0);
            Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var2 = var0.get(1);
            Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var3 = var0.get(2);
            if (var1.getKey() != Level.OVERWORLD || var2.getKey() != Level.NETHER || var3.getKey() != Level.END) {
                return false;
            } else if (!var1.getValue().getFirst().isOverworld() || !var2.getValue().getFirst().isNether() || !var3.getValue().getFirst().isEnd()) {
                return false;
            } else if (var2.getValue().getSecond() instanceof NoiseBasedChunkGenerator && var3.getValue().getSecond() instanceof NoiseBasedChunkGenerator) {
                NoiseBasedChunkGenerator var4 = (NoiseBasedChunkGenerator)var2.getValue().getSecond();
                NoiseBasedChunkGenerator var5 = (NoiseBasedChunkGenerator)var3.getValue().getSecond();
                if (!var4.stable(param0, NoiseGeneratorSettings.Preset.NETHER)) {
                    return false;
                } else if (!var5.stable(param0, NoiseGeneratorSettings.Preset.END)) {
                    return false;
                } else if (!(var4.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
                    return false;
                } else {
                    MultiNoiseBiomeSource var6 = (MultiNoiseBiomeSource)var4.getBiomeSource();
                    if (!var6.stable(param0)) {
                        return false;
                    } else if (!(var5.getBiomeSource() instanceof TheEndBiomeSource)) {
                        return false;
                    } else {
                        TheEndBiomeSource var7 = (TheEndBiomeSource)var5.getBiomeSource();
                        return var7.stable(param0);
                    }
                }
            } else {
                return false;
            }
        }
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

    public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> sortMap(
        Map<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> param0
    ) {
        LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var0 = Maps.newLinkedHashMap();

        for(ResourceKey<Level> var1 : BUILTIN_ORDER) {
            Pair<DimensionType, ChunkGenerator> var2 = param0.get(var1);
            if (var2 != null) {
                var0.put(var1, var2);
            }
        }

        for(Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> var3 : param0.entrySet()) {
            if (!BUILTIN_ORDER.contains(var3.getKey())) {
                var0.put(var3.getKey(), var3.getValue());
            }
        }

        return var0;
    }
}
