package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DimensionType {
    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
    public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
    public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
    public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.<DimensionType>create(
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
                        Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY),
                        Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height),
                        Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
                        TagKey.hashedCodec(Registry.BLOCK_REGISTRY).fieldOf("infiniburn").forGetter(param0x -> param0x.infiniburn),
                        ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter(param0x -> param0x.effectsLocation),
                        Codec.FLOAT.fieldOf("ambient_light").forGetter(param0x -> param0x.ambientLight)
                    )
                    .apply(param0, DimensionType::new)
        )
        .comapFlatMap(DimensionType::guardY, Function.identity());
    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = create(
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
        -64,
        384,
        384,
        BlockTags.INFINIBURN_OVERWORLD,
        OVERWORLD_EFFECTS,
        0.0F
    );
    protected static final DimensionType DEFAULT_NETHER = create(
        OptionalLong.of(18000L), false, true, true, false, 8.0, false, true, false, true, false, 0, 256, 128, BlockTags.INFINIBURN_NETHER, NETHER_EFFECTS, 0.1F
    );
    protected static final DimensionType DEFAULT_END = create(
        OptionalLong.of(6000L), false, false, false, false, 1.0, true, false, false, false, true, 0, 256, 256, BlockTags.INFINIBURN_END, END_EFFECTS, 0.0F
    );
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(
        Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves")
    );
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(
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
        -64,
        384,
        384,
        BlockTags.INFINIBURN_OVERWORLD,
        OVERWORLD_EFFECTS,
        0.0F
    );
    public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
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
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final TagKey<Block> infiniburn;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    private static DataResult<DimensionType> guardY(DimensionType param0) {
        if (param0.height() < 16) {
            return DataResult.error("height has to be at least 16");
        } else if (param0.minY() + param0.height() > MAX_Y + 1) {
            return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
        } else if (param0.logicalHeight() > param0.height()) {
            return DataResult.error("logical_height cannot be higher than height");
        } else if (param0.height() % 16 != 0) {
            return DataResult.error("height has to be multiple of 16");
        } else {
            return param0.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(param0);
        }
    }

    private DimensionType(
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
        int param11,
        int param12,
        TagKey<Block> param13,
        ResourceLocation param14,
        float param15
    ) {
        this(param0, param1, param2, param3, param4, param5, false, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15);
    }

    public static DimensionType create(
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
        int param12,
        int param13,
        TagKey<Block> param14,
        ResourceLocation param15,
        float param16
    ) {
        DimensionType var0 = new DimensionType(
            param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16
        );
        guardY(var0).error().ifPresent(param0x -> {
            throw new IllegalStateException(param0x.message());
        });
        return var0;
    }

    @Deprecated
    private DimensionType(
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
        int param12,
        int param13,
        TagKey<Block> param14,
        ResourceLocation param15,
        float param16
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
        this.minY = param11;
        this.height = param12;
        this.logicalHeight = param13;
        this.infiniburn = param14;
        this.effectsLocation = param15;
        this.ambientLight = param16;
        this.brightnessRamp = fillBrightnessRamp(param16);
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

    public static RegistryAccess.Writable registerBuiltin(RegistryAccess.Writable param0) {
        WritableRegistry<DimensionType> var0 = param0.ownedWritableRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        var0.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
        var0.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        var0.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
        var0.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
        return param0;
    }

    public static Registry<LevelStem> defaultDimensions(RegistryAccess param0, long param1) {
        return defaultDimensions(param0, param1, true);
    }

    public static Registry<LevelStem> defaultDimensions(RegistryAccess param0, long param1, boolean param2) {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> var2 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> var3 = param0.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NoiseGeneratorSettings> var4 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> var5 = param0.registryOrThrow(Registry.NOISE_REGISTRY);
        var0.register(
            LevelStem.NETHER,
            new LevelStem(
                var1.getOrCreateHolder(NETHER_LOCATION),
                new NoiseBasedChunkGenerator(
                    var3, var5, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(var2, param2), param1, var4.getOrCreateHolder(NoiseGeneratorSettings.NETHER)
                )
            ),
            Lifecycle.stable()
        );
        var0.register(
            LevelStem.END,
            new LevelStem(
                var1.getOrCreateHolder(END_LOCATION),
                new NoiseBasedChunkGenerator(var3, var5, new TheEndBiomeSource(var2, param1), param1, var4.getOrCreateHolder(NoiseGeneratorSettings.END))
            ),
            Lifecycle.stable()
        );
        return var0;
    }

    public static double getTeleportationScale(DimensionType param0, DimensionType param1) {
        double var0 = param0.coordinateScale();
        double var1 = param1.coordinateScale();
        return var0 / var1;
    }

    public static Path getStorageFolder(ResourceKey<Level> param0, Path param1) {
        if (param0 == Level.OVERWORLD) {
            return param1;
        } else if (param0 == Level.END) {
            return param1.resolve("DIM1");
        } else {
            return param0 == Level.NETHER
                ? param1.resolve("DIM-1")
                : param1.resolve("dimensions").resolve(param0.location().getNamespace()).resolve(param0.location().getPath());
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

    public int minY() {
        return this.minY;
    }

    public int height() {
        return this.height;
    }

    public int logicalHeight() {
        return this.logicalHeight;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
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

    public TagKey<Block> infiniburn() {
        return this.infiniburn;
    }

    public ResourceLocation effectsLocation() {
        return this.effectsLocation;
    }
}
