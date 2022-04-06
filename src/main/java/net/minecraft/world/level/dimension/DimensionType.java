package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record DimensionType(
    OptionalLong fixedTime,
    boolean hasSkyLight,
    boolean hasCeiling,
    boolean ultraWarm,
    boolean natural,
    double coordinateScale,
    boolean piglinSafe,
    boolean bedWorks,
    boolean respawnAnchorWorks,
    boolean hasRaids,
    int minY,
    int height,
    int logicalHeight,
    TagKey<Block> infiniburn,
    ResourceLocation effectsLocation,
    float ambientLight
) {
    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
    public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
    public static final Codec<DimensionType> DIRECT_CODEC = ExtraCodecs.catchDecoderException(
        RecordCodecBuilder.create(
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
                        ResourceLocation.CODEC.fieldOf("effects").orElse(BuiltinDimensionTypes.OVERWORLD_EFFECTS).forGetter(param0x -> param0x.effectsLocation),
                        Codec.FLOAT.fieldOf("ambient_light").forGetter(param0x -> param0x.ambientLight)
                    )
                    .apply(param0, DimensionType::new)
        )
    );
    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);

    public DimensionType(
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
        if (param11 < 16) {
            throw new IllegalStateException("height has to be at least 16");
        } else if (param10 + param11 > MAX_Y + 1) {
            throw new IllegalStateException("min_y + height cannot be higher than: " + (MAX_Y + 1));
        } else if (param12 > param11) {
            throw new IllegalStateException("logical_height cannot be higher than height");
        } else if (param11 % 16 != 0) {
            throw new IllegalStateException("height has to be multiple of 16");
        } else if (param10 % 16 != 0) {
            throw new IllegalStateException("min_y has to be a multiple of 16");
        } else {
            this.fixedTime = param0;
            this.hasSkyLight = param1;
            this.hasCeiling = param2;
            this.ultraWarm = param3;
            this.natural = param4;
            this.coordinateScale = param5;
            this.piglinSafe = param6;
            this.bedWorks = param7;
            this.respawnAnchorWorks = param8;
            this.hasRaids = param9;
            this.minY = param10;
            this.height = param11;
            this.logicalHeight = param12;
            this.infiniburn = param13;
            this.effectsLocation = param14;
            this.ambientLight = param15;
        }
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
}
