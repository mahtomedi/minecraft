package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeUtil;

public class TickCommand {
    private static final float MAX_TICKRATE = 10000.0F;
    private static final String DEFAULT_TICKRATE = String.valueOf(20);

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("tick")
                .requires(param0x -> param0x.hasPermission(3))
                .then(Commands.literal("query").executes(param0x -> tickQuery(param0x.getSource())))
                .then(
                    Commands.literal("rate")
                        .then(
                            Commands.argument("rate", FloatArgumentType.floatArg(1.0F, 10000.0F))
                                .suggests((param0x, param1) -> SharedSuggestionProvider.suggest(new String[]{DEFAULT_TICKRATE}, param1))
                                .executes(param0x -> setTickingRate(param0x.getSource(), FloatArgumentType.getFloat(param0x, "rate")))
                        )
                )
                .then(
                    Commands.literal("step")
                        .executes(param0x -> step(param0x.getSource(), 1))
                        .then(Commands.literal("stop").executes(param0x -> stopStepping(param0x.getSource())))
                        .then(
                            Commands.argument("time", TimeArgument.time(1))
                                .suggests((param0x, param1) -> SharedSuggestionProvider.suggest(new String[]{"1t", "1s"}, param1))
                                .executes(param0x -> step(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "time")))
                        )
                )
                .then(
                    Commands.literal("sprint")
                        .then(Commands.literal("stop").executes(param0x -> stopSprinting(param0x.getSource())))
                        .then(
                            Commands.argument("time", TimeArgument.time(1))
                                .suggests((param0x, param1) -> SharedSuggestionProvider.suggest(new String[]{"60s", "1d", "3d"}, param1))
                                .executes(param0x -> sprint(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "time")))
                        )
                )
                .then(Commands.literal("unfreeze").executes(param0x -> setFreeze(param0x.getSource(), false)))
                .then(Commands.literal("freeze").executes(param0x -> setFreeze(param0x.getSource(), true)))
        );
    }

    private static String nanosToMilisString(long param0) {
        return String.format("%.1f", (float)param0 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND);
    }

    private static int setTickingRate(CommandSourceStack param0, float param1) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        var0.setTickRate(param1);
        String var1 = String.format("%.1f", param1);
        param0.sendSuccess(() -> Component.translatable("commands.tick.rate.success", var1), true);
        return (int)param1;
    }

    private static int tickQuery(CommandSourceStack param0) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        String var1 = nanosToMilisString(param0.getServer().getAverageTickTimeNanos());
        float var2 = var0.tickrate();
        String var3 = String.format("%.1f", var2);
        if (var0.isSprinting()) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), false);
            param0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.sprinting", var3, var1), false);
        } else {
            if (var0.isFrozen()) {
                param0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), false);
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), false);
            }

            String var4 = nanosToMilisString(var0.nanosecondsPerTick());
            param0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.running", var3, var1, var4), false);
        }

        long[] var5 = Arrays.copyOf(param0.getServer().getTickTimesNanos(), param0.getServer().getTickTimesNanos().length);
        Arrays.sort(var5);
        String var6 = nanosToMilisString(var5[var5.length / 2]);
        String var7 = nanosToMilisString(var5[(int)((double)var5.length * 0.95)]);
        String var8 = nanosToMilisString(var5[(int)((double)var5.length * 0.99)]);
        param0.sendSuccess(() -> Component.translatable("commands.tick.query.percentiles", var6, var7, var8, var5.length), false);
        return (int)var2;
    }

    private static int sprint(CommandSourceStack param0, int param1) {
        boolean var0 = param0.getServer().tickRateManager().requestGameToSprint(param1);
        if (var0) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
        }

        param0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), true);
        return 1;
    }

    private static int setFreeze(CommandSourceStack param0, boolean param1) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        if (param1) {
            if (var0.isSprinting()) {
                var0.stopSprinting();
            }

            if (var0.isSteppingForward()) {
                var0.stopStepping();
            }
        }

        var0.setFrozen(param1);
        if (param1) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), true);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), true);
        }

        return param1 ? 1 : 0;
    }

    private static int step(CommandSourceStack param0, int param1) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        boolean var1 = var0.stepGameIfPaused(param1);
        if (var1) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.step.success", param1), true);
        } else {
            param0.sendFailure(Component.translatable("commands.tick.step.fail"));
        }

        return 1;
    }

    private static int stopStepping(CommandSourceStack param0) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        boolean var1 = var0.stopStepping();
        if (var1) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.step.stop.success"), true);
            return 1;
        } else {
            param0.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
            return 0;
        }
    }

    private static int stopSprinting(CommandSourceStack param0) {
        ServerTickRateManager var0 = param0.getServer().tickRateManager();
        boolean var1 = var0.stopSprinting();
        if (var1) {
            param0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
            return 1;
        } else {
            param0.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
            return 0;
        }
    }
}
