package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.center.failed")
    );
    private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.set.failed.nochange")
    );
    private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.set.failed.small")
    );
    private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.set.failed.big", 5.9999968E7)
    );
    private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7)
    );
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.warning.time.failed")
    );
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.warning.distance.failed")
    );
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.damage.buffer.failed")
    );
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(
        Component.translatable("commands.worldborder.damage.amount.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("worldborder")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7, 5.9999968E7))
                                .executes(
                                    param0x -> setSize(
                                            param0x.getSource(),
                                            param0x.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(param0x, "distance"),
                                            0L
                                        )
                                )
                                .then(
                                    Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(
                                            param0x -> setSize(
                                                    param0x.getSource(),
                                                    param0x.getSource().getLevel().getWorldBorder().getSize()
                                                        + DoubleArgumentType.getDouble(param0x, "distance"),
                                                    param0x.getSource().getLevel().getWorldBorder().getLerpRemainingTime()
                                                        + (long)IntegerArgumentType.getInteger(param0x, "time") * 1000L
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("set")
                        .then(
                            Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7, 5.9999968E7))
                                .executes(param0x -> setSize(param0x.getSource(), DoubleArgumentType.getDouble(param0x, "distance"), 0L))
                                .then(
                                    Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(
                                            param0x -> setSize(
                                                    param0x.getSource(),
                                                    DoubleArgumentType.getDouble(param0x, "distance"),
                                                    (long)IntegerArgumentType.getInteger(param0x, "time") * 1000L
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("center")
                        .then(
                            Commands.argument("pos", Vec2Argument.vec2())
                                .executes(param0x -> setCenter(param0x.getSource(), Vec2Argument.getVec2(param0x, "pos")))
                        )
                )
                .then(
                    Commands.literal("damage")
                        .then(
                            Commands.literal("amount")
                                .then(
                                    Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F))
                                        .executes(param0x -> setDamageAmount(param0x.getSource(), FloatArgumentType.getFloat(param0x, "damagePerBlock")))
                                )
                        )
                        .then(
                            Commands.literal("buffer")
                                .then(
                                    Commands.argument("distance", FloatArgumentType.floatArg(0.0F))
                                        .executes(param0x -> setDamageBuffer(param0x.getSource(), FloatArgumentType.getFloat(param0x, "distance")))
                                )
                        )
                )
                .then(Commands.literal("get").executes(param0x -> getSize(param0x.getSource())))
                .then(
                    Commands.literal("warning")
                        .then(
                            Commands.literal("distance")
                                .then(
                                    Commands.argument("distance", IntegerArgumentType.integer(0))
                                        .executes(param0x -> setWarningDistance(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "distance")))
                                )
                        )
                        .then(
                            Commands.literal("time")
                                .then(
                                    Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(param0x -> setWarningTime(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "time")))
                                )
                        )
                )
        );
    }

    private static int setDamageBuffer(CommandSourceStack param0, float param1) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        if (var0.getDamageSafeZone() == (double)param1) {
            throw ERROR_SAME_DAMAGE_BUFFER.create();
        } else {
            var0.setDamageSafeZone((double)param1);
            param0.sendSuccess(() -> Component.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", param1)), true);
            return (int)param1;
        }
    }

    private static int setDamageAmount(CommandSourceStack param0, float param1) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        if (var0.getDamagePerBlock() == (double)param1) {
            throw ERROR_SAME_DAMAGE_AMOUNT.create();
        } else {
            var0.setDamagePerBlock((double)param1);
            param0.sendSuccess(() -> Component.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", param1)), true);
            return (int)param1;
        }
    }

    private static int setWarningTime(CommandSourceStack param0, int param1) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        if (var0.getWarningTime() == param1) {
            throw ERROR_SAME_WARNING_TIME.create();
        } else {
            var0.setWarningTime(param1);
            param0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.time.success", param1), true);
            return param1;
        }
    }

    private static int setWarningDistance(CommandSourceStack param0, int param1) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        if (var0.getWarningBlocks() == param1) {
            throw ERROR_SAME_WARNING_DISTANCE.create();
        } else {
            var0.setWarningBlocks(param1);
            param0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.distance.success", param1), true);
            return param1;
        }
    }

    private static int getSize(CommandSourceStack param0) {
        double var0 = param0.getServer().overworld().getWorldBorder().getSize();
        param0.sendSuccess(() -> Component.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", var0)), false);
        return Mth.floor(var0 + 0.5);
    }

    private static int setCenter(CommandSourceStack param0, Vec2 param1) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        if (var0.getCenterX() == (double)param1.x && var0.getCenterZ() == (double)param1.y) {
            throw ERROR_SAME_CENTER.create();
        } else if (!((double)Math.abs(param1.x) > 2.9999984E7) && !((double)Math.abs(param1.y) > 2.9999984E7)) {
            var0.setCenter((double)param1.x, (double)param1.y);
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", param1.x), String.format(Locale.ROOT, "%.2f", param1.y)
                    ),
                true
            );
            return 0;
        } else {
            throw ERROR_TOO_FAR_OUT.create();
        }
    }

    private static int setSize(CommandSourceStack param0, double param1, long param2) throws CommandSyntaxException {
        WorldBorder var0 = param0.getServer().overworld().getWorldBorder();
        double var1 = var0.getSize();
        if (var1 == param1) {
            throw ERROR_SAME_SIZE.create();
        } else if (param1 < 1.0) {
            throw ERROR_TOO_SMALL.create();
        } else if (param1 > 5.9999968E7) {
            throw ERROR_TOO_BIG.create();
        } else {
            if (param2 > 0L) {
                var0.lerpSizeBetween(var1, param1, param2);
                if (param1 > var1) {
                    param0.sendSuccess(
                        () -> Component.translatable("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", param1), Long.toString(param2 / 1000L)),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        () -> Component.translatable(
                                "commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", param1), Long.toString(param2 / 1000L)
                            ),
                        true
                    );
                }
            } else {
                var0.setSize(param1);
                param0.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", param1)), true);
            }

            return (int)(param1 - var1);
        }
    }
}
