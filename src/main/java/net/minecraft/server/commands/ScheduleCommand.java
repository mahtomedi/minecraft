package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(Component.translatable("commands.schedule.same_tick"));
    private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.schedule.cleared.failure", param0)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = (param0, param1) -> SharedSuggestionProvider.suggest(
            param0.getSource().getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), param1
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

    private static int schedule(
        CommandSourceStack param0,
        Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> param1,
        int param2,
        boolean param3
    ) throws CommandSyntaxException {
        if (param2 == 0) {
            throw ERROR_SAME_TICK.create();
        } else {
            long var0 = param0.getLevel().getGameTime() + (long)param2;
            ResourceLocation var1 = param1.getFirst();
            TimerQueue<MinecraftServer> var2 = param0.getServer().getWorldData().overworldData().getScheduledEvents();
            param1.getSecond().ifLeft(param6 -> {
                String var0x = var1.toString();
                if (param3) {
                    var2.remove(var0x);
                }

                var2.schedule(var0x, var0, new FunctionCallback(var1));
                param0.sendSuccess(() -> Component.translatable("commands.schedule.created.function", Component.translationArg(var1), param2, var0), true);
            }).ifRight(param6 -> {
                String var0x = "#" + var1;
                if (param3) {
                    var2.remove(var0x);
                }

                var2.schedule(var0x, var0, new FunctionTagCallback(var1));
                param0.sendSuccess(() -> Component.translatable("commands.schedule.created.tag", Component.translationArg(var1), param2, var0), true);
            });
            return Math.floorMod(var0, Integer.MAX_VALUE);
        }
    }

    private static int remove(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        int var0 = param0.getServer().getWorldData().overworldData().getScheduledEvents().remove(param1);
        if (var0 == 0) {
            throw ERROR_CANT_REMOVE.create(param1);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.schedule.cleared.success", var0, param1), true);
            return var0;
        }
    }
}
