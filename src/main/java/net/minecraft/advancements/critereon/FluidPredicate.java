package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidPredicate {
    public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePropertiesPredicate.ANY);
    @Nullable
    private final TagKey<Fluid> tag;
    @Nullable
    private final Fluid fluid;
    private final StatePropertiesPredicate properties;

    public FluidPredicate(@Nullable TagKey<Fluid> param0, @Nullable Fluid param1, StatePropertiesPredicate param2) {
        this.tag = param0;
        this.fluid = param1;
        this.properties = param2;
    }

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (this == ANY) {
            return true;
        } else if (!param0.isLoaded(param1)) {
            return false;
        } else {
            FluidState var0 = param0.getFluidState(param1);
            if (this.tag != null && !var0.is(this.tag)) {
                return false;
            } else if (this.fluid != null && !var0.is(this.fluid)) {
                return false;
            } else {
                return this.properties.matches(var0);
            }
        }
    }

    public static FluidPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "fluid");
            Fluid var1 = null;
            if (var0.has("fluid")) {
                ResourceLocation var2 = new ResourceLocation(GsonHelper.getAsString(var0, "fluid"));
                var1 = Registry.FLUID.get(var2);
            }

            TagKey<Fluid> var3 = null;
            if (var0.has("tag")) {
                ResourceLocation var4 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                var3 = TagKey.create(Registry.FLUID_REGISTRY, var4);
            }

            StatePropertiesPredicate var5 = StatePropertiesPredicate.fromJson(var0.get("state"));
            return new FluidPredicate(var3, var1, var5);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.fluid != null) {
                var0.addProperty("fluid", Registry.FLUID.getKey(this.fluid).toString());
            }

            if (this.tag != null) {
                var0.addProperty("tag", this.tag.location().toString());
            }

            var0.add("state", this.properties.serializeToJson());
            return var0;
        }
    }

    public static class Builder {
        @Nullable
        private Fluid fluid;
        @Nullable
        private TagKey<Fluid> fluids;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

        private Builder() {
        }

        public static FluidPredicate.Builder fluid() {
            return new FluidPredicate.Builder();
        }

        public FluidPredicate.Builder of(Fluid param0) {
            this.fluid = param0;
            return this;
        }

        public FluidPredicate.Builder of(TagKey<Fluid> param0) {
            this.fluids = param0;
            return this;
        }

        public FluidPredicate.Builder setProperties(StatePropertiesPredicate param0) {
            this.properties = param0;
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.fluid, this.properties);
        }
    }
}
