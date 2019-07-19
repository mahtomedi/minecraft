package net.minecraft.network.chat;

public class TextComponent extends BaseComponent {
    private final String text;

    public TextComponent(String param0) {
        this.text = param0;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String getContents() {
        return this.text;
    }

    public TextComponent copy() {
        return new TextComponent(this.text);
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
