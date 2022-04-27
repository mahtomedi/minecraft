package net.minecraft.network.chat;

public class ThrowingComponent extends Exception {
    private final Component component;

    public ThrowingComponent(Component param0) {
        super(param0.getString());
        this.component = param0;
    }

    public ThrowingComponent(Component param0, Throwable param1) {
        super(param0.getString(), param1);
        this.component = param0;
    }

    public Component getComponent() {
        return this.component;
    }
}
