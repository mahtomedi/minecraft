package net.minecraft.network.chat;

import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
    Style getStyle();

    ComponentContents getContents();

    @Override
    default String getString() {
        return FormattedText.super.getString();
    }

    default String getString(int param0) {
        StringBuilder var0 = new StringBuilder();
        this.visit(param2 -> {
            int var0x = param0 - var0.length();
            if (var0x <= 0) {
                return STOP_ITERATION;
            } else {
                var0.append(param2.length() <= var0x ? param2 : param2.substring(0, var0x));
                return Optional.empty();
            }
        });
        return var0.toString();
    }

    List<Component> getSiblings();

    default MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
    }

    FormattedCharSequence getVisualOrderText();

    @Override
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        Style var0 = this.getStyle().applyTo(param1);
        Optional<T> var1 = this.getContents().visit(param0, var0);
        if (var1.isPresent()) {
            return var1;
        } else {
            for(Component var2 : this.getSiblings()) {
                Optional<T> var3 = var2.visit(param0, var0);
                if (var3.isPresent()) {
                    return var3;
                }
            }

            return Optional.empty();
        }
    }

    @Override
    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
        Optional<T> var0 = this.getContents().visit(param0);
        if (var0.isPresent()) {
            return var0;
        } else {
            for(Component var1 : this.getSiblings()) {
                Optional<T> var2 = var1.visit(param0);
                if (var2.isPresent()) {
                    return var2;
                }
            }

            return Optional.empty();
        }
    }

    default List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default List<Component> toFlatList(Style param0) {
        List<Component> var0 = Lists.newArrayList();
        this.visit((param1, param2) -> {
            if (!param2.isEmpty()) {
                var0.add(literal(param2).withStyle(param1));
            }

            return Optional.empty();
        }, param0);
        return var0;
    }

    default boolean contains(Component param0) {
        if (this.equals(param0)) {
            return true;
        } else {
            List<Component> var0 = this.toFlatList();
            List<Component> var1 = param0.toFlatList(this.getStyle());
            return Collections.indexOfSubList(var0, var1) != -1;
        }
    }

    static Component nullToEmpty(@Nullable String param0) {
        return (Component)(param0 != null ? literal(param0) : CommonComponents.EMPTY);
    }

    static MutableComponent literal(String param0) {
        return MutableComponent.create(new LiteralContents(param0));
    }

    static MutableComponent translatable(String param0) {
        return MutableComponent.create(new TranslatableContents(param0, null, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatable(String param0, Object... param1) {
        return MutableComponent.create(new TranslatableContents(param0, null, param1));
    }

    static MutableComponent translatableWithFallback(String param0, @Nullable String param1) {
        return MutableComponent.create(new TranslatableContents(param0, param1, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatableWithFallback(String param0, @Nullable String param1, Object... param2) {
        return MutableComponent.create(new TranslatableContents(param0, param1, param2));
    }

    static MutableComponent empty() {
        return MutableComponent.create(ComponentContents.EMPTY);
    }

    static MutableComponent keybind(String param0) {
        return MutableComponent.create(new KeybindContents(param0));
    }

    static MutableComponent nbt(String param0, boolean param1, Optional<Component> param2, DataSource param3) {
        return MutableComponent.create(new NbtContents(param0, param1, param2, param3));
    }

    static MutableComponent score(String param0, String param1) {
        return MutableComponent.create(new ScoreContents(param0, param1));
    }

    static MutableComponent selector(String param0, Optional<Component> param1) {
        return MutableComponent.create(new SelectorContents(param0, param1));
    }

    public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
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

        public MutableComponent deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonPrimitive()) {
                return Component.literal(param0.getAsString());
            } else if (!param0.isJsonObject()) {
                if (param0.isJsonArray()) {
                    JsonArray var27 = param0.getAsJsonArray();
                    MutableComponent var28 = null;

                    for(JsonElement var29 : var27) {
                        MutableComponent var30 = this.deserialize(var29, var29.getClass(), param2);
                        if (var28 == null) {
                            var28 = var30;
                        } else {
                            var28.append(var30);
                        }
                    }

                    return var28;
                } else {
                    throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                }
            } else {
                JsonObject var0 = param0.getAsJsonObject();
                MutableComponent var2;
                if (var0.has("text")) {
                    String var1 = GsonHelper.getAsString(var0, "text");
                    var2 = var1.isEmpty() ? Component.empty() : Component.literal(var1);
                } else if (var0.has("translate")) {
                    String var3 = GsonHelper.getAsString(var0, "translate");
                    String var4 = GsonHelper.getAsString(var0, "fallback", null);
                    if (var0.has("with")) {
                        JsonArray var5 = GsonHelper.getAsJsonArray(var0, "with");
                        Object[] var6 = new Object[var5.size()];

                        for(int var7 = 0; var7 < var6.length; ++var7) {
                            var6[var7] = unwrapTextArgument(this.deserialize(var5.get(var7), param1, param2));
                        }

                        var2 = Component.translatableWithFallback(var3, var4, var6);
                    } else {
                        var2 = Component.translatableWithFallback(var3, var4);
                    }
                } else if (var0.has("score")) {
                    JsonObject var10 = GsonHelper.getAsJsonObject(var0, "score");
                    if (!var10.has("name") || !var10.has("objective")) {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    var2 = Component.score(GsonHelper.getAsString(var10, "name"), GsonHelper.getAsString(var10, "objective"));
                } else if (var0.has("selector")) {
                    Optional<Component> var13 = this.parseSeparator(param1, param2, var0);
                    var2 = Component.selector(GsonHelper.getAsString(var0, "selector"), var13);
                } else if (var0.has("keybind")) {
                    var2 = Component.keybind(GsonHelper.getAsString(var0, "keybind"));
                } else {
                    if (!var0.has("nbt")) {
                        throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                    }

                    String var16 = GsonHelper.getAsString(var0, "nbt");
                    Optional<Component> var17 = this.parseSeparator(param1, param2, var0);
                    boolean var18 = GsonHelper.getAsBoolean(var0, "interpret", false);
                    DataSource var19;
                    if (var0.has("block")) {
                        var19 = new BlockDataSource(GsonHelper.getAsString(var0, "block"));
                    } else if (var0.has("entity")) {
                        var19 = new EntityDataSource(GsonHelper.getAsString(var0, "entity"));
                    } else {
                        if (!var0.has("storage")) {
                            throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                        }

                        var19 = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(var0, "storage")));
                    }

                    var2 = Component.nbt(var16, var18, var17, var19);
                }

                if (var0.has("extra")) {
                    JsonArray var25 = GsonHelper.getAsJsonArray(var0, "extra");
                    if (var25.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for(int var26 = 0; var26 < var25.size(); ++var26) {
                        var2.append(this.deserialize(var25.get(var26), param1, param2));
                    }
                }

                var2.setStyle(param2.deserialize(param0, Style.class));
                return var2;
            }
        }

        private static Object unwrapTextArgument(Object param0) {
            if (param0 instanceof Component var0 && var0.getStyle().isEmpty() && var0.getSiblings().isEmpty()) {
                ComponentContents var1 = var0.getContents();
                if (var1 instanceof LiteralContents var2) {
                    return var2.text();
                }
            }

            return param0;
        }

        private Optional<Component> parseSeparator(Type param0, JsonDeserializationContext param1, JsonObject param2) {
            return param2.has("separator") ? Optional.of(this.deserialize(param2.get("separator"), param0, param1)) : Optional.empty();
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
                    var1.add(this.serialize(var2, Component.class, param2));
                }

                var0.add("extra", var1);
            }

            ComponentContents var3 = param0.getContents();
            if (var3 == ComponentContents.EMPTY) {
                var0.addProperty("text", "");
            } else if (var3 instanceof LiteralContents var4) {
                var0.addProperty("text", var4.text());
            } else if (var3 instanceof TranslatableContents var5) {
                var0.addProperty("translate", var5.getKey());
                String var6 = var5.getFallback();
                if (var6 != null) {
                    var0.addProperty("fallback", var6);
                }

                if (var5.getArgs().length > 0) {
                    JsonArray var7 = new JsonArray();

                    for(Object var8 : var5.getArgs()) {
                        if (var8 instanceof Component) {
                            var7.add(this.serialize((Component)var8, var8.getClass(), param2));
                        } else {
                            var7.add(new JsonPrimitive(String.valueOf(var8)));
                        }
                    }

                    var0.add("with", var7);
                }
            } else if (var3 instanceof ScoreContents var9) {
                JsonObject var10 = new JsonObject();
                var10.addProperty("name", var9.getName());
                var10.addProperty("objective", var9.getObjective());
                var0.add("score", var10);
            } else if (var3 instanceof SelectorContents var11) {
                var0.addProperty("selector", var11.getPattern());
                this.serializeSeparator(param2, var0, var11.getSeparator());
            } else if (var3 instanceof KeybindContents var12) {
                var0.addProperty("keybind", var12.getName());
            } else {
                if (!(var3 instanceof NbtContents)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + var3 + " as a Component");
                }

                NbtContents var13 = (NbtContents)var3;
                var0.addProperty("nbt", var13.getNbtPath());
                var0.addProperty("interpret", var13.isInterpreting());
                this.serializeSeparator(param2, var0, var13.getSeparator());
                DataSource var14 = var13.getDataSource();
                if (var14 instanceof BlockDataSource var15) {
                    var0.addProperty("block", var15.posPattern());
                } else if (var14 instanceof EntityDataSource var16) {
                    var0.addProperty("entity", var16.selectorPattern());
                } else {
                    if (!(var14 instanceof StorageDataSource)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + var3 + " as a Component");
                    }

                    StorageDataSource var17 = (StorageDataSource)var14;
                    var0.addProperty("storage", var17.id().toString());
                }
            }

            return var0;
        }

        private void serializeSeparator(JsonSerializationContext param0, JsonObject param1, Optional<Component> param2) {
            param2.ifPresent(param2x -> param1.add("separator", this.serialize(param2x, param2x.getClass(), param0)));
        }

        public static String toJson(Component param0) {
            return GSON.toJson(param0);
        }

        public static String toStableJson(Component param0) {
            return GsonHelper.toStableString(toJsonTree(param0));
        }

        public static JsonElement toJsonTree(Component param0) {
            return GSON.toJsonTree(param0);
        }

        @Nullable
        public static MutableComponent fromJson(String param0) {
            return GsonHelper.fromNullableJson(GSON, param0, MutableComponent.class, false);
        }

        @Nullable
        public static MutableComponent fromJson(JsonElement param0) {
            return GSON.fromJson(param0, MutableComponent.class);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String param0) {
            return GsonHelper.fromNullableJson(GSON, param0, MutableComponent.class, true);
        }

        public static MutableComponent fromJson(com.mojang.brigadier.StringReader param0) {
            try {
                JsonReader var0 = new JsonReader(new StringReader(param0.getRemaining()));
                var0.setLenient(false);
                MutableComponent var1 = GSON.getAdapter(MutableComponent.class).read(var0);
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
