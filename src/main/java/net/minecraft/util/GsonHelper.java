package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

public class GsonHelper {
    private static final Gson GSON = new GsonBuilder().create();

    public static boolean isStringValue(JsonObject param0, String param1) {
        return !isValidPrimitive(param0, param1) ? false : param0.getAsJsonPrimitive(param1).isString();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isStringValue(JsonElement param0) {
        return !param0.isJsonPrimitive() ? false : param0.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(JsonElement param0) {
        return !param0.isJsonPrimitive() ? false : param0.getAsJsonPrimitive().isNumber();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isBooleanValue(JsonObject param0, String param1) {
        return !isValidPrimitive(param0, param1) ? false : param0.getAsJsonPrimitive(param1).isBoolean();
    }

    public static boolean isArrayNode(JsonObject param0, String param1) {
        return !isValidNode(param0, param1) ? false : param0.get(param1).isJsonArray();
    }

    public static boolean isValidPrimitive(JsonObject param0, String param1) {
        return !isValidNode(param0, param1) ? false : param0.get(param1).isJsonPrimitive();
    }

    public static boolean isValidNode(JsonObject param0, String param1) {
        if (param0 == null) {
            return false;
        } else {
            return param0.get(param1) != null;
        }
    }

    public static String convertToString(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive()) {
            return param0.getAsString();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a string, was " + getType(param0));
        }
    }

    public static String getAsString(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToString(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a string");
        }
    }

    public static String getAsString(JsonObject param0, String param1, String param2) {
        return param0.has(param1) ? convertToString(param0.get(param1), param1) : param2;
    }

    public static Item convertToItem(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive()) {
            String var0 = param0.getAsString();
            return Registry.ITEM
                .getOptional(new ResourceLocation(var0))
                .orElseThrow(() -> new JsonSyntaxException("Expected " + param1 + " to be an item, was unknown string '" + var0 + "'"));
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be an item, was " + getType(param0));
        }
    }

    public static Item getAsItem(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToItem(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find an item");
        }
    }

    public static boolean convertToBoolean(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive()) {
            return param0.getAsBoolean();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Boolean, was " + getType(param0));
        }
    }

    public static boolean getAsBoolean(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToBoolean(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Boolean");
        }
    }

    public static boolean getAsBoolean(JsonObject param0, String param1, boolean param2) {
        return param0.has(param1) ? convertToBoolean(param0.get(param1), param1) : param2;
    }

