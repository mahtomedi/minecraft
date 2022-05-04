package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public final class TextColor {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(param0 -> {
        TextColor var0 = parseColor(param0);
        return var0 != null ? DataResult.success(var0) : DataResult.error("String is not a valid color name or hex color code");
    }, TextColor::serialize);
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(ChatFormatting.values())
        .filter(ChatFormatting::isColor)
        .collect(ImmutableMap.toImmutableMap(Function.identity(), param0 -> new TextColor(param0.getColor(), param0.getName())));
    private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values()
        .stream()
        .collect(ImmutableMap.toImmutableMap(param0 -> param0.name, Function.identity()));
    private final int value;
    @Nullable
    private final String name;

    private TextColor(int param0, String param1) {
        this.value = param0;
        this.name = param1;
    }

    private TextColor(int param0) {
        this.value = param0;
        this.name = null;
    }

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    private String formatValue() {
        return String.format("#%06X", this.value);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            TextColor var0 = (TextColor)param0;
            return this.value == var0.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    @Override
    public String toString() {
        return this.name != null ? this.name : this.formatValue();
    }

    @Nullable
    public static TextColor fromLegacyFormat(ChatFormatting param0) {
        return LEGACY_FORMAT_TO_COLOR.get(param0);
    }

    public static TextColor fromRgb(int param0) {
        return new TextColor(param0);
    }

    @Nullable
    public static TextColor parseColor(String param0) {
        if (param0.startsWith("#")) {
            try {
                int var0 = Integer.parseInt(param0.substring(1), 16);
                return fromRgb(var0);
            } catch (NumberFormatException var2) {
                return null;
            }
        } else {
            return NAMED_COLORS.get(param0);
        }
    }
}
