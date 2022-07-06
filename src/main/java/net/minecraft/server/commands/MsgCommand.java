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
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("msg")
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("message", MessageArgument.message())
                                .executes(
                                    param0x -> sendMessage(
                                            param0x.getSource(),
                                            EntityArgument.getPlayers(param0x, "targets"),
                                            MessageArgument.getChatMessage(param0x, "message")
                                        )
                                )
                        )
                )
        );
        param0.register(Commands.literal("tell").redirect(var0));
        param0.register(Commands.literal("w").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, Collection<ServerPlayer> param1, MessageArgument.ChatMessage param2) {
        if (param1.isEmpty()) {
            return 0;
        } else {
            ChatSender var0 = param0.asChatSender();
            param2.resolve(param0).thenAcceptAsync(param3 -> {
                for(ServerPlayer var0x : param1) {
                    ChatSender var1x = var0.withTargetName(var0x.getDisplayName());
                    param0.sendChatMessage(var1x, param3.raw(), ChatType.MSG_COMMAND_OUTGOING);
                    PlayerChatMessage var2x = param3.filter(param0, var0x);
                    if (var2x != null) {
                        var0x.sendChatMessage(var2x, var0, ChatType.MSG_COMMAND_INCOMING);
                    }
                }

            }, param0.getServer());
            return param1.size();
        }
    }
}
