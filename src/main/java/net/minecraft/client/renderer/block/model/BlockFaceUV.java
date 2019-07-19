package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockFaceUV {
    public float[] uvs;
    public final int rotation;

    public BlockFaceUV(@Nullable float[] param0, int param1) {
        this.uvs = param0;
        this.rotation = param1;
    }

    public float getU(int param0) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int var0 = this.getShiftedIndex(param0);
            return this.uvs[var0 != 0 && var0 != 1 ? 2 : 0];
        }
    }

    public float getV(int param0) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int var0 = this.getShiftedIndex(param0);
            return this.uvs[var0 != 0 && var0 != 3 ? 3 : 1];
        }
    }

    private int getShiftedIndex(int param0) {
        return (param0 + this.rotation / 90) % 4;
    }

    public int getReverseIndex(int param0) {
        return (param0 + 4 - this.rotation / 90) % 4;
    }

    public void setMissingUv(float[] param0) {
        if (this.uvs == null) {
            this.uvs = param0;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockFaceUV> {
        protected Deserializer() {
        }

        public BlockFaceUV deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            float[] var1 = this.getUVs(var0);
            int var2 = this.getRotation(var0);
            return new BlockFaceUV(var1, var2);
        }

        protected int getRotation(JsonObject param0) {
            int var0 = GsonHelper.getAsInt(param0, "rotation", 0);
            if (var0 >= 0 && var0 % 90 == 0 && var0 / 90 <= 3) {
                return var0;
            } else {
                throw new JsonParseException("Invalid rotation " + var0 + " found, only 0/90/180/270 allowed");
            }
        }

        @Nullable
        private float[] getUVs(JsonObject param0) {
            if (!param0.has("uv")) {
                return null;
            } else {
                JsonArray var0 = GsonHelper.getAsJsonArray(param0, "uv");
                if (var0.size() != 4) {
                    throw new JsonParseException("Expected 4 uv values, found: " + var0.size());
                } else {
                    float[] var1 = new float[4];

                    for(int var2 = 0; var2 < var1.length; ++var2) {
                        var1[var2] = GsonHelper.convertToFloat(var0.get(var2), "uv[" + var2 + "]");
                    }

                    return var1;
                }
            }
        }
    }
}
