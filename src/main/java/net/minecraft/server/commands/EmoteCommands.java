package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes(param0x -> {
            MessageArgument.ChatMessage var0x = MessageArgument.getChatMessage(param0x, "action");
            CommandSourceStack var1 = param0x.getSource();
            PlayerList var2 = var1.getServer().getPlayerList();
            var0x.resolve(var1).thenAcceptAsync(param2 -> var2.broadcastChatMessage(param2, var1, ChatType.EMOTE_COMMAND), var1.getServer());
            return 1;
        })));
    }
}
