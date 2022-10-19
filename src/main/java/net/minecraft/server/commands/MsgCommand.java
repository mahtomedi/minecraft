package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("msg")
                .then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                    Collection<ServerPlayer> var0x = EntityArgument.getPlayers(param0x, "targets");
                    if (!var0x.isEmpty()) {
                        MessageArgument.resolveChatMessage(param0x, "message", param2 -> sendMessage(param0x.getSource(), var0x, param2));
                    }
        
                    return var0x.size();
                })))
        );
        param0.register(Commands.literal("tell").redirect(var0));
        param0.register(Commands.literal("w").redirect(var0));
    }

    private static void sendMessage(CommandSourceStack param0, Collection<ServerPlayer> param1, PlayerChatMessage param2) {
        ChatType.Bound var0 = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, param0);
        OutgoingChatMessage var1 = OutgoingChatMessage.create(param2);
        boolean var2 = false;

        for(ServerPlayer var3 : param1) {
            ChatType.Bound var4 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, param0).withTargetName(var3.getDisplayName());
            param0.sendChatMessage(var1, false, var4);
            boolean var5 = param0.shouldFilterMessageTo(var3);
            var3.sendChatMessage(var1, var5, var0);
            var2 |= var5 && param2.isFullyFiltered();
        }

        if (var2) {
            param0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

    }
}
