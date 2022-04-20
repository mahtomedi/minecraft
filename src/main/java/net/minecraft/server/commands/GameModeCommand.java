package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
    public static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("gamemode").requires(param0x -> param0x.hasPermission(2));

        for(GameType var1 : GameType.values()) {
            var0.then(
                Commands.literal(var1.getName())
                    .executes(param1 -> setMode(param1, Collections.singleton(param1.getSource().getPlayerOrException()), var1))
                    .then(
                        Commands.argument("target", EntityArgument.players())
                            .executes(param1 -> setMode(param1, EntityArgument.getPlayers(param1, "target"), var1))
                    )
            );
        }

        param0.register(var0);
    }

    private static void logGamemodeChange(CommandSourceStack param0, ServerPlayer param1, GameType param2) {
        Component var0 = Component.translatable("gameMode." + param2.getName());
        if (param0.getEntity() == param1) {
            param0.sendSuccess(Component.translatable("commands.gamemode.success.self", var0), true);
        } else {
            if (param0.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                param1.sendMessage(Component.translatable("gameMode.changed", var0), Util.NIL_UUID);
            }

            param0.sendSuccess(Component.translatable("commands.gamemode.success.other", param1.getDisplayName(), var0), true);
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
