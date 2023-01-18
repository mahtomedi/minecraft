package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
    private static final int DEFAULT_TIME = -1;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("weather")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .executes(param0x -> setClear(param0x.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(param0x -> setClear(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration")))
                        )
                )
                .then(
                    Commands.literal("rain")
                        .executes(param0x -> setRain(param0x.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(param0x -> setRain(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration")))
                        )
                )
                .then(
                    Commands.literal("thunder")
                        .executes(param0x -> setThunder(param0x.getSource(), -1))
                        .then(
                            Commands.argument("duration", TimeArgument.time(1))
                                .executes(param0x -> setThunder(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration")))
                        )
                )
        );
    }

    private static int getDuration(CommandSourceStack param0, int param1, IntProvider param2) {
        return param1 == -1 ? param2.sample(param0.getLevel().getRandom()) : param1;
    }

    private static int setClear(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(getDuration(param0, param1, ServerLevel.RAIN_DELAY), 0, false, false);
        param0.sendSuccess(Component.translatable("commands.weather.set.clear"), true);
        return param1;
    }

    private static int setRain(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(0, getDuration(param0, param1, ServerLevel.RAIN_DURATION), true, false);
        param0.sendSuccess(Component.translatable("commands.weather.set.rain"), true);
        return param1;
    }

    private static int setThunder(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(0, getDuration(param0, param1, ServerLevel.THUNDER_DURATION), true, true);
        param0.sendSuccess(Component.translatable("commands.weather.set.thunder"), true);
        return param1;
    }
}
