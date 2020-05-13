package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocationPredicate {
    public static final LocationPredicate ANY = new LocationPredicate(
        MinMaxBounds.Floats.ANY,
        MinMaxBounds.Floats.ANY,
        MinMaxBounds.Floats.ANY,
        null,
        null,
        null,
        null,
        LightPredicate.ANY,
        BlockPredicate.ANY,
        FluidPredicate.ANY
    );
    private final MinMaxBounds.Floats x;
    private final MinMaxBounds.Floats y;
    private final MinMaxBounds.Floats z;
    @Nullable
    private final Biome biome;
    @Nullable
    private final StructureFeature<?> feature;
    @Nullable
    private final DimensionType dimension;
    @Nullable
    private final Boolean smokey;
    private final LightPredicate light;
    private final BlockPredicate block;
    private final FluidPredicate fluid;

    public LocationPredicate(
        MinMaxBounds.Floats param0,
        MinMaxBounds.Floats param1,
        MinMaxBounds.Floats param2,
        @Nullable Biome param3,
        @Nullable StructureFeature<?> param4,
        @Nullable DimensionType param5,
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

    public static LocationPredicate inBiome(Biome param0) {
        return new LocationPredicate(
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
            param0,
            null,
            null,
            null,
            LightPredicate.ANY,
            BlockPredicate.ANY,
            FluidPredicate.ANY
        );
    }

    public static LocationPredicate inDimension(DimensionType param0) {
        return new LocationPredicate(
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
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
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
            MinMaxBounds.Floats.ANY,
            null,
            param0,
            null,
            null,
            LightPredicate.ANY,
            BlockPredicate.ANY,
            FluidPredicate.ANY
        );
    }

    public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
        return this.matches(param0, (float)param1, (float)param2, (float)param3);
    }

    public boolean matches(ServerLevel param0, float param1, float param2, float param3) {
        if (!this.x.matches(param1)) {
            return false;
        } else if (!this.y.matches(param2)) {
            return false;
        } else if (!this.z.matches(param3)) {
            return false;
        } else if (this.dimension != null && this.dimension != param0.dimensionType()) {
            return false;
        } else {
            BlockPos var0 = new BlockPos((double)param1, (double)param2, (double)param3);
            boolean var1 = param0.isLoaded(var0);
            if (this.biome == null || var1 && this.biome == param0.getBiome(var0)) {
                if (this.feature == null || var1 && this.feature.isInsideFeature(param0.structureFeatureManager(), var0)) {
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
                var0.addProperty("dimension", DimensionType.getName(this.dimension).toString());
            }

            if (this.feature != null) {
                var0.addProperty("feature", Feature.STRUCTURES_REGISTRY.inverse().get(this.feature));
            }

            if (this.biome != null) {
                var0.addProperty("biome", Registry.BIOME.getKey(this.biome).toString());
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
            MinMaxBounds.Floats var2 = MinMaxBounds.Floats.fromJson(var1.get("x"));
            MinMaxBounds.Floats var3 = MinMaxBounds.Floats.fromJson(var1.get("y"));
            MinMaxBounds.Floats var4 = MinMaxBounds.Floats.fromJson(var1.get("z"));
            DimensionType var5 = var0.has("dimension") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(var0, "dimension"))) : null;
            StructureFeature<?> var6 = var0.has("feature") ? Feature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(var0, "feature")) : null;
            Biome var7 = null;
            if (var0.has("biome")) {
                ResourceLocation var8 = new ResourceLocation(GsonHelper.getAsString(var0, "biome"));
                var7 = Registry.BIOME.getOptional(var8).orElseThrow(() -> new JsonSyntaxException("Unknown biome '" + var8 + "'"));
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
        private MinMaxBounds.Floats x = MinMaxBounds.Floats.ANY;
        private MinMaxBounds.Floats y = MinMaxBounds.Floats.ANY;
        private MinMaxBounds.Floats z = MinMaxBounds.Floats.ANY;
        @Nullable
        private Biome biome;
        @Nullable
        private StructureFeature<?> feature;
        @Nullable
        private DimensionType dimension;
        @Nullable
        private Boolean smokey;
        private LightPredicate light = LightPredicate.ANY;
        private BlockPredicate block = BlockPredicate.ANY;
        private FluidPredicate fluid = FluidPredicate.ANY;

        public static LocationPredicate.Builder location() {
            return new LocationPredicate.Builder();
        }

        public LocationPredicate.Builder setBiome(@Nullable Biome param0) {
            this.biome = param0;
            return this;
        }

        public LocationPredicate.Builder setBlock(BlockPredicate param0) {
            this.block = param0;
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
