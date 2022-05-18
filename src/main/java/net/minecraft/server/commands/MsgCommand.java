package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
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
            param2.resolve(param0)
                .thenAcceptAsync(
                    param2x -> {
                        Component var0x = param2x.raw().serverContent();
        
                        for(ServerPlayer var1x : param1) {
                            param0.sendSuccess(
                                Component.translatable("commands.message.display.outgoing", var1x.getDisplayName(), var0x)
                                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                false
                            );
                            PlayerChatMessage var2x = param2x.filter(param0, var1x);
                            if (var2x != null) {
                                var1x.sendChatMessage(var2x, param0.asChatSender(), ChatType.MSG_COMMAND);
                            }
                        }
        
                    },
                    param0.getServer()
                );
            return param1.size();
        }
    }
}
