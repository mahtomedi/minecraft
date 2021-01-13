package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("defaultgamemode").requires(param0x -> param0x.hasPermission(2));

        for(GameType var1 : GameType.values()) {
            if (var1 != GameType.NOT_SET) {
                var0.then(Commands.literal(var1.getName()).executes(param1 -> setMode(param1.getSource(), var1)));
            }
        }

        param0.register(var0);
    }

    private static int setMode(CommandSourceStack param0, GameType param1) {
        int var0 = 0;
        MinecraftServer var1 = param0.getServer();
        var1.setDefaultGameType(param1);
        if (var1.getForceGameType()) {
            for(ServerPlayer var2 : var1.getPlayerList().getPlayers()) {
                if (var2.gameMode.getGameModeForPlayer() != param1) {
                    var2.setGameMode(param1);
                    ++var0;
                }
            }
        }

        param0.sendSuccess(new TranslatableComponent("commands.defaultgamemode.success", param1.getDisplayName()), true);
        return var0;
    }
}
