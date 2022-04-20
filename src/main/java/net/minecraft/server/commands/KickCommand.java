package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("kick")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .executes(
                            param0x -> kickPlayers(
                                    param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), Component.translatable("multiplayer.disconnect.kicked")
                                )
                        )
                        .then(
                            Commands.argument("reason", MessageArgument.message())
                                .executes(
                                    param0x -> kickPlayers(
                                            param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), MessageArgument.getMessage(param0x, "reason")
                                        )
                                )
                        )
                )
        );
    }

    private static int kickPlayers(CommandSourceStack param0, Collection<ServerPlayer> param1, Component param2) {
        for(ServerPlayer var0 : param1) {
            var0.connection.disconnect(param2);
            param0.sendSuccess(Component.translatable("commands.kick.success", var0.getDisplayName(), param2), true);
        }

        return param1.size();
    }
}
