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
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface Component extends Message, FormattedText {
    Style getStyle();

    String getContents();

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

    MutableComponent plainCopy();

    MutableComponent copy();

    @OnlyIn(Dist.CLIENT)
    FormattedCharSequence getVisualOrderText();

    @OnlyIn(Dist.CLIENT)
    @Override
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        Style var0 = this.getStyle().applyTo(param1);
        Optional<T> var1 = this.visitSelf(param0, var0);
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
        Optional<T> var0 = this.visitSelf(param0);
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

    @OnlyIn(Dist.CLIENT)
    default <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        return param0.accept(param1, this.getContents());
    }

    default <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> param0) {
        return param0.accept(this.getContents());
    }

    @OnlyIn(Dist.CLIENT)
    static Component nullToEmpty(@Nullable String param0) {
        return (Component)(param0 != null ? new TextComponent(param0) : TextComponent.EMPTY);
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
                return new TextComponent(param0.getAsString());
            } else if (!param0.isJsonObject()) {
                if (param0.isJsonArray()) {
                    JsonArray var23 = param0.getAsJsonArray();
                    MutableComponent var24 = null;

                    for(JsonElement var25 : var23) {
                        MutableComponent var26 = this.deserialize(var25, var25.getClass(), param2);
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
                MutableComponent var1;
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
                } else if (param0 instanceof NbtComponent.EntityNbtComponent) {
                    NbtComponent.EntityNbtComponent var12 = (NbtComponent.EntityNbtComponent)param0;
                    var0.addProperty("entity", var12.getSelector());
                } else {
                    if (!(param0 instanceof NbtComponent.StorageNbtComponent)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + param0 + " as a Component");
                    }

                    NbtComponent.StorageNbtComponent var13 = (NbtComponent.StorageNbtComponent)param0;
                    var0.addProperty("storage", var13.getId().toString());
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
