package net.minecraft.util.datafix.fixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.lang.reflect.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class BlockEntitySignTextStrictJsonFix extends NamedEntityFix {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Component.class, new JsonDeserializer<Component>() {
        public MutableComponent deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonPrimitive()) {
                return new TextComponent(param0.getAsString());
            } else if (param0.isJsonArray()) {
                JsonArray var0 = param0.getAsJsonArray();
                MutableComponent var1 = null;

                for(JsonElement var2 : var0) {
                    MutableComponent var3 = this.deserialize(var2, var2.getClass(), param2);
                    if (var1 == null) {
                        var1 = var3;
                    } else {
                        var1.append(var3);
                    }
                }

                return var1;
            } else {
                throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
            }
        }
    }).create();

    public BlockEntitySignTextStrictJsonFix(Schema param0, boolean param1) {
        super(param0, param1, "BlockEntitySignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> updateLine(Dynamic<?> param0, String param1) {
        String var0 = param0.get(param1).asString("");
        Component var1 = null;
        if (!"null".equals(var0) && !StringUtils.isEmpty(var0)) {
            if (var0.charAt(0) == '"' && var0.charAt(var0.length() - 1) == '"' || var0.charAt(0) == '{' && var0.charAt(var0.length() - 1) == '}') {
                try {
                    var1 = GsonHelper.fromJson(GSON, var0, Component.class, true);
                    if (var1 == null) {
                        var1 = TextComponent.EMPTY;
                    }
                } catch (Exception var8) {
                }

                if (var1 == null) {
                    try {
                        var1 = Component.Serializer.fromJson(var0);
                    } catch (Exception var7) {
                    }
                }

                if (var1 == null) {
                    try {
                        var1 = Component.Serializer.fromJsonLenient(var0);
                    } catch (Exception var6) {
                    }
                }

                if (var1 == null) {
                    var1 = new TextComponent(var0);
                }
            } else {
                var1 = new TextComponent(var0);
            }
        } else {
            var1 = TextComponent.EMPTY;
        }

        return param0.set(param1, param0.createString(Component.Serializer.toJson(var1)));
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), param0x -> {
            param0x = this.updateLine(param0x, "Text1");
            param0x = this.updateLine(param0x, "Text2");
            param0x = this.updateLine(param0x, "Text3");
            return this.updateLine(param0x, "Text4");
        });
    }
}
