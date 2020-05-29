package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.locale.Language;

public class TextComponent extends BaseComponent {
    public static final Component EMPTY = new TextComponent("");
    private final String text;
    @Nullable
    private Language decomposedWith;
    private String reorderedText;

    public TextComponent(String param0) {
        this.text = param0;
        this.reorderedText = param0;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String getContents() {
        if (this.text.isEmpty()) {
            return this.text;
        } else {
            Language var0 = Language.getInstance();
            if (this.decomposedWith != var0) {
                this.reorderedText = var0.reorder(this.text, false);
                this.decomposedWith = var0;
            }

            return this.reorderedText;
        }
    }

    public TextComponent toMutable() {
        TextComponent var0 = new TextComponent(this.text);
        var0.setStyle(this.getStyle());
        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof TextComponent)) {
            return false;
        } else {
            TextComponent var0 = (TextComponent)param0;
            return this.text.equals(var0.getText()) && super.equals(param0);
        }
    }

    @Override
    public String toString() {
        return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
