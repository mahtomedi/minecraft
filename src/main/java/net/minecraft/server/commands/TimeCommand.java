package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("time")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("set")
                        .then(Commands.literal("day").executes(param0x -> setTime(param0x.getSource(), 1000)))
                        .then(Commands.literal("noon").executes(param0x -> setTime(param0x.getSource(), 6000)))
                        .then(Commands.literal("night").executes(param0x -> setTime(param0x.getSource(), 13000)))
                        .then(Commands.literal("midnight").executes(param0x -> setTime(param0x.getSource(), 18000)))
                        .then(
                            Commands.argument("time", TimeArgument.time())
                                .executes(param0x -> setTime(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "time")))
                        )
                )
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("time", TimeArgument.time())
                                .executes(param0x -> addTime(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "time")))
                        )
                )
                .then(
                    Commands.literal("query")
                        .then(Commands.literal("daytime").executes(param0x -> queryTime(param0x.getSource(), getDayTime(param0x.getSource().getLevel()))))
                        .then(
                            Commands.literal("gametime")
                                .executes(param0x -> queryTime(param0x.getSource(), (int)(param0x.getSource().getLevel().getGameTime() % 2147483647L)))
                        )
                        .then(
                            Commands.literal("day")
                                .executes(param0x -> queryTime(param0x.getSource(), (int)(param0x.getSource().getLevel().getDayTime() / 24000L % 2147483647L)))
                        )
                )
        );
    }

    private static int getDayTime(ServerLevel param0) {
        return (int)(param0.getDayTime() % 24000L);
    }

    private static int queryTime(CommandSourceStack param0, int param1) {
        param0.sendSuccess(Component.translatable("commands.time.query", param1), false);
        return param1;
    }

    public static int setTime(CommandSourceStack param0, int param1) {
        for(ServerLevel var0 : param0.getServer().getAllLevels()) {
            var0.setDayTime((long)param1);
        }

        param0.sendSuccess(Component.translatable("commands.time.set", param1), true);
        return getDayTime(param0.getLevel());
    }

    public static int addTime(CommandSourceStack param0, int param1) {
        for(ServerLevel var0 : param0.getServer().getAllLevels()) {
            var0.setDayTime(var0.getDayTime() + (long)param1);
        }

        int var1 = getDayTime(param0.getLevel());
        param0.sendSuccess(Component.translatable("commands.time.set", var1), true);
        return var1;
    }
}
