package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes(param0x -> {
            SignedMessage var0x = MessageArgument.getSignedMessage(param0x, "action");
            CommandSourceStack var1 = param0x.getSource();
            if (var1.isPlayer()) {
                ServerPlayer var2 = var1.getPlayerOrException();
                var2.getTextFilter().processStreamMessage(var0x.content().getString()).thenAcceptAsync(param3 -> {
                    PlayerList var0xx = var1.getServer().getPlayerList();
                    var0xx.broadcastChatMessage(var0x, param3, var2, ChatType.EMOTE_COMMAND);
                }, var1.getServer());
            } else {
                var1.getServer().getPlayerList().broadcastChatMessage(var0x, var1.asChatSender(), ChatType.EMOTE_COMMAND);
            }

            return 1;
        })));
    }
}
