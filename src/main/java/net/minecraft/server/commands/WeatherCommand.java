package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WeatherCommand {
    private static final int DEFAULT_TIME = 6000;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("weather")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .executes(param0x -> setClear(param0x.getSource(), 6000))
                        .then(
                            Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
                                .executes(param0x -> setClear(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration") * 20))
                        )
                )
                .then(
                    Commands.literal("rain")
                        .executes(param0x -> setRain(param0x.getSource(), 6000))
                        .then(
                            Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
                                .executes(param0x -> setRain(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration") * 20))
                        )
                )
                .then(
                    Commands.literal("thunder")
                        .executes(param0x -> setThunder(param0x.getSource(), 6000))
                        .then(
                            Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
                                .executes(param0x -> setThunder(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "duration") * 20))
                        )
                )
        );
    }

    private static int setClear(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(param1, 0, false, false);
        param0.sendSuccess(Component.translatable("commands.weather.set.clear"), true);
        return param1;
    }

    private static int setRain(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(0, param1, true, false);
        param0.sendSuccess(Component.translatable("commands.weather.set.rain"), true);
        return param1;
    }

    private static int setThunder(CommandSourceStack param0, int param1) {
        param0.getLevel().setWeatherParameters(0, param1, true, true);
        param0.sendSuccess(Component.translatable("commands.weather.set.thunder"), true);
        return param1;
    }
}
