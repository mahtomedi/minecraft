package net.minecraft.server;

import net.minecraft.commands.CommandSourceStack;

public class ConsoleInput {
    public final String msg;
    public final CommandSourceStack source;

    public ConsoleInput(String param0, CommandSourceStack param1) {
        this.msg = param0;
        this.source = param1;
    }
}
