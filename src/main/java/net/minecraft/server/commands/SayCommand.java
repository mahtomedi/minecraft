package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("say")
                .requires(param0x -> param0x.hasPermission(2))
                .then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                    MessageArgument.resolveChatMessage(param0x, "message", param1 -> {
                        CommandSourceStack var0x = param0x.getSource();
                        PlayerList var1 = var0x.getServer().getPlayerList();
                        var1.broadcastChatMessage(param1, var0x, ChatType.bind(ChatType.SAY_COMMAND, var0x));
                    });
                    return 1;
                }))
        );
    }
}
