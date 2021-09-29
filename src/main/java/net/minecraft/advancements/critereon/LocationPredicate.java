package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocationPredicate {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LocationPredicate ANY = new LocationPredicate(
        MinMaxBounds.Doubles.ANY,
        MinMaxBounds.Doubles.ANY,
        MinMaxBounds.Doubles.ANY,
        null,
        null,
        null,
        null,
        LightPredicate.ANY,
        BlockPredicate.ANY,
        FluidPredicate.ANY
    );
    private final MinMaxBounds.Doubles x;
    private final MinMaxBounds.Doubles y;
    private final MinMaxBounds.Doubles z;
    @Nullable
    private final ResourceKey<Biome> biome;
    @Nullable
    private final StructureFeature<?> feature;
    @Nullable
    private final ResourceKey<Level> dimension;
    @Nullable
    private final Boolean smokey;
    private final LightPredicate light;
    private final BlockPredicate block;
    private final FluidPredicate fluid;

    public LocationPredicate(
        MinMaxBounds.Doubles param0,
        MinMaxBounds.Doubles param1,
        MinMaxBounds.Doubles param2,
        @Nullable ResourceKey<Biome> param3,
        @Nullable StructureFeature<?> param4,
        @Nullable ResourceKey<Level> param5,
        @Nullable Boolean param6,
        LightPredicate param7,
        BlockPredicate param8,
        FluidPredicate param9
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.biome = param3;
        this.feature = param4;
        this.dimension = param5;
        this.smokey = param6;
        this.light = param7;
        this.block = param8;
        this.fluid = param9;
    }

    public static LocationPredicate inBiome(ResourceKey<Biome> param0) {
        return new LocationPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            param0,
            null,
            null,
            null,
            LightPredicate.ANY,
            BlockPredicate.ANY,
            FluidPredicate.ANY
        );
    }

    public static LocationPredicate inDimension(ResourceKey<Level> param0) {
        return new LocationPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            null,
            null,
            param0,
            null,
            LightPredicate.ANY,
            BlockPredicate.ANY,
            FluidPredicate.ANY
        );
    }

    public static LocationPredicate inFeature(StructureFeature<?> param0) {
        return new LocationPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            null,
            param0,
            null,
            null,
            LightPredicate.ANY,
            BlockPredicate.ANY,
            FluidPredicate.ANY
        );
    }

    public static LocationPredicate atYLocation(MinMaxBounds.Doubles param0) {
        return new LocationPredicate(
            MinMaxBounds.Doubles.ANY, param0, MinMaxBounds.Doubles.ANY, null, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY
        );
    }

    public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
        if (!this.x.matches(param1)) {
            return false;
        } else if (!this.y.matches(param2)) {
            return false;
        } else if (!this.z.matches(param3)) {
            return false;
        } else if (this.dimension != null && this.dimension != param0.dimension()) {
            return false;
        } else {
            BlockPos var0 = new BlockPos(param1, param2, param3);
            boolean var1 = param0.isLoaded(var0);
            Optional<ResourceKey<Biome>> var2 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(param0.getBiome(var0));
            if (!var2.isPresent()) {
                return false;
            } else if (this.biome == null || var1 && this.biome == var2.get()) {
                if (this.feature == null || var1 && param0.structureFeatureManager().getStructureWithPieceAt(var0, this.feature).isValid()) {
                    if (this.smokey == null || var1 && this.smokey == CampfireBlock.isSmokeyPos(param0, var0)) {
                        if (!this.light.matches(param0, var0)) {
                            return false;
                        } else if (!this.block.matches(param0, var0)) {
                            return false;
                        } else {
                            return this.fluid.matches(param0, var0);
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
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
                JsonObject var1 = new JsonObject();
                var1.add("x", this.x.serializeToJson());
                var1.add("y", this.y.serializeToJson());
                var1.add("z", this.z.serializeToJson());
                var0.add("position", var1);
            }

            if (this.dimension != null) {
                Level.RESOURCE_KEY_CODEC
                    .encodeStart(JsonOps.INSTANCE, this.dimension)
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(param1 -> var0.add("dimension", param1));
            }

            if (this.feature != null) {
                var0.addProperty("feature", this.feature.getFeatureName());
            }

            if (this.biome != null) {
                var0.addProperty("biome", this.biome.location().toString());
            }

            if (this.smokey != null) {
                var0.addProperty("smokey", this.smokey);
            }

            var0.add("light", this.light.serializeToJson());
            var0.add("block", this.block.serializeToJson());
            var0.add("fluid", this.fluid.serializeToJson());
            return var0;
        }
    }

    public static LocationPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "location");
            JsonObject var1 = GsonHelper.getAsJsonObject(var0, "position", new JsonObject());
            MinMaxBounds.Doubles var2 = MinMaxBounds.Doubles.fromJson(var1.get("x"));
            MinMaxBounds.Doubles var3 = MinMaxBounds.Doubles.fromJson(var1.get("y"));
            MinMaxBounds.Doubles var4 = MinMaxBounds.Doubles.fromJson(var1.get("z"));
            ResourceKey<Level> var5 = var0.has("dimension")
                ? ResourceLocation.CODEC
                    .parse(JsonOps.INSTANCE, var0.get("dimension"))
                    .resultOrPartial(LOGGER::error)
                    .map(param0x -> ResourceKey.create(Registry.DIMENSION_REGISTRY, param0x))
                    .orElse(null)
                : null;
            StructureFeature<?> var6 = var0.has("feature") ? StructureFeature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(var0, "feature")) : null;
            ResourceKey<Biome> var7 = null;
            if (var0.has("biome")) {
                ResourceLocation var8 = new ResourceLocation(GsonHelper.getAsString(var0, "biome"));
                var7 = ResourceKey.create(Registry.BIOME_REGISTRY, var8);
            }

            Boolean var9 = var0.has("smokey") ? var0.get("smokey").getAsBoolean() : null;
            LightPredicate var10 = LightPredicate.fromJson(var0.get("light"));
            BlockPredicate var11 = BlockPredicate.fromJson(var0.get("block"));
            FluidPredicate var12 = FluidPredicate.fromJson(var0.get("fluid"));
            return new LocationPredicate(var2, var3, var4, var7, var6, var5, var9, var10, var11, var12);
        } else {
            return ANY;
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        @Nullable
        private ResourceKey<Biome> biome;
        @Nullable
        private StructureFeature<?> feature;
        @Nullable
        private ResourceKey<Level> dimension;
        @Nullable
        private Boolean smokey;
        private LightPredicate light = LightPredicate.ANY;
        private BlockPredicate block = BlockPredicate.ANY;
        private FluidPredicate fluid = FluidPredicate.ANY;

        public static LocationPredicate.Builder location() {
            return new LocationPredicate.Builder();
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

        public LocationPredicate.Builder setBiome(@Nullable ResourceKey<Biome> param0) {
            this.biome = param0;
            return this;
        }

        public LocationPredicate.Builder setFeature(@Nullable StructureFeature<?> param0) {
            this.feature = param0;
            return this;
        }

        public LocationPredicate.Builder setDimension(@Nullable ResourceKey<Level> param0) {
            this.dimension = param0;
            return this;
        }

        public LocationPredicate.Builder setLight(LightPredicate param0) {
            this.light = param0;
            return this;
        }

        public LocationPredicate.Builder setBlock(BlockPredicate param0) {
            this.block = param0;
            return this;
        }

        public LocationPredicate.Builder setFluid(FluidPredicate param0) {
            this.fluid = param0;
            return this;
        }

        public LocationPredicate.Builder setSmokey(Boolean param0) {
            this.smokey = param0;
            return this;
        }

        public LocationPredicate build() {
            return new LocationPredicate(this.x, this.y, this.z, this.biome, this.feature, this.dimension, this.smokey, this.light, this.block, this.fluid);
        }
    }
}
