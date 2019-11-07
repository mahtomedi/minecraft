package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.lang.reflect.Type;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemTransform {
    public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
    public final Vector3f rotation;
    public final Vector3f translation;
    public final Vector3f scale;

    public ItemTransform(Vector3f param0, Vector3f param1, Vector3f param2) {
        this.rotation = param0.copy();
        this.translation = param1.copy();
        this.scale = param2.copy();
    }

    public void apply(boolean param0, PoseStack param1) {
        if (this != NO_TRANSFORM) {
            float var0 = this.rotation.x();
            float var1 = this.rotation.y();
            float var2 = this.rotation.z();
            if (param0) {
                var1 = -var1;
                var2 = -var2;
            }

            int var3 = param0 ? -1 : 1;
            param1.translate((double)((float)var3 * this.translation.x()), (double)this.translation.y(), (double)this.translation.z());
            param1.mulPose(new Quaternion(var0, var1, var2, true));
            param1.scale(this.scale.x(), this.scale.y(), this.scale.z());
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (this.getClass() != param0.getClass()) {
            return false;
        } else {
            ItemTransform var0 = (ItemTransform)param0;
            return this.rotation.equals(var0.rotation) && this.scale.equals(var0.scale) && this.translation.equals(var0.translation);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.rotation.hashCode();
        var0 = 31 * var0 + this.translation.hashCode();
        return 31 * var0 + this.scale.hashCode();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<ItemTransform> {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);

        protected Deserializer() {
        }

        public ItemTransform deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            Vector3f var1 = this.getVector3f(var0, "rotation", DEFAULT_ROTATION);
            Vector3f var2 = this.getVector3f(var0, "translation", DEFAULT_TRANSLATION);
            var2.mul(0.0625F);
            var2.clamp(-5.0F, 5.0F);
            Vector3f var3 = this.getVector3f(var0, "scale", DEFAULT_SCALE);
            var3.clamp(-4.0F, 4.0F);
            return new ItemTransform(var1, var2, var3);
        }

        private Vector3f getVector3f(JsonObject param0, String param1, Vector3f param2) {
            if (!param0.has(param1)) {
                return param2;
            } else {
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
}
