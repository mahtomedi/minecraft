package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardon.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("pardon")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(
                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                    param0x.getSource().getServer().getPlayerList().getBans().getUserList(), param1
                                )
                        )
                        .executes(param0x -> pardonPlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets")))
                )
        );
    }

    private static int pardonPlayers(CommandSourceStack param0, Collection<GameProfile> param1) throws CommandSyntaxException {
        UserBanList var0 = param0.getServer().getPlayerList().getBans();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (var0.isBanned(var2)) {
                var0.remove(var2);
                ++var1;
                param0.sendSuccess(() -> Component.translatable("commands.pardon.success", Component.literal(var2.getName())), true);
            }
        }

        if (var1 == 0) {
            throw ERROR_NOT_BANNED.create();
        } else {
            return var1;
        }
    }
}
