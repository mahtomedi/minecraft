package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Variant implements ModelState {
    private final ResourceLocation modelLocation;
    private final Transformation rotation;
    private final boolean uvLock;
    private final int weight;

    public Variant(ResourceLocation param0, Transformation param1, boolean param2, int param3) {
        this.modelLocation = param0;
        this.rotation = param1;
        this.uvLock = param2;
        this.weight = param3;
    }

    public ResourceLocation getModelLocation() {
        return this.modelLocation;
    }

    @Override
    public Transformation getRotation() {
        return this.rotation;
    }

    @Override
    public boolean isUvLocked() {
        return this.uvLock;
    }

    public int getWeight() {
        return this.weight;
    }

    @Override
    public String toString() {
        return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Variant)) {
            return false;
        } else {
            Variant var0 = (Variant)param0;
            return this.modelLocation.equals(var0.modelLocation)
                && Objects.equals(this.rotation, var0.rotation)
                && this.uvLock == var0.uvLock
                && this.weight == var0.weight;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.modelLocation.hashCode();
        var0 = 31 * var0 + this.rotation.hashCode();
        var0 = 31 * var0 + Boolean.valueOf(this.uvLock).hashCode();
        return 31 * var0 + this.weight;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<Variant> {
        @VisibleForTesting
        static final boolean DEFAULT_UVLOCK = false;
        @VisibleForTesting
        static final int DEFAULT_WEIGHT = 1;
        @VisibleForTesting
        static final int DEFAULT_X_ROTATION = 0;
        @VisibleForTesting
        static final int DEFAULT_Y_ROTATION = 0;

        public Variant deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            ResourceLocation var1 = this.getModel(var0);
            BlockModelRotation var2 = this.getBlockRotation(var0);
            boolean var3 = this.getUvLock(var0);
            int var4 = this.getWeight(var0);
            return new Variant(var1, var2.getRotation(), var3, var4);
        }

        private boolean getUvLock(JsonObject param0) {
            return GsonHelper.getAsBoolean(param0, "uvlock", false);
        }

        protected BlockModelRotation getBlockRotation(JsonObject param0) {
            int var0 = GsonHelper.getAsInt(param0, "x", 0);
            int var1 = GsonHelper.getAsInt(param0, "y", 0);
            BlockModelRotation var2 = BlockModelRotation.by(var0, var1);
            if (var2 == null) {
                throw new JsonParseException("Invalid BlockModelRotation x: " + var0 + ", y: " + var1);
            } else {
                return var2;
            }
        }

        protected ResourceLocation getModel(JsonObject param0) {
            return new ResourceLocation(GsonHelper.getAsString(param0, "model"));
        }

        protected int getWeight(JsonObject param0) {
            int var0 = GsonHelper.getAsInt(param0, "weight", 1);
            if (var0 < 1) {
                throw new JsonParseException("Invalid weight " + var0 + " found, expected integer >= 1");
            } else {
                return var0;
            }
        }
    }
}
