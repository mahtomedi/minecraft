package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.schedule.same_tick"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("schedule")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("function")
                        .then(
                            Commands.argument("function", FunctionArgument.functions())
                                .suggests(FunctionCommand.SUGGEST_FUNCTION)
                                .then(
                                    Commands.argument("time", TimeArgument.time())
                                        .executes(
                                            param0x -> schedule(
                                                    param0x.getSource(),
                                                    FunctionArgument.getFunctionOrTag(param0x, "function"),
                                                    IntegerArgumentType.getInteger(param0x, "time")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int schedule(CommandSourceStack param0, Either<CommandFunction, Tag<CommandFunction>> param1, int param2) throws CommandSyntaxException {
        if (param2 == 0) {
            throw ERROR_SAME_TICK.create();
        } else {
            long var0 = param0.getLevel().getGameTime() + (long)param2;
            param1.ifLeft(param3 -> {
                ResourceLocation var0x = param3.getId();
                param0.getLevel().getLevelData().getScheduledEvents().reschedule(var0x.toString(), var0, new FunctionCallback(var0x));
                param0.sendSuccess(new TranslatableComponent("commands.schedule.created.function", var0x, param2, var0), true);
            }).ifRight(param3 -> {
                ResourceLocation var0x = param3.getId();
                param0.getLevel().getLevelData().getScheduledEvents().reschedule("#" + var0x.toString(), var0, new FunctionTagCallback(var0x));
                param0.sendSuccess(new TranslatableComponent("commands.schedule.created.tag", var0x, param2, var0), true);
            });
            return (int)Math.floorMod(var0, 2147483647L);
        }
    }
}
