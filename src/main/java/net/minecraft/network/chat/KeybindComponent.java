package net.minecraft.network.chat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeybindComponent extends BaseComponent {
    private static Function<String, Supplier<Component>> keyResolver = param0 -> () -> new TextComponent(param0);
    private final String name;
    private Supplier<Component> nameResolver;

    public KeybindComponent(String param0) {
        this.name = param0;
    }

    public static void setKeyResolver(Function<String, Supplier<Component>> param0) {
        keyResolver = param0;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = keyResolver.apply(this.name);
        }

        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> param0) {
        return this.getNestedComponent().visit(param0);
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        return this.getNestedComponent().visit(param0, param1);
    }

    public KeybindComponent plainCopy() {
        return new KeybindComponent(this.name);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof KeybindComponent)) {
            return false;
        } else {
            KeybindComponent var0 = (KeybindComponent)param0;
            return this.name.equals(var0.name) && super.equals(param0);
        }
    }

    @Override
    public String toString() {
        return "KeybindComponent{keybind='" + this.name + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    public String getName() {
        return this.name;
    }
}
