package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
                .then(Commands.literal("run").redirect(param0.getRoot(), param0x -> param0x.getSource().withCallback(ReturnCommand::writeResultToReturnValue)))
        );
    }

    private static int setReturn(CommandSourceStack param0, int param1) {
        param0.getReturnValueConsumer().accept(param1);
        return param1;
    }

    private static int writeResultToReturnValue(CommandContext<CommandSourceStack> param0, boolean param1, int param2) {
        int var0 = param1 ? param2 : 0;
        setReturn(param0.getSource(), var0);
        return var0;
    }
}
