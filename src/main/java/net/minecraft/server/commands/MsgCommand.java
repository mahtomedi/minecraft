package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
        UUID var0 = param0.getEntity() == null ? Util.NIL_UUID : param0.getEntity().getUUID();

        for(ServerPlayer var1 : param1) {
            var1.sendMessage(
                new TranslatableComponent("commands.message.display.incoming", param0.getDisplayName(), param2)
                    .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
                var0
            );
            param0.sendSuccess(
                new TranslatableComponent("commands.message.display.outgoing", var1.getDisplayName(), param2)
                    .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
                false
            );
        }

        return param1.size();
    }
}
