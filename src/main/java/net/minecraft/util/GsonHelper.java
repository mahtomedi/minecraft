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
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

public class GsonHelper {
    private static final Gson GSON = new GsonBuilder().create();

    public static boolean isStringValue(JsonObject param0, String param1) {
        return !isValidPrimitive(param0, param1) ? false : param0.getAsJsonPrimitive(param1).isString();
    }

    public static boolean isStringValue(JsonElement param0) {
        return !param0.isJsonPrimitive() ? false : param0.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(JsonObject param0, String param1) {
        return !isValidPrimitive(param0, param1) ? false : param0.getAsJsonPrimitive(param1).isNumber();
    }

    public static boolean isNumberValue(JsonElement param0) {
        return !param0.isJsonPrimitive() ? false : param0.getAsJsonPrimitive().isNumber();
    }

    public static boolean isBooleanValue(JsonObject param0, String param1) {
        return !isValidPrimitive(param0, param1) ? false : param0.getAsJsonPrimitive(param1).isBoolean();
    }

    public static boolean isBooleanValue(JsonElement param0) {
        return !param0.isJsonPrimitive() ? false : param0.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isArrayNode(JsonObject param0, String param1) {
        return !isValidNode(param0, param1) ? false : param0.get(param1).isJsonArray();
    }

    public static boolean isObjectNode(JsonObject param0, String param1) {
        return !isValidNode(param0, param1) ? false : param0.get(param1).isJsonObject();
    }

    public static boolean isValidPrimitive(JsonObject param0, String param1) {
        return !isValidNode(param0, param1) ? false : param0.get(param1).isJsonPrimitive();
    }

    public static boolean isValidNode(@Nullable JsonObject param0, String param1) {
        if (param0 == null) {
            return false;
        } else {
            return param0.get(param1) != null;
        }
    }

    public static JsonElement getNonNull(JsonObject param0, String param1) {
        JsonElement var0 = param0.get(param1);
        if (var0 != null && !var0.isJsonNull()) {
            return var0;
        } else {
            throw new JsonSyntaxException("Missing field " + param1);
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

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static String getAsString(JsonObject param0, String param1, @Nullable String param2) {
        return param0.has(param1) ? convertToString(param0.get(param1), param1) : param2;
    }

    public static Holder<Item> convertToItem(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive()) {
            String var0 = param0.getAsString();
            return BuiltInRegistries.ITEM
                .getHolder(ResourceKey.create(Registries.ITEM, new ResourceLocation(var0)))
                .orElseThrow(() -> new JsonSyntaxException("Expected " + param1 + " to be an item, was unknown string '" + var0 + "'"));
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be an item, was " + getType(param0));
        }
    }

    public static Holder<Item> getAsItem(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToItem(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find an item");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static Holder<Item> getAsItem(JsonObject param0, String param1, @Nullable Holder<Item> param2) {
        return param0.has(param1) ? convertToItem(param0.get(param1), param1) : param2;
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

    public static double convertToDouble(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsDouble();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Double, was " + getType(param0));
        }
    }

    public static double getAsDouble(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToDouble(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Double");
        }
    }

    public static double getAsDouble(JsonObject param0, String param1, double param2) {
        return param0.has(param1) ? convertToDouble(param0.get(param1), param1) : param2;
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

    public static byte getAsByte(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToByte(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Byte");
        }
    }

    public static byte getAsByte(JsonObject param0, String param1, byte param2) {
        return param0.has(param1) ? convertToByte(param0.get(param1), param1) : param2;
    }

    public static char convertToCharacter(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsCharacter();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Character, was " + getType(param0));
        }
    }

    public static char getAsCharacter(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToCharacter(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Character");
        }
    }

    public static char getAsCharacter(JsonObject param0, String param1, char param2) {
        return param0.has(param1) ? convertToCharacter(param0.get(param1), param1) : param2;
    }

    public static BigDecimal convertToBigDecimal(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsBigDecimal();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a BigDecimal, was " + getType(param0));
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToBigDecimal(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a BigDecimal");
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject param0, String param1, BigDecimal param2) {
        return param0.has(param1) ? convertToBigDecimal(param0.get(param1), param1) : param2;
    }

    public static BigInteger convertToBigInteger(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsBigInteger();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a BigInteger, was " + getType(param0));
        }
    }

    public static BigInteger getAsBigInteger(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToBigInteger(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a BigInteger");
        }
    }

    public static BigInteger getAsBigInteger(JsonObject param0, String param1, BigInteger param2) {
        return param0.has(param1) ? convertToBigInteger(param0.get(param1), param1) : param2;
    }

    public static short convertToShort(JsonElement param0, String param1) {
        if (param0.isJsonPrimitive() && param0.getAsJsonPrimitive().isNumber()) {
            return param0.getAsShort();
        } else {
            throw new JsonSyntaxException("Expected " + param1 + " to be a Short, was " + getType(param0));
        }
    }

    public static short getAsShort(JsonObject param0, String param1) {
        if (param0.has(param1)) {
            return convertToShort(param0.get(param1), param1);
        } else {
            throw new JsonSyntaxException("Missing " + param1 + ", expected to find a Short");
        }
    }

    public static short getAsShort(JsonObject param0, String param1, short param2) {
        return param0.has(param1) ? convertToShort(param0.get(param1), param1) : param2;
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

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonObject getAsJsonObject(JsonObject param0, String param1, @Nullable JsonObject param2) {
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
    @Contract("_,_,!null->!null;_,_,null->_")
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

    @Nullable
    @Contract("_,_,!null,_,_->!null;_,_,null,_,_->_")
    public static <T> T getAsObject(JsonObject param0, String param1, @Nullable T param2, JsonDeserializationContext param3, Class<? extends T> param4) {
        return (T)(param0.has(param1) ? convertToObject(param0.get(param1), param1, param3, param4) : param2);
    }

    public static String getType(@Nullable JsonElement param0) {
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
    public static <T> T fromNullableJson(Gson param0, Reader param1, Class<T> param2, boolean param3) {
        try {
            JsonReader var0 = new JsonReader(param1);
            var0.setLenient(param3);
            return param0.getAdapter(param2).read(var0);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    public static <T> T fromJson(Gson param0, Reader param1, Class<T> param2, boolean param3) {
        T var0 = fromNullableJson(param0, param1, param2, param3);
        if (var0 == null) {
            throw new JsonParseException("JSON data was null or empty");
        } else {
            return var0;
        }
    }

    @Nullable
    public static <T> T fromNullableJson(Gson param0, Reader param1, TypeToken<T> param2, boolean param3) {
        try {
            JsonReader var0 = new JsonReader(param1);
            var0.setLenient(param3);
            return param0.getAdapter(param2).read(var0);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    public static <T> T fromJson(Gson param0, Reader param1, TypeToken<T> param2, boolean param3) {
        T var0 = fromNullableJson(param0, param1, param2, param3);
        if (var0 == null) {
            throw new JsonParseException("JSON data was null or empty");
        } else {
            return var0;
        }
    }

    @Nullable
    public static <T> T fromNullableJson(Gson param0, String param1, TypeToken<T> param2, boolean param3) {
        return fromNullableJson(param0, new StringReader(param1), param2, param3);
    }

    public static <T> T fromJson(Gson param0, String param1, Class<T> param2, boolean param3) {
        return fromJson(param0, new StringReader(param1), param2, param3);
    }

    @Nullable
    public static <T> T fromNullableJson(Gson param0, String param1, Class<T> param2, boolean param3) {
        return fromNullableJson(param0, new StringReader(param1), param2, param3);
    }

    public static <T> T fromJson(Gson param0, Reader param1, TypeToken<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

    @Nullable
    public static <T> T fromNullableJson(Gson param0, String param1, TypeToken<T> param2) {
        return fromNullableJson(param0, param1, param2, false);
    }

    public static <T> T fromJson(Gson param0, Reader param1, Class<T> param2) {
        return fromJson(param0, param1, param2, false);
    }

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

    public static JsonArray parseArray(String param0) {
        return parseArray(new StringReader(param0));
    }

    public static JsonArray parseArray(Reader param0) {
        return fromJson(GSON, param0, JsonArray.class, false);
    }

    public static String toStableString(JsonElement param0) {
        StringWriter var0 = new StringWriter();
        JsonWriter var1 = new JsonWriter(var0);

        try {
            writeValue(var1, param0, Comparator.naturalOrder());
        } catch (IOException var4) {
            throw new AssertionError(var4);
        }

        return var0.toString();
    }

    public static void writeValue(JsonWriter param0, @Nullable JsonElement param1, @Nullable Comparator<String> param2) throws IOException {
        if (param1 == null || param1.isJsonNull()) {
            param0.nullValue();
        } else if (param1.isJsonPrimitive()) {
            JsonPrimitive var0 = param1.getAsJsonPrimitive();
            if (var0.isNumber()) {
                param0.value(var0.getAsNumber());
            } else if (var0.isBoolean()) {
                param0.value(var0.getAsBoolean());
            } else {
                param0.value(var0.getAsString());
            }
        } else if (param1.isJsonArray()) {
            param0.beginArray();

            for(JsonElement var1 : param1.getAsJsonArray()) {
                writeValue(param0, var1, param2);
            }

            param0.endArray();
        } else {
            if (!param1.isJsonObject()) {
                throw new IllegalArgumentException("Couldn't write " + param1.getClass());
            }

            param0.beginObject();

            for(Entry<String, JsonElement> var2 : sortByKeyIfNeeded(param1.getAsJsonObject().entrySet(), param2)) {
                param0.name(var2.getKey());
                writeValue(param0, var2.getValue(), param2);
            }

            param0.endObject();
        }

    }

    private static Collection<Entry<String, JsonElement>> sortByKeyIfNeeded(Collection<Entry<String, JsonElement>> param0, @Nullable Comparator<String> param1) {
        if (param1 == null) {
            return param0;
        } else {
            List<Entry<String, JsonElement>> var0 = new ArrayList<>(param0);
            var0.sort(Entry.comparingByKey(param1));
            return var0;
        }
    }
}
