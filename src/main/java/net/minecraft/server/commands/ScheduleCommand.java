package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.schedule.same_tick"));
    private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.schedule.cleared.failure", param0)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = (param0, param1) -> SharedSuggestionProvider.suggest(
            param0.getSource().getLevel().getLevelData().getScheduledEvents().getEventsIds(), param1
        );

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
                                                    IntegerArgumentType.getInteger(param0x, "time"),
                                                    true
                                                )
                                        )
                                        .then(
                                            Commands.literal("append")
                                                .executes(
                                                    param0x -> schedule(
                                                            param0x.getSource(),
                                                            FunctionArgument.getFunctionOrTag(param0x, "function"),
                                                            IntegerArgumentType.getInteger(param0x, "time"),
                                                            false
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("replace")
                                                .executes(
                                                    param0x -> schedule(
                                                            param0x.getSource(),
                                                            FunctionArgument.getFunctionOrTag(param0x, "function"),
                                                            IntegerArgumentType.getInteger(param0x, "time"),
                                                            true
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("clear")
                        .then(
                            Commands.argument("function", StringArgumentType.greedyString())
                                .suggests(SUGGEST_SCHEDULE)
                                .executes(param0x -> remove(param0x.getSource(), StringArgumentType.getString(param0x, "function")))
                        )
                )
        );
    }

    private static int schedule(CommandSourceStack param0, Either<CommandFunction, Tag<CommandFunction>> param1, int param2, boolean param3) throws CommandSyntaxException {
        if (param2 == 0) {
            throw ERROR_SAME_TICK.create();
        } else {
            long var0 = param0.getLevel().getGameTime() + (long)param2;
            TimerQueue<MinecraftServer> var1 = param0.getLevel().getLevelData().getScheduledEvents();
            param1.ifLeft(param5 -> {
                ResourceLocation var0x = param5.getId();
                String var1x = var0x.toString();
                if (param3) {
                    var1.remove(var1x);
                }

                var1.schedule(var1x, var0, new FunctionCallback(var0x));
                param0.sendSuccess(new TranslatableComponent("commands.schedule.created.function", var0x, param2, var0), true);
            }).ifRight(param5 -> {
                ResourceLocation var0x = param5.getId();
                String var1x = "#" + var0x.toString();
                if (param3) {
                    var1.remove(var1x);
                }

                var1.schedule(var1x, var0, new FunctionTagCallback(var0x));
                param0.sendSuccess(new TranslatableComponent("commands.schedule.created.tag", var0x, param2, var0), true);
            });
            return (int)Math.floorMod(var0, 2147483647L);
        }
    }

    private static int remove(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        int var0 = param0.getLevel().getLevelData().getScheduledEvents().remove(param1);
        if (var0 == 0) {
            throw ERROR_CANT_REMOVE.create(param1);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.schedule.cleared.success", var0, param1), true);
            return var0;
        }
    }
}
