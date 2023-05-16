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

public class OpCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(Component.translatable("commands.op.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("op")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(
                            (param0x, param1) -> {
                                PlayerList var0x = param0x.getSource().getServer().getPlayerList();
                                return SharedSuggestionProvider.suggest(
                                    var0x.getPlayers()
                                        .stream()
                                        .filter(param1x -> !var0x.isOp(param1x.getGameProfile()))
                                        .map(param0xx -> param0xx.getGameProfile().getName()),
                                    param1
                                );
                            }
                        )
                        .executes(param0x -> opPlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets")))
                )
        );
    }

    private static int opPlayers(CommandSourceStack param0, Collection<GameProfile> param1) throws CommandSyntaxException {
        PlayerList var0 = param0.getServer().getPlayerList();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (!var0.isOp(var2)) {
                var0.op(var2);
                ++var1;
                param0.sendSuccess(() -> Component.translatable("commands.op.success", param1.iterator().next().getName()), true);
            }
        }

        if (var1 == 0) {
            throw ERROR_ALREADY_OP.create();
        } else {
            return var1;
        }
    }
}
