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
            var0.then(Commands.literal(var1.getName()).executes(param1 -> setMode(param1.getSource(), var1)));
        }

        param0.register(var0);
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

        param0.sendSuccess(new TranslatableComponent("commands.defaultgamemode.success", param1.getLongDisplayName()), true);
        return var0;
    }
}
