package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Vector3f;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockElement {
    public final Vector3f from;
    public final Vector3f to;
    public final Map<Direction, BlockElementFace> faces;
    public final BlockElementRotation rotation;
    public final boolean shade;

    public BlockElement(Vector3f param0, Vector3f param1, Map<Direction, BlockElementFace> param2, @Nullable BlockElementRotation param3, boolean param4) {
        this.from = param0;
        this.to = param1;
        this.faces = param2;
        this.rotation = param3;
        this.shade = param4;
        this.fillUvs();
    }

    private void fillUvs() {
        for(Entry<Direction, BlockElementFace> var0 : this.faces.entrySet()) {
            float[] var1 = this.uvsByFace(var0.getKey());
            var0.getValue().uv.setMissingUv(var1);
        }

    }

    private float[] uvsByFace(Direction param0) {
        switch(param0) {
            case DOWN:
                return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
            case UP:
                return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
            case NORTH:
            default:
                return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
            case SOUTH:
                return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
            case WEST:
                return new float[]{this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
            case EAST:
                return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockElement> {
        protected Deserializer() {
        }

        public BlockElement deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            Vector3f var1 = this.getFrom(var0);
            Vector3f var2 = this.getTo(var0);
            BlockElementRotation var3 = this.getRotation(var0);
            Map<Direction, BlockElementFace> var4 = this.getFaces(param2, var0);
            if (var0.has("shade") && !GsonHelper.isBooleanValue(var0, "shade")) {
                throw new JsonParseException("Expected shade to be a Boolean");
            } else {
                boolean var5 = GsonHelper.getAsBoolean(var0, "shade", true);
                return new BlockElement(var1, var2, var4, var3, var5);
            }
        }

        @Nullable
        private BlockElementRotation getRotation(JsonObject param0) {
            BlockElementRotation var0 = null;
            if (param0.has("rotation")) {
                JsonObject var1 = GsonHelper.getAsJsonObject(param0, "rotation");
                Vector3f var2 = this.getVector3f(var1, "origin");
                var2.mul(0.0625F);
                Direction.Axis var3 = this.getAxis(var1);
                float var4 = this.getAngle(var1);
                boolean var5 = GsonHelper.getAsBoolean(var1, "rescale", false);
                var0 = new BlockElementRotation(var2, var3, var4, var5);
            }

            return var0;
        }

        private float getAngle(JsonObject param0) {
            float var0 = GsonHelper.getAsFloat(param0, "angle");
            if (var0 != 0.0F && Mth.abs(var0) != 22.5F && Mth.abs(var0) != 45.0F) {
                throw new JsonParseException("Invalid rotation " + var0 + " found, only -45/-22.5/0/22.5/45 allowed");
            } else {
                return var0;
            }
        }

        private Direction.Axis getAxis(JsonObject param0) {
            String var0 = GsonHelper.getAsString(param0, "axis");
            Direction.Axis var1 = Direction.Axis.byName(var0.toLowerCase(Locale.ROOT));
            if (var1 == null) {
                throw new JsonParseException("Invalid rotation axis: " + var0);
            } else {
                return var1;
            }
        }

        private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext param0, JsonObject param1) {
            Map<Direction, BlockElementFace> var0 = this.filterNullFromFaces(param0, param1);
            if (var0.isEmpty()) {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            } else {
                return var0;
            }
        }

        private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext param0, JsonObject param1) {
            Map<Direction, BlockElementFace> var0 = Maps.newEnumMap(Direction.class);
            JsonObject var1 = GsonHelper.getAsJsonObject(param1, "faces");

            for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                Direction var3 = this.getFacing(var2.getKey());
                var0.put(var3, param0.deserialize(var2.getValue(), BlockElementFace.class));
            }

            return var0;
        }

        private Direction getFacing(String param0) {
            Direction var0 = Direction.byName(param0);
            if (var0 == null) {
                throw new JsonParseException("Unknown facing: " + param0);
            } else {
                return var0;
            }
        }

        private Vector3f getTo(JsonObject param0) {
            Vector3f var0 = this.getVector3f(param0, "to");
            if (!(var0.x() < -16.0F) && !(var0.y() < -16.0F) && !(var0.z() < -16.0F) && !(var0.x() > 32.0F) && !(var0.y() > 32.0F) && !(var0.z() > 32.0F)) {
                return var0;
            } else {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + var0);
            }
        }

        private Vector3f getFrom(JsonObject param0) {
            Vector3f var0 = this.getVector3f(param0, "from");
            if (!(var0.x() < -16.0F) && !(var0.y() < -16.0F) && !(var0.z() < -16.0F) && !(var0.x() > 32.0F) && !(var0.y() > 32.0F) && !(var0.z() > 32.0F)) {
                return var0;
            } else {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + var0);
            }
        }

        private Vector3f getVector3f(JsonObject param0, String param1) {
            JsonArray var0 = GsonHelper.getAsJsonArray(param0, param1);
            if (var0.size() != 3) {
                throw new JsonParseException("Expected 3 " + param1 + " values, found: " + var0.size());
            } else {
                float[] var1 = new float[3];

                for(int var2 = 0; var2 < var1.length; ++var2) {
                    var1[var2] = GsonHelper.convertToFloat(var0.get(var2), param1 + "[" + var2 + "]");
                }

                return new Vector3f(var1[0], var1[1], var1[2]);
            }
        }
    }
}
