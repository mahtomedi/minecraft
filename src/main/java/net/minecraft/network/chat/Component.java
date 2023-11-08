package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

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

    @Nullable
    default String tryCollapseToString() {
        ComponentContents var2 = this.getContents();
        if (var2 instanceof PlainTextContents var0 && this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
            return var0.text();
        }

        return null;
    }

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
        return MutableComponent.create(PlainTextContents.create(param0));
    }

    static MutableComponent translatable(String param0) {
        return MutableComponent.create(new TranslatableContents(param0, null, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatable(String param0, Object... param1) {
        return MutableComponent.create(new TranslatableContents(param0, null, param1));
    }

    static MutableComponent translatableEscape(String param0, Object... param1) {
        for(int var0 = 0; var0 < param1.length; ++var0) {
            Object var1 = param1[var0];
            if (!TranslatableContents.isAllowedPrimitiveArgument(var1) && !(var1 instanceof Component)) {
                param1[var0] = String.valueOf(var1);
            }
        }

        return translatable(param0, param1);
    }

    static MutableComponent translatableWithFallback(String param0, @Nullable String param1) {
        return MutableComponent.create(new TranslatableContents(param0, param1, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatableWithFallback(String param0, @Nullable String param1, Object... param2) {
        return MutableComponent.create(new TranslatableContents(param0, param1, param2));
    }

    static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
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

    static Component translationArg(Date param0) {
        return literal(param0.toString());
    }

    static Component translationArg(Message param0) {
        return (Component)(param0 instanceof Component var0 ? var0 : literal(param0.getString()));
    }

    static Component translationArg(UUID param0) {
        return literal(param0.toString());
    }

    static Component translationArg(ResourceLocation param0) {
        return literal(param0.toString());
    }

    static Component translationArg(ChunkPos param0) {
        return literal(param0.toString());
    }

    public static class Serializer {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
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

        private Serializer() {
        }

        static MutableComponent deserialize(JsonElement param0) {
            return Util.getOrThrow(ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new);
        }

        static JsonElement serialize(Component param0) {
            return Util.getOrThrow(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, param0), JsonParseException::new);
        }

        public static String toJson(Component param0) {
            return GSON.toJson(serialize(param0));
        }

        public static JsonElement toJsonTree(Component param0) {
            return serialize(param0);
        }

        @Nullable
        public static MutableComponent fromJson(String param0) {
            JsonElement var0 = JsonParser.parseString(param0);
            return var0 == null ? null : deserialize(var0);
        }

        @Nullable
        public static MutableComponent fromJson(@Nullable JsonElement param0) {
            return param0 == null ? null : deserialize(param0);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String param0) {
            JsonReader var0 = new JsonReader(new StringReader(param0));
            var0.setLenient(true);
            JsonElement var1 = JsonParser.parseReader(var0);
            return var1 == null ? null : deserialize(var1);
        }

        @Nullable
        public static MutableComponent fromJson(com.mojang.brigadier.StringReader param0) {
            JsonReader var0 = new JsonReader(new StringReader(param0.getRemaining()));
            var0.setLenient(false);

            MutableComponent var3;
            try {
                JsonElement var1 = Streams.parse(var0);
                var3 = var1 != null ? deserialize(var1) : null;
            } catch (StackOverflowError var7) {
                throw new JsonParseException(var7);
            } finally {
                param0.setCursor(param0.getCursor() + getPos(var0));
            }

            return var3;
        }

        private static int getPos(JsonReader param0) {
            try {
                return JSON_READER_POS.getInt(param0) - JSON_READER_LINESTART.getInt(param0);
            } catch (IllegalAccessException var2) {
                throw new IllegalStateException("Couldn't read position of JsonReader", var2);
            }
        }
    }

    public static class SerializerAdapter implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
        public MutableComponent deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            return Component.Serializer.deserialize(param0);
        }

        public JsonElement serialize(Component param0, Type param1, JsonSerializationContext param2) {
            return Component.Serializer.serialize(param0);
        }
    }
}
