package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("say")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("message", MessageArgument.message())
                        .executes(
                            param0x -> {
                                Component var0x = MessageArgument.getMessage(param0x, "message");
                                param0x.getSource()
                                    .getServer()
                                    .getPlayerList()
                                    .broadcastMessage(new TranslatableComponent("chat.type.announcement", param0x.getSource().getDisplayName(), var0x));
                                return 1;
                            }
                        )
                )
        );
    }
}
