package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    public static final Codec<Style> FORMATTING_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TextColor.CODEC.optionalFieldOf("color").forGetter(param0x -> Optional.ofNullable(param0x.color)),
                    Codec.BOOL.optionalFieldOf("bold").forGetter(param0x -> Optional.ofNullable(param0x.bold)),
                    Codec.BOOL.optionalFieldOf("italic").forGetter(param0x -> Optional.ofNullable(param0x.italic)),
                    Codec.BOOL.optionalFieldOf("underlined").forGetter(param0x -> Optional.ofNullable(param0x.underlined)),
                    Codec.BOOL.optionalFieldOf("strikethrough").forGetter(param0x -> Optional.ofNullable(param0x.strikethrough)),
                    Codec.BOOL.optionalFieldOf("obfuscated").forGetter(param0x -> Optional.ofNullable(param0x.obfuscated)),
                    Codec.STRING.optionalFieldOf("insertion").forGetter(param0x -> Optional.ofNullable(param0x.insertion)),
                    ResourceLocation.CODEC.optionalFieldOf("font").forGetter(param0x -> Optional.ofNullable(param0x.font))
                )
                .apply(param0, Style::create)
    );
    public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final ResourceLocation font;

    private static Style create(
        Optional<TextColor> param0,
        Optional<Boolean> param1,
        Optional<Boolean> param2,
        Optional<Boolean> param3,
        Optional<Boolean> param4,
        Optional<Boolean> param5,
        Optional<String> param6,
        Optional<ResourceLocation> param7
    ) {
        return new Style(
            param0.orElse(null),
            param1.orElse(null),
            param2.orElse(null),
            param3.orElse(null),
            param4.orElse(null),
            param5.orElse(null),
            null,
            null,
            param6.orElse(null),
            param7.orElse(null)
        );
    }

    Style(
        @Nullable TextColor param0,
        @Nullable Boolean param1,
        @Nullable Boolean param2,
        @Nullable Boolean param3,
        @Nullable Boolean param4,
        @Nullable Boolean param5,
        @Nullable ClickEvent param6,
        @Nullable HoverEvent param7,
        @Nullable String param8,
        @Nullable ResourceLocation param9
    ) {
        this.color = param0;
        this.bold = param1;
        this.italic = param2;
        this.underlined = param3;
        this.strikethrough = param4;
        this.obfuscated = param5;
        this.clickEvent = param6;
        this.hoverEvent = param7;
        this.insertion = param8;
        this.font = param9;
    }

    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public ResourceLocation getFont() {
        return this.font != null ? this.font : DEFAULT_FONT;
    }

    public Style withColor(@Nullable TextColor param0) {
        return new Style(
            param0, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withColor(@Nullable ChatFormatting param0) {
        return this.withColor(param0 != null ? TextColor.fromLegacyFormat(param0) : null);
    }

    public Style withColor(int param0) {
        return this.withColor(TextColor.fromRgb(param0));
    }

    public Style withBold(@Nullable Boolean param0) {
        return new Style(
            this.color, param0, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withItalic(@Nullable Boolean param0) {
        return new Style(
            this.color, this.bold, param0, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withUnderlined(@Nullable Boolean param0) {
        return new Style(
            this.color, this.bold, this.italic, param0, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withStrikethrough(@Nullable Boolean param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, param0, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withObfuscated(@Nullable Boolean param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, param0, this.clickEvent, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withClickEvent(@Nullable ClickEvent param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, param0, this.hoverEvent, this.insertion, this.font
        );
    }

    public Style withHoverEvent(@Nullable HoverEvent param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, param0, this.insertion, this.font
        );
    }

    public Style withInsertion(@Nullable String param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, param0, this.font
        );
    }

    public Style withFont(@Nullable ResourceLocation param0) {
        return new Style(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, param0
        );
    }

    public Style applyFormat(ChatFormatting param0) {
        TextColor var0 = this.color;
        Boolean var1 = this.bold;
        Boolean var2 = this.italic;
        Boolean var3 = this.strikethrough;
        Boolean var4 = this.underlined;
        Boolean var5 = this.obfuscated;
        switch(param0) {
            case OBFUSCATED:
                var5 = true;
                break;
            case BOLD:
                var1 = true;
                break;
            case STRIKETHROUGH:
                var3 = true;
                break;
            case UNDERLINE:
                var4 = true;
                break;
            case ITALIC:
                var2 = true;
                break;
            case RESET:
                return EMPTY;
            default:
                var0 = TextColor.fromLegacyFormat(param0);
        }

        return new Style(var0, var1, var2, var4, var3, var5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyLegacyFormat(ChatFormatting param0) {
        TextColor var0 = this.color;
        Boolean var1 = this.bold;
        Boolean var2 = this.italic;
        Boolean var3 = this.strikethrough;
        Boolean var4 = this.underlined;
        Boolean var5 = this.obfuscated;
        switch(param0) {
            case OBFUSCATED:
                var5 = true;
                break;
            case BOLD:
                var1 = true;
                break;
            case STRIKETHROUGH:
                var3 = true;
                break;
            case UNDERLINE:
                var4 = true;
                break;
            case ITALIC:
                var2 = true;
                break;
            case RESET:
                return EMPTY;
            default:
                var5 = false;
                var1 = false;
                var3 = false;
                var4 = false;
                var2 = false;
                var0 = TextColor.fromLegacyFormat(param0);
        }

        return new Style(var0, var1, var2, var4, var3, var5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyFormats(ChatFormatting... param0) {
        TextColor var0 = this.color;
        Boolean var1 = this.bold;
        Boolean var2 = this.italic;
        Boolean var3 = this.strikethrough;
        Boolean var4 = this.underlined;
        Boolean var5 = this.obfuscated;

        for(ChatFormatting var6 : param0) {
            switch(var6) {
                case OBFUSCATED:
                    var5 = true;
                    break;
                case BOLD:
                    var1 = true;
                    break;
                case STRIKETHROUGH:
                    var3 = true;
                    break;
                case UNDERLINE:
                    var4 = true;
                    break;
                case ITALIC:
                    var2 = true;
                    break;
                case RESET:
                    return EMPTY;
                default:
                    var0 = TextColor.fromLegacyFormat(var6);
            }
        }

        return new Style(var0, var1, var2, var4, var3, var5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyTo(Style param0) {
        if (this == EMPTY) {
            return param0;
        } else {
            return param0 == EMPTY
                ? this
                : new Style(
                    this.color != null ? this.color : param0.color,
                    this.bold != null ? this.bold : param0.bold,
                    this.italic != null ? this.italic : param0.italic,
                    this.underlined != null ? this.underlined : param0.underlined,
                    this.strikethrough != null ? this.strikethrough : param0.strikethrough,
                    this.obfuscated != null ? this.obfuscated : param0.obfuscated,
                    this.clickEvent != null ? this.clickEvent : param0.clickEvent,
                    this.hoverEvent != null ? this.hoverEvent : param0.hoverEvent,
                    this.insertion != null ? this.insertion : param0.insertion,
                    this.font != null ? this.font : param0.font
                );
        }
    }

    @Override
    public String toString() {
        final StringBuilder var0 = new StringBuilder("{");

        class Collector {
            private boolean isNotFirst;

            private void prependSeparator() {
                if (this.isNotFirst) {
                    var0.append(',');
                }

                this.isNotFirst = true;
            }

            void addFlagString(String param0, @Nullable Boolean param1) {
                if (param1 != null) {
                    this.prependSeparator();
                    if (!param1) {
                        var0.append('!');
                    }

                    var0.append(param0);
                }

            }

            void addValueString(String param0, @Nullable Object param1) {
                if (param1 != null) {
                    this.prependSeparator();
                    var0.append(param0);
                    var0.append('=');
                    var0.append(param1);
                }

            }
        }

        Collector var1 = new Collector();
        var1.addValueString("color", this.color);
        var1.addFlagString("bold", this.bold);
        var1.addFlagString("italic", this.italic);
        var1.addFlagString("underlined", this.underlined);
        var1.addFlagString("strikethrough", this.strikethrough);
        var1.addFlagString("obfuscated", this.obfuscated);
        var1.addValueString("clickEvent", this.clickEvent);
        var1.addValueString("hoverEvent", this.hoverEvent);
        var1.addValueString("insertion", this.insertion);
        var1.addValueString("font", this.font);
        var0.append("}");
        return var0.toString();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Style)) {
            return false;
        } else {
            Style var0 = (Style)param0;
            return this.isBold() == var0.isBold()
                && Objects.equals(this.getColor(), var0.getColor())
                && this.isItalic() == var0.isItalic()
                && this.isObfuscated() == var0.isObfuscated()
                && this.isStrikethrough() == var0.isStrikethrough()
                && this.isUnderlined() == var0.isUnderlined()
                && Objects.equals(this.getClickEvent(), var0.getClickEvent())
                && Objects.equals(this.getHoverEvent(), var0.getHoverEvent())
                && Objects.equals(this.getInsertion(), var0.getInsertion())
                && Objects.equals(this.getFont(), var0.getFont());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion
        );
    }

    public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
        @Nullable
        public Style deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonObject()) {
                JsonObject var0 = param0.getAsJsonObject();
                if (var0 == null) {
                    return null;
                } else {
                    Boolean var1 = getOptionalFlag(var0, "bold");
                    Boolean var2 = getOptionalFlag(var0, "italic");
                    Boolean var3 = getOptionalFlag(var0, "underlined");
                    Boolean var4 = getOptionalFlag(var0, "strikethrough");
                    Boolean var5 = getOptionalFlag(var0, "obfuscated");
                    TextColor var6 = getTextColor(var0);
                    String var7 = getInsertion(var0);
                    ClickEvent var8 = getClickEvent(var0);
                    HoverEvent var9 = getHoverEvent(var0);
                    ResourceLocation var10 = getFont(var0);
                    return new Style(var6, var1, var2, var3, var4, var5, var8, var9, var7, var10);
                }
            } else {
                return null;
            }
        }

        @Nullable
        private static ResourceLocation getFont(JsonObject param0) {
            if (param0.has("font")) {
                String var0 = GsonHelper.getAsString(param0, "font");

                try {
                    return new ResourceLocation(var0);
                } catch (ResourceLocationException var3) {
                    throw new JsonSyntaxException("Invalid font name: " + var0);
                }
            } else {
                return null;
            }
        }

        @Nullable
        private static HoverEvent getHoverEvent(JsonObject param0) {
            if (param0.has("hoverEvent")) {
                JsonObject var0 = GsonHelper.getAsJsonObject(param0, "hoverEvent");
                HoverEvent var1 = HoverEvent.deserialize(var0);
                if (var1 != null && var1.getAction().isAllowedFromServer()) {
                    return var1;
                }
            }

            return null;
        }

        @Nullable
        private static ClickEvent getClickEvent(JsonObject param0) {
            if (param0.has("clickEvent")) {
                JsonObject var0 = GsonHelper.getAsJsonObject(param0, "clickEvent");
                String var1 = GsonHelper.getAsString(var0, "action", null);
                ClickEvent.Action var2 = var1 == null ? null : ClickEvent.Action.getByName(var1);
                String var3 = GsonHelper.getAsString(var0, "value", null);
                if (var2 != null && var3 != null && var2.isAllowedFromServer()) {
                    return new ClickEvent(var2, var3);
                }
            }

            return null;
        }

        @Nullable
        private static String getInsertion(JsonObject param0) {
            return GsonHelper.getAsString(param0, "insertion", null);
        }

        @Nullable
        private static TextColor getTextColor(JsonObject param0) {
            if (param0.has("color")) {
                String var0 = GsonHelper.getAsString(param0, "color");
                return TextColor.parseColor(var0);
            } else {
                return null;
            }
        }

        @Nullable
        private static Boolean getOptionalFlag(JsonObject param0, String param1) {
            return param0.has(param1) ? param0.get(param1).getAsBoolean() : null;
        }

        @Nullable
        public JsonElement serialize(Style param0, Type param1, JsonSerializationContext param2) {
            if (param0.isEmpty()) {
                return null;
            } else {
                JsonObject var0 = new JsonObject();
                if (param0.bold != null) {
                    var0.addProperty("bold", param0.bold);
                }

                if (param0.italic != null) {
                    var0.addProperty("italic", param0.italic);
                }

                if (param0.underlined != null) {
                    var0.addProperty("underlined", param0.underlined);
                }

                if (param0.strikethrough != null) {
                    var0.addProperty("strikethrough", param0.strikethrough);
                }

                if (param0.obfuscated != null) {
                    var0.addProperty("obfuscated", param0.obfuscated);
                }

                if (param0.color != null) {
                    var0.addProperty("color", param0.color.serialize());
                }

                if (param0.insertion != null) {
                    var0.add("insertion", param2.serialize(param0.insertion));
                }

                if (param0.clickEvent != null) {
                    JsonObject var1 = new JsonObject();
                    var1.addProperty("action", param0.clickEvent.getAction().getName());
                    var1.addProperty("value", param0.clickEvent.getValue());
                    var0.add("clickEvent", var1);
                }

                if (param0.hoverEvent != null) {
                    var0.add("hoverEvent", param0.hoverEvent.serialize());
                }

                if (param0.font != null) {
                    var0.addProperty("font", param0.font.toString());
                }

                return var0;
            }
        }
    }
}
