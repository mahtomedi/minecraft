package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
    public static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("gamemode")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("gamemode", GameModeArgument.gameMode())
                        .executes(
                            param0x -> setMode(
                                    param0x,
                                    Collections.singleton(param0x.getSource().getPlayerOrException()),
                                    GameModeArgument.getGameMode(param0x, "gamemode")
                                )
                        )
                        .then(
                            Commands.argument("target", EntityArgument.players())
                                .executes(
                                    param0x -> setMode(param0x, EntityArgument.getPlayers(param0x, "target"), GameModeArgument.getGameMode(param0x, "gamemode"))
                                )
                        )
                )
        );
    }

    private static void logGamemodeChange(CommandSourceStack param0, ServerPlayer param1, GameType param2) {
        Component var0 = Component.translatable("gameMode." + param2.getName());
        if (param0.getEntity() == param1) {
            param0.sendSuccess(() -> Component.translatable("commands.gamemode.success.self", var0), true);
        } else {
            if (param0.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                param1.sendSystemMessage(Component.translatable("gameMode.changed", var0));
            }

            param0.sendSuccess(() -> Component.translatable("commands.gamemode.success.other", param1.getDisplayName(), var0), true);
        }

    }

    private static int setMode(CommandContext<CommandSourceStack> param0, Collection<ServerPlayer> param1, GameType param2) {
        int var0 = 0;

        for(ServerPlayer var1 : param1) {
            if (var1.setGameMode(param2)) {
                logGamemodeChange(param0.getSource(), var1, param2);
                ++var0;
            }
        }

        return var0;
    }
}
