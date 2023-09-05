package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReturnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("return")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("value", IntegerArgumentType.integer())
                        .executes(param0x -> setReturn(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "value")))
                )
        );
    }

    private static int setReturn(CommandSourceStack param0, int param1) {
        param0.getReturnValueConsumer().accept(param1);
        return param1;
    }
}
