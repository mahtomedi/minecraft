package net.minecraft.network.chat;

import java.util.function.Function;
import java.util.function.Supplier;

public class KeybindComponent extends BaseComponent {
    public static Function<String, Supplier<String>> keyResolver = param0 -> () -> param0;
    private final String name;
    private Supplier<String> nameResolver;

    public KeybindComponent(String param0) {
        this.name = param0;
    }

    @Override
    public String getContents() {
        if (this.nameResolver == null) {
            this.nameResolver = keyResolver.apply(this.name);
        }

        return this.nameResolver.get();
    }

    public KeybindComponent copy() {
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
