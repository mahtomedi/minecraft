package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("msg")
                .then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                    MessageArgument.ChatMessage var0x = MessageArgument.getChatMessage(param0x, "message");
        
                    try {
                        return sendMessage(param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), var0x);
                    } catch (Exception var3) {
                        var0x.consume(param0x.getSource());
                        throw var3;
                    }
                })))
        );
        param0.register(Commands.literal("tell").redirect(var0));
        param0.register(Commands.literal("w").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, Collection<ServerPlayer> param1, MessageArgument.ChatMessage param2) {
        ChatSender var0 = param0.asChatSender();
        ChatType.Bound var1 = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, param0);
        param2.resolve(param0).thenAcceptAsync(param4 -> {
            FilteredText<OutgoingPlayerChatMessage> var0x = OutgoingPlayerChatMessage.createFromFiltered(param4, var0);

            for(ServerPlayer var1x : param1) {
                ChatType.Bound var2x = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, param0).withTargetName(var1x.getDisplayName());
                param0.sendChatMessage(var0x.raw(), var2x);
                OutgoingPlayerChatMessage var3x = var0x.filter(param0, var1x);
                if (var3x != null) {
                    var1x.sendChatMessage(var3x, var1);
                }
            }

            var0x.raw().sendHeadersToRemainingPlayers(param0.getServer().getPlayerList());
        }, param0.getServer());
        return param1.size();
    }
}
