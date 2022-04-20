package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("warden_spawn_tracker")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .executes(param0x -> resetTracker(param0x.getSource(), ImmutableList.of(param0x.getSource().getPlayerOrException())))
                )
                .then(
                    Commands.literal("set")
                        .then(
                            Commands.argument("warning_level", IntegerArgumentType.integer(0, 3))
                                .executes(
                                    param0x -> setWarningLevel(
                                            param0x.getSource(),
                                            ImmutableList.of(param0x.getSource().getPlayerOrException()),
                                            IntegerArgumentType.getInteger(param0x, "warning_level")
                                        )
                                )
                        )
                )
        );
    }

    private static int setWarningLevel(CommandSourceStack param0, Collection<? extends Player> param1, int param2) {
        for(Player var0 : param1) {
            var0.getWardenSpawnTracker().setWarningLevel(param2);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.warden_spawn_tracker.set.success.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.warden_spawn_tracker.set.success.multiple", param1.size()), true);
        }

        return param1.size();
    }

    private static int resetTracker(CommandSourceStack param0, Collection<? extends Player> param1) {
        for(Player var0 : param1) {
            var0.getWardenSpawnTracker().reset();
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.warden_spawn_tracker.clear.success.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", param1.size()), true);
        }

        return param1.size();
    }
}
