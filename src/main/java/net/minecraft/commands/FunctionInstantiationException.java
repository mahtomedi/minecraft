package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public class FunctionInstantiationException extends Exception {
    private final Component messageComponent;

    public FunctionInstantiationException(Component param0) {
        super(param0.getString());
        this.messageComponent = param0;
    }

    public Component messageComponent() {
        return this.messageComponent;
    }
}
