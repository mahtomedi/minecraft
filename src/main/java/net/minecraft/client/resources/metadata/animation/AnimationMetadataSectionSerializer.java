package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSectionSerializer implements MetadataSectionSerializer<AnimationMetadataSection> {
    public AnimationMetadataSection fromJson(JsonObject param0) {
        List<AnimationFrame> var0 = Lists.newArrayList();
        int var1 = GsonHelper.getAsInt(param0, "frametime", 1);
        if (var1 != 1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)var1, "Invalid default frame time");
        }

        if (param0.has("frames")) {
            try {
                JsonArray var2 = GsonHelper.getAsJsonArray(param0, "frames");

                for(int var3 = 0; var3 < var2.size(); ++var3) {
                    JsonElement var4 = var2.get(var3);
                    AnimationFrame var5 = this.getFrame(var3, var4);
                    if (var5 != null) {
                        var0.add(var5);
                    }
                }
            } catch (ClassCastException var81) {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + param0.get("frames"), var81);
            }
        }

        int var7 = GsonHelper.getAsInt(param0, "width", -1);
        int var8 = GsonHelper.getAsInt(param0, "height", -1);
        if (var7 != -1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)var7, "Invalid width");
        }

        if (var8 != -1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)var8, "Invalid height");
        }

        boolean var9 = GsonHelper.getAsBoolean(param0, "interpolate", false);
        return new AnimationMetadataSection(var0, var7, var8, var1, var9);
    }

    private AnimationFrame getFrame(int param0, JsonElement param1) {
        if (param1.isJsonPrimitive()) {
            return new AnimationFrame(GsonHelper.convertToInt(param1, "frames[" + param0 + "]"));
        } else if (param1.isJsonObject()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param1, "frames[" + param0 + "]");
            int var1 = GsonHelper.getAsInt(var0, "time", -1);
            if (var0.has("time")) {
                Validate.inclusiveBetween(1L, 2147483647L, (long)var1, "Invalid frame time");
            }

            int var2 = GsonHelper.getAsInt(var0, "index");
            Validate.inclusiveBetween(0L, 2147483647L, (long)var2, "Invalid frame index");
            return new AnimationFrame(var2, var1);
        } else {
            return null;
        }
    }

    @Override
    public String getMetadataSectionName() {
        return "animation";
    }
}
