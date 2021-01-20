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
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidPredicate {
    public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePropertiesPredicate.ANY);
    @Nullable
    private final Tag<Fluid> tag;
    @Nullable
    private final Fluid fluid;
    private final StatePropertiesPredicate properties;

    public FluidPredicate(@Nullable Tag<Fluid> param0, @Nullable Fluid param1, StatePropertiesPredicate param2) {
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
            Fluid var1 = var0.getType();
            if (this.tag != null && !var1.is(this.tag)) {
                return false;
            } else if (this.fluid != null && var1 != this.fluid) {
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

            Tag<Fluid> var3 = null;
            if (var0.has("tag")) {
                ResourceLocation var4 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                var3 = SerializationTags.getInstance()
                    .getTagOrThrow(Registry.FLUID_REGISTRY, var4, param0x -> new JsonSyntaxException("Unknown fluid tag '" + param0x + "'"));
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
                var0.addProperty(
                    "tag",
                    SerializationTags.getInstance()
                        .getIdOrThrow(Registry.FLUID_REGISTRY, this.tag, () -> new IllegalStateException("Unknown fluid tag"))
                        .toString()
                );
            }

            var0.add("state", this.properties.serializeToJson());
            return var0;
        }
    }
}
