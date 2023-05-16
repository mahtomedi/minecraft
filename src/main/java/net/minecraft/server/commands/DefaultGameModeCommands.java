package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("defaultgamemode")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("gamemode", GameModeArgument.gameMode())
                        .executes(param0x -> setMode(param0x.getSource(), GameModeArgument.getGameMode(param0x, "gamemode")))
                )
        );
    }

    private static int setMode(CommandSourceStack param0, GameType param1) {
        int var0 = 0;
        MinecraftServer var1 = param0.getServer();
        var1.setDefaultGameType(param1);
        GameType var2 = var1.getForcedGameType();
        if (var2 != null) {
            for(ServerPlayer var3 : var1.getPlayerList().getPlayers()) {
                if (var3.setGameMode(var2)) {
                    ++var0;
                }
            }
        }

        param0.sendSuccess(() -> Component.translatable("commands.defaultgamemode.success", param1.getLongDisplayName()), true);
        return var0;
    }
}
