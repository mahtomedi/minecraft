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
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
    private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(Component.translatable("commands.deop.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("deop")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests((param0x, param1) -> SharedSuggestionProvider.suggest(param0x.getSource().getServer().getPlayerList().getOpNames(), param1))
                        .executes(param0x -> deopPlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets")))
                )
        );
    }

    private static int deopPlayers(CommandSourceStack param0, Collection<GameProfile> param1) throws CommandSyntaxException {
        PlayerList var0 = param0.getServer().getPlayerList();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (var0.isOp(var2)) {
                var0.deop(var2);
                ++var1;
                param0.sendSuccess(Component.translatable("commands.deop.success", param1.iterator().next().getName()), true);
            }
        }

        if (var1 == 0) {
            throw ERROR_NOT_OP.create();
        } else {
            param0.getServer().kickUnlistedPlayers(param0);
            return var1;
        }
    }
}
