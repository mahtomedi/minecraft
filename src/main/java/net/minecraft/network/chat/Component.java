package net.minecraft.network.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, Iterable<Component> {
    Component setStyle(Style var1);

    Style getStyle();

    default Component append(String param0) {
        return this.append(new TextComponent(param0));
    }

    Component append(Component var1);

    String getContents();

    @Override
    default String getString() {
        StringBuilder var0 = new StringBuilder();
        this.stream().forEach(param1 -> var0.append(param1.getContents()));
        return var0.toString();
    }

    default String getString(int param0) {
        StringBuilder var0 = new StringBuilder();
        Iterator<Component> var1 = this.stream().iterator();

        while(var1.hasNext()) {
            int var2 = param0 - var0.length();
            if (var2 <= 0) {
                break;
            }

            String var3 = var1.next().getContents();
            var0.append(var3.length() <= var2 ? var3 : var3.substring(0, var2));
        }

        return var0.toString();
    }

    default String getColoredString() {
        StringBuilder var0 = new StringBuilder();
        String var1 = "";
        Iterator<Component> var2 = this.stream().iterator();

        while(var2.hasNext()) {
            Component var3 = var2.next();
            String var4 = var3.getContents();
            if (!var4.isEmpty()) {
                String var5 = var3.getStyle().getLegacyFormatCodes();
                if (!var5.equals(var1)) {
                    if (!var1.isEmpty()) {
                        var0.append(ChatFormatting.RESET);
                    }

                    var0.append(var5);
                    var1 = var5;
                }

                var0.append(var4);
            }
        }

        if (!var1.isEmpty()) {
            var0.append(ChatFormatting.RESET);
        }

        return var0.toString();
    }

    List<Component> getSiblings();

    Stream<Component> stream();

    default Stream<Component> flatStream() {
        return this.stream().map(Component::flattenStyle);
    }

    @Override
    default Iterator<Component> iterator() {
        return this.flatStream().iterator();
    }

    Component copy();

    default Component deepCopy() {
        Component var0 = this.copy();
        var0.setStyle(this.getStyle().copy());

        for(Component var1 : this.getSiblings()) {
            var0.append(var1.deepCopy());
        }

        return var0;
    }

    default Component withStyle(Consumer<Style> param0) {
        param0.accept(this.getStyle());
        return this;
    }

    default Component withStyle(ChatFormatting... param0) {
        for(ChatFormatting var0 : param0) {
            this.withStyle(var0);
        }

        return this;
    }

    default Component withStyle(ChatFormatting param0) {
        Style var0 = this.getStyle();
        if (param0.isColor()) {
            var0.setColor(param0);
        }

        if (param0.isFormat()) {
            switch(param0) {
                case OBFUSCATED:
                    var0.setObfuscated(true);
                    break;
                case BOLD:
                    var0.setBold(true);
                    break;
                case STRIKETHROUGH:
                    var0.setStrikethrough(true);
                    break;
                case UNDERLINE:
                    var0.setUnderlined(true);
                    break;
                case ITALIC:
                    var0.setItalic(true);
            }
        }

        return this;
    }

    static Component flattenStyle(Component param0) {
        Component var0 = param0.copy();
        var0.setStyle(param0.getStyle().flatCopy());
        return var0;
    }

    public static class Serializer implements JsonDeserializer<Component>, JsonSerializer<Component> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder var0 = new GsonBuilder();
            var0.disableHtmlEscaping();
            var0.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
            var0.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
            var0.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            return var0.create();
        });
        private static final Field JSON_READER_POS = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field var0 = JsonReader.class.getDeclaredField("pos");
                var0.setAccessible(true);
                return var0;
            } catch (NoSuchFieldException var11) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var11);
            }
        });
        private static final Field JSON_READER_LINESTART = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field var0 = JsonReader.class.getDeclaredField("lineStart");
                var0.setAccessible(true);
                return var0;
            } catch (NoSuchFieldException var11) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var11);
            }
        });

        public Component deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonPrimitive()) {
                return new TextComponent(param0.getAsString());
            } else if (!param0.isJsonObject()) {
                if (param0.isJsonArray()) {
                    JsonArray var23 = param0.getAsJsonArray();
                    Component var24 = null;

                    for(JsonElement var25 : var23) {
                        Component var26 = this.deserialize(var25, var25.getClass(), param2);
                        if (var24 == null) {
                            var24 = var26;
                        } else {
                            var24.append(var26);
                        }
                    }

                    return var24;
                } else {
                    throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                }
            } else {
                JsonObject var0 = param0.getAsJsonObject();
                Component var1;
                if (var0.has("text")) {
                    var1 = new TextComponent(GsonHelper.getAsString(var0, "text"));
                } else if (var0.has("translate")) {
                    String var2 = GsonHelper.getAsString(var0, "translate");
                    if (var0.has("with")) {
                        JsonArray var3 = GsonHelper.getAsJsonArray(var0, "with");
                        Object[] var4 = new Object[var3.size()];

                        for(int var5 = 0; var5 < var4.length; ++var5) {
                            var4[var5] = this.deserialize(var3.get(var5), param1, param2);
                            if (var4[var5] instanceof TextComponent) {
                                TextComponent var6 = (TextComponent)var4[var5];
                                if (var6.getStyle().isEmpty() && var6.getSiblings().isEmpty()) {
                                    var4[var5] = var6.getText();
                                }
                            }
                        }

                        var1 = new TranslatableComponent(var2, var4);
                    } else {
                        var1 = new TranslatableComponent(var2);
                    }
                } else if (var0.has("score")) {
                    JsonObject var9 = GsonHelper.getAsJsonObject(var0, "score");
                    if (!var9.has("name") || !var9.has("objective")) {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    var1 = new ScoreComponent(GsonHelper.getAsString(var9, "name"), GsonHelper.getAsString(var9, "objective"));
                    if (var9.has("value")) {
                        ((ScoreComponent)var1).setValue(GsonHelper.getAsString(var9, "value"));
                    }
                } else if (var0.has("selector")) {
                    var1 = new SelectorComponent(GsonHelper.getAsString(var0, "selector"));
                } else if (var0.has("keybind")) {
                    var1 = new KeybindComponent(GsonHelper.getAsString(var0, "keybind"));
                } else {
                    if (!var0.has("nbt")) {
                        throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                    }

                    String var14 = GsonHelper.getAsString(var0, "nbt");
                    boolean var15 = GsonHelper.getAsBoolean(var0, "interpret", false);
                    if (var0.has("block")) {
                        var1 = new NbtComponent.BlockNbtComponent(var14, var15, GsonHelper.getAsString(var0, "block"));
                    } else if (var0.has("entity")) {
                        var1 = new NbtComponent.EntityNbtComponent(var14, var15, GsonHelper.getAsString(var0, "entity"));
                    } else {
                        if (!var0.has("storage")) {
                            throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                        }

                        var1 = new NbtComponent.StorageNbtComponent(var14, var15, new ResourceLocation(GsonHelper.getAsString(var0, "storage")));
                    }
                }

                if (var0.has("extra")) {
                    JsonArray var21 = GsonHelper.getAsJsonArray(var0, "extra");
                    if (var21.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for(int var22 = 0; var22 < var21.size(); ++var22) {
                        var1.append(this.deserialize(var21.get(var22), param1, param2));
                    }
                }

                var1.setStyle(param2.deserialize(param0, Style.class));
                return var1;
            }
        }

        private void serializeStyle(Style param0, JsonObject param1, JsonSerializationContext param2) {
            JsonElement var0 = param2.serialize(param0);
            if (var0.isJsonObject()) {
                JsonObject var1 = (JsonObject)var0;

                for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                    param1.add(var2.getKey(), var2.getValue());
                }
            }

        }

        public JsonElement serialize(Component param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            if (!param0.getStyle().isEmpty()) {
                this.serializeStyle(param0.getStyle(), var0, param2);
            }

            if (!param0.getSiblings().isEmpty()) {
                JsonArray var1 = new JsonArray();

                for(Component var2 : param0.getSiblings()) {
                    var1.add(this.serialize(var2, var2.getClass(), param2));
                }

                var0.add("extra", var1);
            }

            if (param0 instanceof TextComponent) {
                var0.addProperty("text", ((TextComponent)param0).getText());
            } else if (param0 instanceof TranslatableComponent) {
                TranslatableComponent var3 = (TranslatableComponent)param0;
                var0.addProperty("translate", var3.getKey());
                if (var3.getArgs() != null && var3.getArgs().length > 0) {
                    JsonArray var4 = new JsonArray();

                    for(Object var5 : var3.getArgs()) {
                        if (var5 instanceof Component) {
                            var4.add(this.serialize((Component)var5, var5.getClass(), param2));
                        } else {
                            var4.add(new JsonPrimitive(String.valueOf(var5)));
                        }
                    }

                    var0.add("with", var4);
                }
            } else if (param0 instanceof ScoreComponent) {
                ScoreComponent var6 = (ScoreComponent)param0;
                JsonObject var7 = new JsonObject();
                var7.addProperty("name", var6.getName());
                var7.addProperty("objective", var6.getObjective());
                var7.addProperty("value", var6.getContents());
                var0.add("score", var7);
            } else if (param0 instanceof SelectorComponent) {
                SelectorComponent var8 = (SelectorComponent)param0;
                var0.addProperty("selector", var8.getPattern());
            } else if (param0 instanceof KeybindComponent) {
                KeybindComponent var9 = (KeybindComponent)param0;
                var0.addProperty("keybind", var9.getName());
            } else {
                if (!(param0 instanceof NbtComponent)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + param0 + " as a Component");
                }

                NbtComponent var10 = (NbtComponent)param0;
                var0.addProperty("nbt", var10.getNbtPath());
                var0.addProperty("interpret", var10.isInterpreting());
                if (param0 instanceof NbtComponent.BlockNbtComponent) {
                    NbtComponent.BlockNbtComponent var11 = (NbtComponent.BlockNbtComponent)param0;
                    var0.addProperty("block", var11.getPos());
                } else {
                    if (!(param0 instanceof NbtComponent.EntityNbtComponent)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + param0 + " as a Component");
                    }

                    NbtComponent.EntityNbtComponent var12 = (NbtComponent.EntityNbtComponent)param0;
                    var0.addProperty("entity", var12.getSelector());
                }
            }

            return var0;
        }

        public static String toJson(Component param0) {
            return GSON.toJson(param0);
        }

        public static JsonElement toJsonTree(Component param0) {
            return GSON.toJsonTree(param0);
        }

        @Nullable
        public static Component fromJson(String param0) {
            return GsonHelper.fromJson(GSON, param0, Component.class, false);
        }

        @Nullable
        public static Component fromJson(JsonElement param0) {
            return GSON.fromJson(param0, Component.class);
        }

        @Nullable
        public static Component fromJsonLenient(String param0) {
            return GsonHelper.fromJson(GSON, param0, Component.class, true);
        }

        public static Component fromJson(com.mojang.brigadier.StringReader param0) {
            try {
                JsonReader var0 = new JsonReader(new StringReader(param0.getRemaining()));
                var0.setLenient(false);
                Component var1 = GSON.getAdapter(Component.class).read(var0);
                param0.setCursor(param0.getCursor() + getPos(var0));
                return var1;
            } catch (StackOverflowError | IOException var3) {
                throw new JsonParseException(var3);
            }
        }

        private static int getPos(JsonReader param0) {
            try {
                return JSON_READER_POS.getInt(param0) - JSON_READER_LINESTART.getInt(param0) + 1;
            } catch (IllegalAccessException var2) {
                throw new IllegalStateException("Couldn't read position of JsonReader", var2);
            }
        }
    }
}
