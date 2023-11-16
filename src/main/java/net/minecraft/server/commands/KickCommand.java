package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
    private static final SimpleCommandExceptionType ERROR_KICKING_OWNER = new SimpleCommandExceptionType(Component.translatable("commands.kick.owner.failed"));
    private static final SimpleCommandExceptionType ERROR_SINGLEPLAYER = new SimpleCommandExceptionType(
        Component.translatable("commands.kick.singleplayer.failed")
    );

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

    private static int kickPlayers(CommandSourceStack param0, Collection<ServerPlayer> param1, Component param2) throws CommandSyntaxException {
        if (!param0.getServer().isPublished()) {
            throw ERROR_SINGLEPLAYER.create();
        } else {
            int var0 = 0;

            for(ServerPlayer var1 : param1) {
                if (!param0.getServer().isSingleplayerOwner(var1.getGameProfile())) {
                    var1.connection.disconnect(param2);
                    param0.sendSuccess(() -> Component.translatable("commands.kick.success", var1.getDisplayName(), param2), true);
                    ++var0;
                }
            }

            if (var0 == 0) {
                throw ERROR_KICKING_OWNER.create();
            } else {
                return var0;
            }
        }
    }
}
