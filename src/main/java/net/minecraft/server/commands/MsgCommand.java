package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
                                            param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), MessageArgument.getMessage(param0x, "message")
                                        )
                                )
                        )
                )
        );
        param0.register(Commands.literal("tell").redirect(var0));
        param0.register(Commands.literal("w").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, Collection<ServerPlayer> param1, Component param2) {
        for(ServerPlayer var0 : param1) {
            var0.sendMessage(
                new TranslatableComponent("commands.message.display.incoming", param0.getDisplayName(), param2.deepCopy())
                    .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
            );
            param0.sendSuccess(
                new TranslatableComponent("commands.message.display.outgoing", var0.getDisplayName(), param2.deepCopy())
                    .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
                false
            );
        }

        return param1.size();
    }
}
