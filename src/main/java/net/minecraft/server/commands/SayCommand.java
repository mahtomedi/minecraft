package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("say")
                .requires(param0x -> param0x.hasPermission(2))
                .then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                    PlayerChatMessage var0x = MessageArgument.getSignedMessage(param0x, "message");
                    CommandSourceStack var1 = param0x.getSource();
                    PlayerList var2 = var1.getServer().getPlayerList();
                    if (var1.isPlayer()) {
                        ServerPlayer var3 = var1.getPlayerOrException();
                        var3.getTextFilter().processStreamMessage(var0x.signedContent().getString()).thenAcceptAsync(param4 -> {
                            PlayerChatMessage var0xx = var1.getServer().getChatDecorator().decorate(var3, var0x);
                            var2.broadcastChatMessage(var0xx, param4, var3, ChatType.SAY_COMMAND);
                        }, var1.getServer());
                    } else {
                        var2.broadcastChatMessage(var0x, var1.asChatSender(), ChatType.SAY_COMMAND);
                    }
        
                    return 1;
                }))
        );
    }
}
