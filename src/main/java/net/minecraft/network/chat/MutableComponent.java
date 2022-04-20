package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

public class MutableComponent implements Component {
    private final ComponentContents contents;
    private final List<Component> siblings;
    private Style style;
    private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
    @Nullable
    private Language decomposedWith;

    MutableComponent(ComponentContents param0, List<Component> param1, Style param2) {
        this.contents = param0;
        this.siblings = param1;
        this.style = param2;
    }

    public static MutableComponent create(ComponentContents param0) {
        return new MutableComponent(param0, Lists.newArrayList(), Style.EMPTY);
    }

    @Override
    public ComponentContents getContents() {
        return this.contents;
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    public MutableComponent setStyle(Style param0) {
        this.style = param0;
        return this;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    public MutableComponent append(String param0) {
        return this.append(Component.literal(param0));
    }

    public MutableComponent append(Component param0) {
        this.siblings.add(param0);
        return this;
    }

    public MutableComponent withStyle(UnaryOperator<Style> param0) {
        this.setStyle(param0.apply(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(Style param0) {
        this.setStyle(param0.applyTo(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting... param0) {
        this.setStyle(this.getStyle().applyFormats(param0));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting param0) {
        this.setStyle(this.getStyle().applyFormat(param0));
        return this;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        Language var0 = Language.getInstance();
        if (this.decomposedWith != var0) {
            this.visualOrderText = var0.getVisualOrder(this);
            this.decomposedWith = var0;
        }

        return this.visualOrderText;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof MutableComponent)) {
            return false;
        } else {
            MutableComponent var0 = (MutableComponent)param0;
            return this.contents.equals(var0.contents) && this.style.equals(var0.style) && this.siblings.equals(var0.siblings);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.contents, this.style, this.siblings);
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder(this.contents.toString());
        boolean var1 = !this.style.isEmpty();
        boolean var2 = !this.siblings.isEmpty();
        if (var1 || var2) {
            var0.append('[');
            if (var1) {
                var0.append("style=");
                var0.append(this.style);
            }

            if (var1 && var2) {
                var0.append(", ");
            }

            if (var2) {
                var0.append("siblings=");
                var0.append(this.siblings);
            }

            var0.append(']');
        }

        return var0.toString();
    }
}
