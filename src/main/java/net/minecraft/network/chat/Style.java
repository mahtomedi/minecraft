package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
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
        Optional<ClickEvent> param6,
        Optional<HoverEvent> param7,
        Optional<String> param8,
        Optional<ResourceLocation> param9
    ) {
        Style var0 = new Style(
            param0.orElse(null),
            param1.orElse(null),
            param2.orElse(null),
            param3.orElse(null),
            param4.orElse(null),
            param5.orElse(null),
            param6.orElse(null),
            param7.orElse(null),
            param8.orElse(null),
            param9.orElse(null)
        );
        return var0.equals(EMPTY) ? EMPTY : var0;
    }

    private Style(
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

    private static <T> Style checkEmptyAfterChange(Style param0, @Nullable T param1, @Nullable T param2) {
        return param1 != null && param2 == null && param0.equals(EMPTY) ? EMPTY : param0;
    }

    public Style withColor(@Nullable TextColor param0) {
        return Objects.equals(this.color, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    param0,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.color,
                param0
            );
    }

    public Style withColor(@Nullable ChatFormatting param0) {
        return this.withColor(param0 != null ? TextColor.fromLegacyFormat(param0) : null);
    }

    public Style withColor(int param0) {
        return this.withColor(TextColor.fromRgb(param0));
    }

    public Style withBold(@Nullable Boolean param0) {
        return Objects.equals(this.bold, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    param0,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.bold,
                param0
            );
    }

    public Style withItalic(@Nullable Boolean param0) {
        return Objects.equals(this.italic, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    param0,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.italic,
                param0
            );
    }

    public Style withUnderlined(@Nullable Boolean param0) {
        return Objects.equals(this.underlined, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    param0,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.underlined,
                param0
            );
    }

    public Style withStrikethrough(@Nullable Boolean param0) {
        return Objects.equals(this.strikethrough, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color, this.bold, this.italic, this.underlined, param0, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
                ),
                this.strikethrough,
                param0
            );
    }

    public Style withObfuscated(@Nullable Boolean param0) {
        return Objects.equals(this.obfuscated, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    param0,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.obfuscated,
                param0
            );
    }

    public Style withClickEvent(@Nullable ClickEvent param0) {
        return Objects.equals(this.clickEvent, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    param0,
                    this.hoverEvent,
                    this.insertion,
                    this.font
                ),
                this.clickEvent,
                param0
            );
    }

    public Style withHoverEvent(@Nullable HoverEvent param0) {
        return Objects.equals(this.hoverEvent, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    param0,
                    this.insertion,
                    this.font
                ),
                this.hoverEvent,
                param0
            );
    }

    public Style withInsertion(@Nullable String param0) {
        return Objects.equals(this.insertion, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    param0,
                    this.font
                ),
                this.insertion,
                param0
            );
    }

    public Style withFont(@Nullable ResourceLocation param0) {
        return Objects.equals(this.font, param0)
            ? this
            : checkEmptyAfterChange(
                new Style(
                    this.color,
                    this.bold,
                    this.italic,
                    this.underlined,
                    this.strikethrough,
                    this.obfuscated,
                    this.clickEvent,
                    this.hoverEvent,
                    this.insertion,
                    param0
                ),
                this.font,
                param0
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
            return this.bold == var0.bold
                && Objects.equals(this.getColor(), var0.getColor())
                && this.italic == var0.italic
                && this.obfuscated == var0.obfuscated
                && this.strikethrough == var0.strikethrough
                && this.underlined == var0.underlined
                && Objects.equals(this.clickEvent, var0.clickEvent)
                && Objects.equals(this.hoverEvent, var0.hoverEvent)
                && Objects.equals(this.insertion, var0.insertion)
                && Objects.equals(this.font, var0.font);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion
        );
    }

    public static class Serializer {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(TextColor.CODEC, "color").forGetter(param0x -> Optional.ofNullable(param0x.color)),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "bold").forGetter(param0x -> Optional.ofNullable(param0x.bold)),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "italic").forGetter(param0x -> Optional.ofNullable(param0x.italic)),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "underlined").forGetter(param0x -> Optional.ofNullable(param0x.underlined)),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "strikethrough").forGetter(param0x -> Optional.ofNullable(param0x.strikethrough)),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "obfuscated").forGetter(param0x -> Optional.ofNullable(param0x.obfuscated)),
                        ExtraCodecs.strictOptionalField(ClickEvent.CODEC, "clickEvent").forGetter(param0x -> Optional.ofNullable(param0x.clickEvent)),
                        ExtraCodecs.strictOptionalField(HoverEvent.CODEC, "hoverEvent").forGetter(param0x -> Optional.ofNullable(param0x.hoverEvent)),
                        ExtraCodecs.strictOptionalField(Codec.STRING, "insertion").forGetter(param0x -> Optional.ofNullable(param0x.insertion)),
                        ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "font").forGetter(param0x -> Optional.ofNullable(param0x.font))
                    )
                    .apply(param0, Style::create)
        );
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
    }
}
