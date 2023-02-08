package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ResourceLocation implements Comparable<ResourceLocation> {
    public static final Codec<ResourceLocation> CODEC = Codec.STRING
        .<ResourceLocation>comapFlatMap(ResourceLocation::read, ResourceLocation::toString)
        .stable();
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    protected ResourceLocation(String param0, String param1, @Nullable ResourceLocation.Dummy param2) {
        this.namespace = param0;
        this.path = param1;
    }

    public ResourceLocation(String param0, String param1) {
        this(assertValidNamespace(param0, param1), assertValidPath(param0, param1), null);
    }

    private ResourceLocation(String[] param0) {
        this(param0[0], param0[1]);
    }

    public ResourceLocation(String param0) {
        this(decompose(param0, ':'));
    }

    public static ResourceLocation of(String param0, char param1) {
        return new ResourceLocation(decompose(param0, param1));
    }

    @Nullable
    public static ResourceLocation tryParse(String param0) {
        try {
            return new ResourceLocation(param0);
        } catch (ResourceLocationException var2) {
            return null;
        }
    }

    @Nullable
    public static ResourceLocation tryBuild(String param0, String param1) {
        try {
            return new ResourceLocation(param0, param1);
        } catch (ResourceLocationException var3) {
            return null;
        }
    }

    protected static String[] decompose(String param0, char param1) {
        String[] var0 = new String[]{"minecraft", param0};
        int var1 = param0.indexOf(param1);
        if (var1 >= 0) {
            var0[1] = param0.substring(var1 + 1);
            if (var1 >= 1) {
                var0[0] = param0.substring(0, var1);
            }
        }

        return var0;
    }

    public static DataResult<ResourceLocation> read(String param0) {
        try {
            return DataResult.success(new ResourceLocation(param0));
        } catch (ResourceLocationException var2) {
            return DataResult.error("Not a valid resource location: " + param0 + " " + var2.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ResourceLocation withPath(String param0) {
        return new ResourceLocation(this.namespace, assertValidPath(this.namespace, param0), null);
    }

    public ResourceLocation withPath(UnaryOperator<String> param0) {
        return this.withPath(param0.apply(this.path));
    }

    public ResourceLocation withPrefix(String param0) {
        return this.withPath(param0 + this.path);
    }

    public ResourceLocation withSuffix(String param0) {
        return this.withPath(this.path + param0);
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof ResourceLocation)) {
            return false;
        } else {
            ResourceLocation var0 = (ResourceLocation)param0;
            return this.namespace.equals(var0.namespace) && this.path.equals(var0.path);
        }
    }

    @Override
    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(ResourceLocation param0) {
        int var0 = this.path.compareTo(param0.path);
        if (var0 == 0) {
            var0 = this.namespace.compareTo(param0.namespace);
        }

        return var0;
    }

    public String toDebugFileName() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toLanguageKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortLanguageKey() {
        return this.namespace.equals("minecraft") ? this.path : this.toLanguageKey();
    }

    public String toLanguageKey(String param0) {
        return param0 + "." + this.toLanguageKey();
    }

    public String toLanguageKey(String param0, String param1) {
        return param0 + "." + this.toLanguageKey() + "." + param1;
    }

    public static ResourceLocation read(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && isAllowedInResourceLocation(param0.peek())) {
            param0.skip();
        }

        String var1 = param0.getString().substring(var0, param0.getCursor());

        try {
            return new ResourceLocation(var1);
        } catch (ResourceLocationException var4) {
            param0.setCursor(var0);
            throw ERROR_INVALID.createWithContext(param0);
        }
    }

    public static boolean isAllowedInResourceLocation(char param0) {
        return param0 >= '0' && param0 <= '9'
            || param0 >= 'a' && param0 <= 'z'
            || param0 == '_'
            || param0 == ':'
            || param0 == '/'
            || param0 == '.'
            || param0 == '-';
    }

    private static boolean isValidPath(String param0) {
        for(int var0 = 0; var0 < param0.length(); ++var0) {
            if (!validPathChar(param0.charAt(var0))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidNamespace(String param0) {
        for(int var0 = 0; var0 < param0.length(); ++var0) {
            if (!validNamespaceChar(param0.charAt(var0))) {
                return false;
            }
        }

        return true;
    }

    private static String assertValidNamespace(String param0, String param1) {
        if (!isValidNamespace(param0)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + param0 + ":" + param1);
        } else {
            return param0;
        }
    }

    public static boolean validPathChar(char param0) {
        return param0 == '_' || param0 == '-' || param0 >= 'a' && param0 <= 'z' || param0 >= '0' && param0 <= '9' || param0 == '/' || param0 == '.';
    }

    private static boolean validNamespaceChar(char param0) {
        return param0 == '_' || param0 == '-' || param0 >= 'a' && param0 <= 'z' || param0 >= '0' && param0 <= '9' || param0 == '.';
    }

    public static boolean isValidResourceLocation(String param0) {
        String[] var0 = decompose(param0, ':');
        return isValidNamespace(StringUtils.isEmpty(var0[0]) ? "minecraft" : var0[0]) && isValidPath(var0[1]);
    }

    private static String assertValidPath(String param0, String param1) {
        if (!isValidPath(param1)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + param0 + ":" + param1);
        } else {
            return param1;
        }
    }

    protected interface Dummy {
    }

    public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
        public ResourceLocation deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            return new ResourceLocation(GsonHelper.convertToString(param0, "location"));
        }

        public JsonElement serialize(ResourceLocation param0, Type param1, JsonSerializationContext param2) {
            return new JsonPrimitive(param0.toString());
        }
    }
}