    public static float convertToFloat(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsFloat();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Float, was " + getType(param0));
        }
    }

    public static float getAsFloat(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToFloat(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Float");
        }
    }

    public static float getAsFloat(JsonObject param0, String param1, float param2) {
        return param0.has(param1) ? convertToFloat(param0.get(param1), param1) : param2;
    }

    public static long convertToLong(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsLong();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Long, was " + getType(param0));
        }
    }

    public static long getAsLong(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToLong(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Long");
        }
    }

    public static long getAsLong(JsonObject param0, String param1, long param2) {
        return param0.has(param1) ? convertToLong(param0.get(param1), param1) : param2;
    }

    public static int convertToInt(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsInt();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Int, was " + getType(param0));
        }
    }

    public static int getAsInt(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToInt(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Int");
        }
    }

    public static int getAsInt(JsonObject param0, String param1, int param2) {
        return param0.has(param1) ? convertToInt(param0.get(param1), param1) : param2;
    }

    public static byte convertToByte(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsByte();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Byte, was " + getType(param0));
        }
    }

    public static byte getAsByte(JsonObject param0, String param1, byte param2) {
        return param0.has(param1) ? convertToByte(param0.get(param1), param1) : param2;
    }

    public static JsonObject convertToJsonObject(JsonElement param0, String param1) {
        if (param0.isJsonObject()) {
            return param0.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a JsonObject, was " + getType(param0));
        }
    }

    public static JsonObject getAsJsonObject(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToJsonObject(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a JsonObject");
        }
    }

    public static JsonObject getAsJsonObject(JsonObject param0, String param1, JsonObject param2) {
        return param0.has(param1) ? convertToJsonObject(param0.get(param1), param1) : param2;
    }

    public static JsonArray convertToJsonArray(JsonElement param0, String param1) {
        if (param0.isJsonArray()) {
            return param0.getAsJsonArray();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a JsonArray, was " + getType(param0));
        }
    }

    public static JsonArray getAsJsonArray(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToJsonArray(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a JsonArray");
        }
    }

    @Nullable
    public static JsonArray getAsJsonArray(JsonObject param0, String param1, @Nullable JsonArray param2) {
        return param0.has(param1) ? convertToJsonArray(param0.get(param1), param1) : param2;
    }

    public static <T> T convertToObject(@Nullable JsonElement param0, String param1, JsonDeserializationContext param2, Class<? extends T> param3) {
        if (param0 != null) {
            return param2.deserialize(param0, param3);
        } else {
            throw new JsonSyntaxException("Missing " + param1);
        }
    }

    public static <T> T getAsObject(JsonObject param0, String param1, JsonDeserializationContext param2, Class<? extends T> param3) {
        if (param0.has(param1)) {
            return convertToObject(param0.get(param1), param1, param2, param3);
        } else {
            throw new JsonSyntaxException("Missing " + param1);
        }
    }

    public static <T> T getAsObject(JsonObject param0, String param1, T param2, JsonDeserializationContext param3, Class<? extends T> param4) {
        return (T)(param0.has(param1) ? convertToObject(param0.get(param1), param1, param3, param4) : param2);
    }

    public static String getType(JsonElement param0) {
        String var0 = StringUtils.abbreviateMiddle(String.valueOf(param0), "...", 10);
        if (param0 == null) {
            return "null (missing)";
        } else if (param0.isJsonNull()) {
            return "null (json)";
        } else if (param0.isJsonArray()) {
            return "an array (" + var0 + ")";
        } else if (param0.isJsonObject()) {
            return "an object (" + var0 + ")";
        } else {
            if (param0.isJsonPrimitive()) {
                JsonPrimitive var1 = param0.getAsJsonPrimitive();
                if (var1.isNumber()) {
                    return "a number (" + var0 + ")";
                }

                if (var1.isBoolean()) {
                    return "a boolean (" + var0 + ")";
                }
            }

            return var0;
        }
    }

    @Nullable
    public static <T> T fromJson(Gson param0, Reader param1, Class<T> param2, boolean param3) {
        try {
            JsonReader var0 = new JsonReader(param1);
            var0.setLenient(param3);
            return param0.getAdapter(param2).read(var0);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static <T> T fromJson(Gson param0, Reader param1, TypeToken<T> param2, boolean param3) {
        try {
            JsonReader var0 = new JsonReader(param1);
            var0.setLenient(param3);
            return param0.getAdapter(param2).read(var0);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static <T> T fromJson(Gson param0, String param1, TypeToken<T> param2, boolean param3) {
        return fromJson(param0, new StringReader(param1), param2, param3);
    }

    @Nullable
    public static <T> T fromJson(Gson param0, String param1, Class<T> param2, boolean param3) {
        return fromJson(param0, new StringReader(param1), param2, param3);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static <T> T fromJson(Gson param0, Reader param1, TypeToken<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static <T> T fromJson(Gson param0, String param1, TypeToken<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

    @Nullable
    public static <T> T fromJson(Gson param0, Reader param1, Class<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

    @Nullable
    public static <T> T fromJson(Gson param0, String param1, Class<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

    public static JsonObject parse(String param0, boolean param1) {
        return parse(new StringReader(param0), param1);
    }

    public static JsonObject parse(Reader param0, boolean param1) {
        return fromJson(GSON, param0, JsonObject.class, param1);
    }

    public static JsonObject parse(String param0) {
        return parse(param0, false);
    }

    public static JsonObject parse(Reader param0) {
        return parse(param0, false);
    }
}
