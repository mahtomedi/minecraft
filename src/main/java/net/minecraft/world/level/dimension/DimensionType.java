package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
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
                    Codec.doubleRange(1.0E-5F, 3.0E7).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale),
                    Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe),
                    Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks),
                    Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks),
                    Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids),
                    Codec.intRange(0, 256).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
                    ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(param0x -> param0x.infiniburn),
                    Codec.FLOAT.fieldOf("ambient_light").forGetter(param0x -> param0x.ambientLight)
                )
                .apply(param0, DimensionType::new)
    );
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = new DimensionType(
        OptionalLong.empty(),
        true,
        false,
        false,
        true,
        1.0,
        false,
        false,
        true,
        false,
        true,
        256,
        FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
        BlockTags.INFINIBURN_OVERWORLD.getName(),
        0.0F
    );
    protected static final DimensionType DEFAULT_NETHER = new DimensionType(
        OptionalLong.of(18000L),
        false,
        true,
        true,
        false,
        8.0,
        false,
        true,
        false,
        true,
        false,
        128,
        FuzzyOffsetBiomeZoomer.INSTANCE,
        BlockTags.INFINIBURN_NETHER.getName(),
        0.1F
    );
    protected static final DimensionType DEFAULT_END = new DimensionType(
        OptionalLong.of(6000L),
        false,
        false,
        false,
        false,
        1.0,
        true,
        false,
        false,
        false,
        true,
        256,
        FuzzyOffsetBiomeZoomer.INSTANCE,
        BlockTags.INFINIBURN_END.getName(),
        0.0F
    );
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(
        Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves")
    );
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = new DimensionType(
        OptionalLong.empty(),
        true,
        true,
        false,
        true,
        1.0,
        false,
        false,
        true,
        false,
        true,
        256,
        FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
        BlockTags.INFINIBURN_OVERWORLD.getName(),
        0.0F
    );
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int logicalHeight;
    private final BiomeZoomer biomeZoomer;
    private final ResourceLocation infiniburn;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    protected DimensionType(
        OptionalLong param0,
        boolean param1,
        boolean param2,
        boolean param3,
        boolean param4,
        double param5,
        boolean param6,
        boolean param7,
        boolean param8,
        boolean param9,
        int param10,
        ResourceLocation param11,
        float param12
    ) {
        this(param0, param1, param2, param3, param4, param5, false, param6, param7, param8, param9, param10, FuzzyOffsetBiomeZoomer.INSTANCE, param11, param12);
    }

    protected DimensionType(
        OptionalLong param0,
        boolean param1,
        boolean param2,
        boolean param3,
        boolean param4,
        double param5,
        boolean param6,
        boolean param7,
        boolean param8,
        boolean param9,
        boolean param10,
        int param11,
        BiomeZoomer param12,
        ResourceLocation param13,
        float param14
    ) {
        this.fixedTime = param0;
        this.hasSkylight = param1;
        this.hasCeiling = param2;
        this.ultraWarm = param3;
        this.natural = param4;
        this.coordinateScale = param5;
        this.createDragonFight = param6;
        this.piglinSafe = param7;
        this.bedWorks = param8;
        this.respawnAnchorWorks = param9;
        this.hasRaids = param10;
        this.logicalHeight = param11;
        this.biomeZoomer = param12;
        this.infiniburn = param13;
        this.ambientLight = param14;
        this.brightnessRamp = fillBrightnessRamp(param14);
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
        Optional<Number> var0 = param0.asNumber().result();
        if (var0.isPresent()) {
            int var1 = var0.get().intValue();
            if (var1 == -1) {
                return DataResult.success(Level.NETHER);
            }

            if (var1 == 0) {
                return DataResult.success(Level.OVERWORLD);
            }

            if (var1 == 1) {
                return DataResult.success(Level.END);
            }
        }

        return Level.RESOURCE_KEY_CODEC.parse(param0);
    }

    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder param0) {
        WritableRegistry<DimensionType> var0 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        var0.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
        var0.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        var0.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
        var0.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
        return param0;
    }

    private static ChunkGenerator defaultEndGenerator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(param0, param2), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.END));
    }

    private static ChunkGenerator defaultNetherGenerator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
        return new NoiseBasedChunkGenerator(
            MultiNoiseBiomeSource.Preset.NETHER.biomeSource(param0, param2), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.NETHER)
        );
    }

    public static MappedRegistry<LevelStem> defaultDimensions(
        Registry<DimensionType> param0, Registry<Biome> param1, Registry<NoiseGeneratorSettings> param2, long param3
    ) {
        MappedRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        var0.register(
            LevelStem.NETHER, new LevelStem(() -> param0.getOrThrow(NETHER_LOCATION), defaultNetherGenerator(param1, param2, param3)), Lifecycle.stable()
        );
        var0.register(LevelStem.END, new LevelStem(() -> param0.getOrThrow(END_LOCATION), defaultEndGenerator(param1, param2, param3)), Lifecycle.stable());
        return var0;
    }

    public static double getTeleportationScale(DimensionType param0, DimensionType param1) {
        double var0 = param0.coordinateScale();
        double var1 = param1.coordinateScale();
        return var0 / var1;
    }

    @Deprecated
    public String getFileSuffix() {
        return this.equalTo(DEFAULT_END) ? "_end" : "";
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

    public double coordinateScale() {
        return this.coordinateScale;
    }

    public boolean piglinSafe() {
        return this.piglinSafe;
    }

    public boolean bedWorks() {
        return this.bedWorks;
    }

    public boolean respawnAnchorWorks() {
        return this.respawnAnchorWorks;
    }

    public boolean hasRaids() {
        return this.hasRaids;
    }

    public int logicalHeight() {
        return this.logicalHeight;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
    }

    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
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

    public Tag<Block> infiniburn() {
        Tag<Block> var0 = BlockTags.getAllTags().getTag(this.infiniburn);
        return (Tag<Block>)(var0 != null ? var0 : BlockTags.INFINIBURN_OVERWORLD);
    }

    public boolean equalTo(DimensionType param0) {
        if (this == param0) {
            return true;
        } else {
            return this.hasSkylight == param0.hasSkylight
                && this.hasCeiling == param0.hasCeiling
                && this.ultraWarm == param0.ultraWarm
                && this.natural == param0.natural
                && this.coordinateScale == param0.coordinateScale
                && this.createDragonFight == param0.createDragonFight
                && this.piglinSafe == param0.piglinSafe
                && this.bedWorks == param0.bedWorks
                && this.respawnAnchorWorks == param0.respawnAnchorWorks
                && this.hasRaids == param0.hasRaids
                && this.logicalHeight == param0.logicalHeight
                && Float.compare(param0.ambientLight, this.ambientLight) == 0
                && this.fixedTime.equals(param0.fixedTime)
                && this.biomeZoomer.equals(param0.biomeZoomer)
                && this.infiniburn.equals(param0.infiniburn);
        }
    }
}
