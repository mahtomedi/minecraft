package net.minecraft.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;

public class CommandRuntimeException extends RuntimeException {
    private final Component message;

    public CommandRuntimeException(Component param0) {
        super(param0.getContents(), null, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES);
        this.message = param0;
    }

    public Component getComponent() {
        return this.message;
    }
}
