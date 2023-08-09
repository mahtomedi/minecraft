package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(
    Optional<LocationPredicate.PositionPredicate> position,
    Optional<ResourceKey<Biome>> biome,
    Optional<ResourceKey<Structure>> structure,
    Optional<ResourceKey<Level>> dimension,
    Optional<Boolean> smokey,
    Optional<LightPredicate> light,
    Optional<BlockPredicate> block,
    Optional<FluidPredicate> fluid
) {
    public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(LocationPredicate.PositionPredicate.CODEC, "position").forGetter(LocationPredicate::position),
                    ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.BIOME), "biome").forGetter(LocationPredicate::biome),
                    ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.STRUCTURE), "structure").forGetter(LocationPredicate::structure),
                    ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DIMENSION), "dimension").forGetter(LocationPredicate::dimension),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "smokey").forGetter(LocationPredicate::smokey),
                    ExtraCodecs.strictOptionalField(LightPredicate.CODEC, "light").forGetter(LocationPredicate::light),
                    ExtraCodecs.strictOptionalField(BlockPredicate.CODEC, "block").forGetter(LocationPredicate::block),
                    ExtraCodecs.strictOptionalField(FluidPredicate.CODEC, "fluid").forGetter(LocationPredicate::fluid)
                )
                .apply(param0, LocationPredicate::new)
    );

    static Optional<LocationPredicate> of(
        Optional<LocationPredicate.PositionPredicate> param0,
        Optional<ResourceKey<Biome>> param1,
        Optional<ResourceKey<Structure>> param2,
        Optional<ResourceKey<Level>> param3,
        Optional<Boolean> param4,
        Optional<LightPredicate> param5,
        Optional<BlockPredicate> param6,
        Optional<FluidPredicate> param7
    ) {
        return param0.isEmpty()
                && param1.isEmpty()
                && param2.isEmpty()
                && param3.isEmpty()
                && param4.isEmpty()
                && param5.isEmpty()
                && param6.isEmpty()
                && param7.isEmpty()
            ? Optional.empty()
            : Optional.of(new LocationPredicate(param0, param1, param2, param3, param4, param5, param6, param7));
    }

    public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
        if (this.position.isPresent() && !this.position.get().matches(param1, param2, param3)) {
            return false;
        } else if (this.dimension.isPresent() && this.dimension.get() != param0.dimension()) {
            return false;
        } else {
            BlockPos var0 = BlockPos.containing(param1, param2, param3);
            boolean var1 = param0.isLoaded(var0);
            if (!this.biome.isPresent() || var1 && param0.getBiome(var0).is(this.biome.get())) {
                if (!this.structure.isPresent() || var1 && param0.structureManager().getStructureWithPieceAt(var0, this.structure.get()).isValid()) {
                    if (!this.smokey.isPresent() || var1 && this.smokey.get() == CampfireBlock.isSmokeyPos(param0, var0)) {
                        if (this.light.isPresent() && !this.light.get().matches(param0, var0)) {
                            return false;
                        } else if (this.block.isPresent() && !this.block.get().matches(param0, var0)) {
                            return false;
                        } else {
                            return !this.fluid.isPresent() || this.fluid.get().matches(param0, var0);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public JsonElement serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static Optional<LocationPredicate> fromJson(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull()
            ? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new))
            : Optional.empty();
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        private Optional<ResourceKey<Biome>> biome = Optional.empty();
        private Optional<ResourceKey<Structure>> structure = Optional.empty();
        private Optional<ResourceKey<Level>> dimension = Optional.empty();
        private Optional<Boolean> smokey = Optional.empty();
        private Optional<LightPredicate> light = Optional.empty();
        private Optional<BlockPredicate> block = Optional.empty();
        private Optional<FluidPredicate> fluid = Optional.empty();

        public static LocationPredicate.Builder location() {
            return new LocationPredicate.Builder();
        }

        public static LocationPredicate.Builder inBiome(ResourceKey<Biome> param0) {
            return location().setBiome(param0);
        }

        public static LocationPredicate.Builder inDimension(ResourceKey<Level> param0) {
            return location().setDimension(param0);
        }

        public static LocationPredicate.Builder inStructure(ResourceKey<Structure> param0) {
            return location().setStructure(param0);
        }

        public static LocationPredicate.Builder atYLocation(MinMaxBounds.Doubles param0) {
            return location().setY(param0);
        }

        public LocationPredicate.Builder setX(MinMaxBounds.Doubles param0) {
            this.x = param0;
            return this;
        }

        public LocationPredicate.Builder setY(MinMaxBounds.Doubles param0) {
            this.y = param0;
            return this;
        }

        public LocationPredicate.Builder setZ(MinMaxBounds.Doubles param0) {
            this.z = param0;
            return this;
        }

        public LocationPredicate.Builder setBiome(ResourceKey<Biome> param0) {
            this.biome = Optional.of(param0);
            return this;
        }

        public LocationPredicate.Builder setStructure(ResourceKey<Structure> param0) {
            this.structure = Optional.of(param0);
            return this;
        }

        public LocationPredicate.Builder setDimension(ResourceKey<Level> param0) {
            this.dimension = Optional.of(param0);
            return this;
        }

        public LocationPredicate.Builder setLight(LightPredicate.Builder param0) {
            this.light = param0.build();
            return this;
        }

        public LocationPredicate.Builder setBlock(BlockPredicate.Builder param0) {
            this.block = param0.build();
            return this;
        }

        public LocationPredicate.Builder setFluid(FluidPredicate.Builder param0) {
            this.fluid = param0.build();
            return this;
        }

        public LocationPredicate.Builder setSmokey(boolean param0) {
            this.smokey = Optional.of(param0);
            return this;
        }

        public Optional<LocationPredicate> build() {
            return LocationPredicate.of(
                LocationPredicate.PositionPredicate.of(this.x, this.y, this.z),
                this.biome,
                this.structure,
                this.dimension,
                this.smokey,
                this.light,
                this.block,
                this.fluid
            );
        }
    }

    static record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
        public static final Codec<LocationPredicate.PositionPredicate> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "x", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::x),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "y", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::y),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "z", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::z)
                    )
                    .apply(param0, LocationPredicate.PositionPredicate::new)
        );

        static Optional<LocationPredicate.PositionPredicate> of(MinMaxBounds.Doubles param0, MinMaxBounds.Doubles param1, MinMaxBounds.Doubles param2) {
            return param0.isAny() && param1.isAny() && param2.isAny()
                ? Optional.empty()
                : Optional.of(new LocationPredicate.PositionPredicate(param0, param1, param2));
        }

        public boolean matches(double param0, double param1, double param2) {
            return this.x.matches(param0) && this.y.matches(param1) && this.z.matches(param2);
        }
    }
}
