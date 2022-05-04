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

    static Component nullToEmpty(@Nullable String param0) {
        return (Component)(param0 != null ? literal(param0) : CommonComponents.EMPTY);
    }

    static MutableComponent literal(String param0) {
        return MutableComponent.create(new LiteralContents(param0));
    }

    static MutableComponent translatable(String param0) {
        return MutableComponent.create(new TranslatableContents(param0));
    }

    static MutableComponent translatable(String param0, Object... param1) {
        return MutableComponent.create(new TranslatableContents(param0, param1));
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
                    JsonArray var26 = param0.getAsJsonArray();
                    MutableComponent var27 = null;

                    for(JsonElement var28 : var26) {
                        MutableComponent var29 = this.deserialize(var28, var28.getClass(), param2);
                        if (var27 == null) {
                            var27 = var29;
                        } else {
                            var27.append(var29);
                        }
                    }

                    return var27;
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
                    if (var0.has("with")) {
                        JsonArray var4 = GsonHelper.getAsJsonArray(var0, "with");
                        Object[] var5 = new Object[var4.size()];

                        for(int var6 = 0; var6 < var5.length; ++var6) {
                            var5[var6] = unwrapTextArgument(this.deserialize(var4.get(var6), param1, param2));
                        }

                        var2 = Component.translatable(var3, var5);
                    } else {
                        var2 = Component.translatable(var3);
                    }
                } else if (var0.has("score")) {
                    JsonObject var9 = GsonHelper.getAsJsonObject(var0, "score");
                    if (!var9.has("name") || !var9.has("objective")) {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    var2 = Component.score(GsonHelper.getAsString(var9, "name"), GsonHelper.getAsString(var9, "objective"));
                } else if (var0.has("selector")) {
                    Optional<Component> var12 = this.parseSeparator(param1, param2, var0);
                    var2 = Component.selector(GsonHelper.getAsString(var0, "selector"), var12);
                } else if (var0.has("keybind")) {
                    var2 = Component.keybind(GsonHelper.getAsString(var0, "keybind"));
                } else {
                    if (!var0.has("nbt")) {
                        throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                    }

                    String var15 = GsonHelper.getAsString(var0, "nbt");
                    Optional<Component> var16 = this.parseSeparator(param1, param2, var0);
                    boolean var17 = GsonHelper.getAsBoolean(var0, "interpret", false);
                    DataSource var18;
                    if (var0.has("block")) {
                        var18 = new BlockDataSource(GsonHelper.getAsString(var0, "block"));
                    } else if (var0.has("entity")) {
                        var18 = new EntityDataSource(GsonHelper.getAsString(var0, "entity"));
                    } else {
                        if (!var0.has("storage")) {
                            throw new JsonParseException("Don't know how to turn " + param0 + " into a Component");
                        }

                        var18 = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(var0, "storage")));
                    }

                    var2 = Component.nbt(var15, var17, var16, var18);
                }

                if (var0.has("extra")) {
                    JsonArray var24 = GsonHelper.getAsJsonArray(var0, "extra");
                    if (var24.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for(int var25 = 0; var25 < var24.size(); ++var25) {
                        var2.append(this.deserialize(var24.get(var25), param1, param2));
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
                if (var5.getArgs().length > 0) {
                    JsonArray var6 = new JsonArray();

                    for(Object var7 : var5.getArgs()) {
                        if (var7 instanceof Component) {
                            var6.add(this.serialize((Component)var7, var7.getClass(), param2));
                        } else {
                            var6.add(new JsonPrimitive(String.valueOf(var7)));
                        }
                    }

                    var0.add("with", var6);
                }
            } else if (var3 instanceof ScoreContents var8) {
                JsonObject var9 = new JsonObject();
                var9.addProperty("name", var8.getName());
                var9.addProperty("objective", var8.getObjective());
                var0.add("score", var9);
            } else if (var3 instanceof SelectorContents var10) {
                var0.addProperty("selector", var10.getPattern());
                this.serializeSeparator(param2, var0, var10.getSeparator());
            } else if (var3 instanceof KeybindContents var11) {
                var0.addProperty("keybind", var11.getName());
            } else {
                if (!(var3 instanceof NbtContents)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + var3 + " as a Component");
                }

                NbtContents var12 = (NbtContents)var3;
                var0.addProperty("nbt", var12.getNbtPath());
                var0.addProperty("interpret", var12.isInterpreting());
                this.serializeSeparator(param2, var0, var12.getSeparator());
                DataSource var13 = var12.getDataSource();
                if (var13 instanceof BlockDataSource var14) {
                    var0.addProperty("block", var14.posPattern());
                } else if (var13 instanceof EntityDataSource var15) {
                    var0.addProperty("entity", var15.selectorPattern());
                } else {
                    if (!(var13 instanceof StorageDataSource)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + var3 + " as a Component");
                    }

                    StorageDataSource var16 = (StorageDataSource)var13;
                    var0.addProperty("storage", var16.id().toString());
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
            return GsonHelper.fromJson(GSON, param0, MutableComponent.class, false);
        }

        @Nullable
        public static MutableComponent fromJson(JsonElement param0) {
            return GSON.fromJson(param0, MutableComponent.class);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String param0) {
            return GsonHelper.fromJson(GSON, param0, MutableComponent.class, true);
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
