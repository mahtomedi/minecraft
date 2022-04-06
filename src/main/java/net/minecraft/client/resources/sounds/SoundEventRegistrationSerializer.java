package net.minecraft.client.resources.sounds;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class SoundEventRegistrationSerializer implements JsonDeserializer<SoundEventRegistration> {
    private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

    public SoundEventRegistration deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
        JsonObject var0 = GsonHelper.convertToJsonObject(param0, "entry");
        boolean var1 = GsonHelper.getAsBoolean(var0, "replace", false);
        String var2 = GsonHelper.getAsString(var0, "subtitle", null);
        List<Sound> var3 = this.getSounds(var0);
        return new SoundEventRegistration(var3, var1, var2);
    }

    private List<Sound> getSounds(JsonObject param0) {
        List<Sound> var0 = Lists.newArrayList();
        if (param0.has("sounds")) {
            JsonArray var1 = GsonHelper.getAsJsonArray(param0, "sounds");

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                JsonElement var3 = var1.get(var2);
                if (GsonHelper.isStringValue(var3)) {
                    String var4 = GsonHelper.convertToString(var3, "sound");
                    var0.add(new Sound(var4, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16));
                } else {
                    var0.add(this.getSound(GsonHelper.convertToJsonObject(var3, "sound")));
                }
            }
        }

        return var0;
    }

    private Sound getSound(JsonObject param0) {
        String var0 = GsonHelper.getAsString(param0, "name");
        Sound.Type var1 = this.getType(param0, Sound.Type.FILE);
        float var2 = GsonHelper.getAsFloat(param0, "volume", 1.0F);
        Validate.isTrue(var2 > 0.0F, "Invalid volume");
        float var3 = GsonHelper.getAsFloat(param0, "pitch", 1.0F);
        Validate.isTrue(var3 > 0.0F, "Invalid pitch");
        int var4 = GsonHelper.getAsInt(param0, "weight", 1);
        Validate.isTrue(var4 > 0, "Invalid weight");
        boolean var5 = GsonHelper.getAsBoolean(param0, "preload", false);
        boolean var6 = GsonHelper.getAsBoolean(param0, "stream", false);
        int var7 = GsonHelper.getAsInt(param0, "attenuation_distance", 16);
        return new Sound(var0, ConstantFloat.of(var2), ConstantFloat.of(var3), var4, var1, var6, var5, var7);
    }

    private Sound.Type getType(JsonObject param0, Sound.Type param1) {
        Sound.Type var0 = param1;
        if (param0.has("type")) {
            var0 = Sound.Type.getByName(GsonHelper.getAsString(param0, "type"));
            Validate.notNull(var0, "Invalid type");
        }

        return var0;
    }
}
