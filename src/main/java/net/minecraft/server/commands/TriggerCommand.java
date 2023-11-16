package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(
        Component.translatable("commands.trigger.failed.unprimed")
    );
    private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(
        Component.translatable("commands.trigger.failed.invalid")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("trigger")
                .then(
                    Commands.argument("objective", ObjectiveArgument.objective())
                        .suggests((param0x, param1) -> suggestObjectives(param0x.getSource(), param1))
                        .executes(
                            param0x -> simpleTrigger(
                                    param0x.getSource(), param0x.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(param0x, "objective")
                                )
                        )
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> addValue(
                                                    param0x.getSource(),
                                                    param0x.getSource().getPlayerOrException(),
                                                    ObjectiveArgument.getObjective(param0x, "objective"),
                                                    IntegerArgumentType.getInteger(param0x, "value")
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("set")
                                .then(
                                    Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> setValue(
                                                    param0x.getSource(),
                                                    param0x.getSource().getPlayerOrException(),
                                                    ObjectiveArgument.getObjective(param0x, "objective"),
                                                    IntegerArgumentType.getInteger(param0x, "value")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack param0, SuggestionsBuilder param1) {
        ScoreHolder var0 = param0.getEntity();
        List<String> var1 = Lists.newArrayList();
        if (var0 != null) {
            Scoreboard var2 = param0.getServer().getScoreboard();

            for(Objective var3 : var2.getObjectives()) {
                if (var3.getCriteria() == ObjectiveCriteria.TRIGGER) {
                    ReadOnlyScoreInfo var4 = var2.getPlayerScoreInfo(var0, var3);
                    if (var4 != null && !var4.isLocked()) {
                        var1.add(var3.getName());
                    }
                }
            }
        }

        return SharedSuggestionProvider.suggest(var1, param1);
    }

    private static int addValue(CommandSourceStack param0, ServerPlayer param1, Objective param2, int param3) throws CommandSyntaxException {
        ScoreAccess var0 = getScore(param0.getServer().getScoreboard(), param1, param2);
        int var1 = var0.add(param3);
        param0.sendSuccess(() -> Component.translatable("commands.trigger.add.success", param2.getFormattedDisplayName(), param3), true);
        return var1;
    }

    private static int setValue(CommandSourceStack param0, ServerPlayer param1, Objective param2, int param3) throws CommandSyntaxException {
        ScoreAccess var0 = getScore(param0.getServer().getScoreboard(), param1, param2);
        var0.set(param3);
        param0.sendSuccess(() -> Component.translatable("commands.trigger.set.success", param2.getFormattedDisplayName(), param3), true);
        return param3;
    }

    private static int simpleTrigger(CommandSourceStack param0, ServerPlayer param1, Objective param2) throws CommandSyntaxException {
        ScoreAccess var0 = getScore(param0.getServer().getScoreboard(), param1, param2);
        int var1 = var0.add(1);
        param0.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", param2.getFormattedDisplayName()), true);
        return var1;
    }

    private static ScoreAccess getScore(Scoreboard param0, ScoreHolder param1, Objective param2) throws CommandSyntaxException {
        if (param2.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_INVALID_OBJECTIVE.create();
        } else {
            ReadOnlyScoreInfo var0 = param0.getPlayerScoreInfo(param1, param2);
            if (var0 != null && !var0.isLocked()) {
                ScoreAccess var1 = param0.getOrCreatePlayerScore(param1, param2);
                var1.lock();
                return var1;
            } else {
                throw ERROR_NOT_PRIMED.create();
            }
        }
    }
}
