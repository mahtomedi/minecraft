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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
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
                                    param0x.getSource(),
                                    getScore(param0x.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(param0x, "objective"))
                                )
                        )
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> addValue(
                                                    param0x.getSource(),
                                                    getScore(param0x.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(param0x, "objective")),
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
                                                    getScore(param0x.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(param0x, "objective")),
                                                    IntegerArgumentType.getInteger(param0x, "value")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack param0, SuggestionsBuilder param1) {
        Entity var0 = param0.getEntity();
        List<String> var1 = Lists.newArrayList();
        if (var0 != null) {
            Scoreboard var2 = param0.getServer().getScoreboard();
            String var3 = var0.getScoreboardName();

            for(Objective var4 : var2.getObjectives()) {
                if (var4.getCriteria() == ObjectiveCriteria.TRIGGER && var2.hasPlayerScore(var3, var4)) {
                    Score var5 = var2.getOrCreatePlayerScore(var3, var4);
                    if (!var5.isLocked()) {
                        var1.add(var4.getName());
                    }
                }
            }
        }

        return SharedSuggestionProvider.suggest(var1, param1);
    }

    private static int addValue(CommandSourceStack param0, Score param1, int param2) {
        param1.add(param2);
        param0.sendSuccess(Component.translatable("commands.trigger.add.success", param1.getObjective().getFormattedDisplayName(), param2), true);
        return param1.getScore();
    }

    private static int setValue(CommandSourceStack param0, Score param1, int param2) {
        param1.setScore(param2);
        param0.sendSuccess(Component.translatable("commands.trigger.set.success", param1.getObjective().getFormattedDisplayName(), param2), true);
        return param2;
    }

    private static int simpleTrigger(CommandSourceStack param0, Score param1) {
        param1.add(1);
        param0.sendSuccess(Component.translatable("commands.trigger.simple.success", param1.getObjective().getFormattedDisplayName()), true);
        return param1.getScore();
    }

    private static Score getScore(ServerPlayer param0, Objective param1) throws CommandSyntaxException {
        if (param1.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_INVALID_OBJECTIVE.create();
        } else {
            Scoreboard var0 = param0.getScoreboard();
            String var1 = param0.getScoreboardName();
            if (!var0.hasPlayerScore(var1, param1)) {
                throw ERROR_NOT_PRIMED.create();
            } else {
                Score var2 = var0.getOrCreatePlayerScore(var1, param1);
                if (var2.isLocked()) {
                    throw ERROR_NOT_PRIMED.create();
                } else {
                    var2.setLocked(true);
                    return var2;
                }
            }
        }
    }
}
