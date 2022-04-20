package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

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
        Entity var1 = param0.getEntity();
        Consumer<Component> var3;
        if (var1 instanceof ServerPlayer var2) {
            var3 = param2x -> var2.sendMessage(
                    Component.translatable("commands.message.display.outgoing", param2x, param2).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                    var2.getUUID()
                );
        } else {
            var3 = param2x -> param0.sendSuccess(
                    Component.translatable("commands.message.display.outgoing", param2x, param2).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false
                );
        }

        for(ServerPlayer var5 : param1) {
            var3.accept(var5.getDisplayName());
            var5.sendMessage(
                Component.translatable("commands.message.display.incoming", param0.getDisplayName(), param2)
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                var0
            );
        }

        return param1.size();
    }
}
