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
                                            MessageArgument.getSignedMessage(param0x, "message")
                                        )
                                )
                        )
                )
        );
        param0.register(Commands.literal("tell").redirect(var0));
        param0.register(Commands.literal("w").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, Collection<ServerPlayer> param1, PlayerChatMessage param2) {
        PlayerChatMessage var0 = param0.getServer().getChatDecorator().decorate(param0.getPlayer(), param2);

        for(ServerPlayer var1 : param1) {
            param0.sendSuccess(
                Component.translatable("commands.message.display.outgoing", var1.getDisplayName(), var0.serverContent())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                false
            );
            var1.sendChatMessage(var0, param0.asChatSender(), ChatType.MSG_COMMAND);
        }

        return param1.size();
    }
}
