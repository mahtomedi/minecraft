package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

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
        ChatType.Bound var0 = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, param0);
        param2.resolve(param0, param3 -> {
            OutgoingPlayerChatMessage var0x = OutgoingPlayerChatMessage.create(param3);
            boolean var1x = param3.isFullyFiltered();
            Entity var2x = param0.getEntity();
            boolean var3x = false;

            for(ServerPlayer var4 : param1) {
                ChatType.Bound var5 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, param0).withTargetName(var4.getDisplayName());
                param0.sendChatMessage(var0x, false, var5);
                boolean var6 = param0.shouldFilterMessageTo(var4);
                var4.sendChatMessage(var0x, var6, var0);
                var3x |= var1x && var6 && var4 != var2x;
            }

            if (var3x) {
                param0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
            }

            var0x.sendHeadersToRemainingPlayers(param0.getServer().getPlayerList());
        });
        return param1.size();
    }
}
