package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
    private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(
        Component.translatable("commands.scoreboard.objectives.add.duplicate")
    );
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(
        Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty")
    );
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(
        Component.translatable("commands.scoreboard.objectives.display.alreadySet")
    );
    private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(
        Component.translatable("commands.scoreboard.players.enable.failed")
    );
    private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(
        Component.translatable("commands.scoreboard.players.enable.invalid")
    );
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("commands.scoreboard.players.get.null", param0, param1)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("scoreboard")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("objectives")
                        .then(Commands.literal("list").executes(param0x -> listObjectives(param0x.getSource())))
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("objective", StringArgumentType.word())
                                        .then(
                                            Commands.argument("criteria", ObjectiveCriteriaArgument.criteria())
                                                .executes(
                                                    param0x -> addObjective(
                                                            param0x.getSource(),
                                                            StringArgumentType.getString(param0x, "objective"),
                                                            ObjectiveCriteriaArgument.getCriteria(param0x, "criteria"),
                                                            Component.literal(StringArgumentType.getString(param0x, "objective"))
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("displayName", ComponentArgument.textComponent())
                                                        .executes(
                                                            param0x -> addObjective(
                                                                    param0x.getSource(),
                                                                    StringArgumentType.getString(param0x, "objective"),
                                                                    ObjectiveCriteriaArgument.getCriteria(param0x, "criteria"),
                                                                    ComponentArgument.getComponent(param0x, "displayName")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("modify")
                                .then(
                                    Commands.argument("objective", ObjectiveArgument.objective())
                                        .then(
                                            Commands.literal("displayname")
                                                .then(
                                                    Commands.argument("displayName", ComponentArgument.textComponent())
                                                        .executes(
                                                            param0x -> setDisplayName(
                                                                    param0x.getSource(),
                                                                    ObjectiveArgument.getObjective(param0x, "objective"),
                                                                    ComponentArgument.getComponent(param0x, "displayName")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(createRenderTypeModify())
                                        .then(
                                            Commands.literal("displayautoupdate")
                                                .then(
                                                    Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(
                                                            param0x -> setDisplayAutoUpdate(
                                                                    param0x.getSource(),
                                                                    ObjectiveArgument.getObjective(param0x, "objective"),
                                                                    BoolArgumentType.getBool(param0x, "value")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            addNumberFormats(
                                                Commands.literal("numberformat"),
                                                (param0x, param1) -> setObjectiveFormat(
                                                        param0x.getSource(), ObjectiveArgument.getObjective(param0x, "objective"), param1
                                                    )
                                            )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("objective", ObjectiveArgument.objective())
                                        .executes(param0x -> removeObjective(param0x.getSource(), ObjectiveArgument.getObjective(param0x, "objective")))
                                )
                        )
                        .then(
                            Commands.literal("setdisplay")
                                .then(
                                    Commands.argument("slot", ScoreboardSlotArgument.displaySlot())
                                        .executes(param0x -> clearDisplaySlot(param0x.getSource(), ScoreboardSlotArgument.getDisplaySlot(param0x, "slot")))
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .executes(
                                                    param0x -> setDisplaySlot(
                                                            param0x.getSource(),
                                                            ScoreboardSlotArgument.getDisplaySlot(param0x, "slot"),
                                                            ObjectiveArgument.getObjective(param0x, "objective")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("players")
                        .then(
                            Commands.literal("list")
                                .executes(param0x -> listTrackedPlayers(param0x.getSource()))
                                .then(
                                    Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .executes(param0x -> listTrackedPlayerScores(param0x.getSource(), ScoreHolderArgument.getName(param0x, "target")))
                                )
                        )
                        .then(
                            Commands.literal("set")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .then(
                                                    Commands.argument("score", IntegerArgumentType.integer())
                                                        .executes(
                                                            param0x -> setScore(
                                                                    param0x.getSource(),
                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                    ObjectiveArgument.getWritableObjective(param0x, "objective"),
                                                                    IntegerArgumentType.getInteger(param0x, "score")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("get")
                                .then(
                                    Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .executes(
                                                    param0x -> getScore(
                                                            param0x.getSource(),
                                                            ScoreHolderArgument.getName(param0x, "target"),
                                                            ObjectiveArgument.getObjective(param0x, "objective")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .then(
                                                    Commands.argument("score", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            param0x -> addScore(
                                                                    param0x.getSource(),
                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                    ObjectiveArgument.getWritableObjective(param0x, "objective"),
                                                                    IntegerArgumentType.getInteger(param0x, "score")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .then(
                                                    Commands.argument("score", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            param0x -> removeScore(
                                                                    param0x.getSource(),
                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                    ObjectiveArgument.getWritableObjective(param0x, "objective"),
                                                                    IntegerArgumentType.getInteger(param0x, "score")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("reset")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .executes(
                                            param0x -> resetScores(param0x.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"))
                                        )
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .executes(
                                                    param0x -> resetScore(
                                                            param0x.getSource(),
                                                            ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                            ObjectiveArgument.getObjective(param0x, "objective")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("enable")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("objective", ObjectiveArgument.objective())
                                                .suggests(
                                                    (param0x, param1) -> suggestTriggers(
                                                            param0x.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"), param1
                                                        )
                                                )
                                                .executes(
                                                    param0x -> enableTrigger(
                                                            param0x.getSource(),
                                                            ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                            ObjectiveArgument.getObjective(param0x, "objective")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("display")
                                .then(
                                    Commands.literal("name")
                                        .then(
                                            Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(
                                                    Commands.argument("objective", ObjectiveArgument.objective())
                                                        .then(
                                                            Commands.argument("name", ComponentArgument.textComponent())
                                                                .executes(
                                                                    param0x -> setScoreDisplay(
                                                                            param0x.getSource(),
                                                                            ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                            ObjectiveArgument.getObjective(param0x, "objective"),
                                                                            ComponentArgument.getComponent(param0x, "name")
                                                                        )
                                                                )
                                                        )
                                                        .executes(
                                                            param0x -> setScoreDisplay(
                                                                    param0x.getSource(),
                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                    ObjectiveArgument.getObjective(param0x, "objective"),
                                                                    null
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("numberformat")
                                        .then(
                                            Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(
                                                    addNumberFormats(
                                                        Commands.argument("objective", ObjectiveArgument.objective()),
                                                        (param0x, param1) -> setScoreNumberFormat(
                                                                param0x.getSource(),
                                                                ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                ObjectiveArgument.getObjective(param0x, "objective"),
                                                                param1
                                                            )
                                                    )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("operation")
                                .then(
                                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .then(
                                            Commands.argument("targetObjective", ObjectiveArgument.objective())
                                                .then(
                                                    Commands.argument("operation", OperationArgument.operation())
                                                        .then(
                                                            Commands.argument("source", ScoreHolderArgument.scoreHolders())
                                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                                .then(
                                                                    Commands.argument("sourceObjective", ObjectiveArgument.objective())
                                                                        .executes(
                                                                            param0x -> performOperation(
                                                                                    param0x.getSource(),
                                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "targets"),
                                                                                    ObjectiveArgument.getWritableObjective(param0x, "targetObjective"),
                                                                                    OperationArgument.getOperation(param0x, "operation"),
                                                                                    ScoreHolderArgument.getNamesWithDefaultWildcard(param0x, "source"),
                                                                                    ObjectiveArgument.getObjective(param0x, "sourceObjective")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addNumberFormats(
        ArgumentBuilder<CommandSourceStack, ?> param0, ScoreboardCommand.NumberFormatCommandExecutor param1
    ) {
        return param0.then(Commands.literal("blank").executes(param1x -> param1.run(param1x, BlankFormat.INSTANCE)))
            .then(Commands.literal("fixed").then(Commands.argument("contents", ComponentArgument.textComponent()).executes(param1x -> {
                Component var0x = ComponentArgument.getComponent(param1x, "contents");
                return param1.run(param1x, new FixedFormat(var0x));
            })))
            .then(Commands.literal("styled").then(Commands.argument("style", StyleArgument.style()).executes(param1x -> {
                Style var0x = StyleArgument.getStyle(param1x, "style");
                return param1.run(param1x, new StyledFormat(var0x));
            })))
            .executes(param1x -> param1.run(param1x, null));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("rendertype");

        for(ObjectiveCriteria.RenderType var1 : ObjectiveCriteria.RenderType.values()) {
            var0.then(
                Commands.literal(var1.getId()).executes(param1 -> setRenderType(param1.getSource(), ObjectiveArgument.getObjective(param1, "objective"), var1))
            );
        }

        return var0;
    }

    private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack param0, Collection<ScoreHolder> param1, SuggestionsBuilder param2) {
        List<String> var0 = Lists.newArrayList();
        Scoreboard var1 = param0.getServer().getScoreboard();

        for(Objective var2 : var1.getObjectives()) {
            if (var2.getCriteria() == ObjectiveCriteria.TRIGGER) {
                boolean var3 = false;

                for(ScoreHolder var4 : param1) {
                    ReadOnlyScoreInfo var5 = var1.getPlayerScoreInfo(var4, var2);
                    if (var5 == null || var5.isLocked()) {
                        var3 = true;
                        break;
                    }
                }

                if (var3) {
                    var0.add(var2.getName());
                }
            }
        }

        return SharedSuggestionProvider.suggest(var0, param2);
    }

    private static int getScore(CommandSourceStack param0, ScoreHolder param1, Objective param2) throws CommandSyntaxException {
        Scoreboard var0 = param0.getServer().getScoreboard();
        ReadOnlyScoreInfo var1 = var0.getPlayerScoreInfo(param1, param2);
        if (var1 == null) {
            throw ERROR_NO_VALUE.create(param2.getName(), param1.getFeedbackDisplayName());
        } else {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.get.success", param1.getFeedbackDisplayName(), var1.value(), param2.getFormattedDisplayName()
                    ),
                false
            );
            return var1.value();
        }
    }

    private static Component getFirstTargetName(Collection<ScoreHolder> param0) {
        return param0.iterator().next().getFeedbackDisplayName();
    }

    private static int performOperation(
        CommandSourceStack param0,
        Collection<ScoreHolder> param1,
        Objective param2,
        OperationArgument.Operation param3,
        Collection<ScoreHolder> param4,
        Objective param5
    ) throws CommandSyntaxException {
        Scoreboard var0 = param0.getServer().getScoreboard();
        int var1 = 0;

        for(ScoreHolder var2 : param1) {
            ScoreAccess var3 = var0.getOrCreatePlayerScore(var2, param2);

            for(ScoreHolder var4 : param4) {
                ScoreAccess var5 = var0.getOrCreatePlayerScore(var4, param5);
                param3.apply(var3, var5);
            }

            var1 += var3.get();
        }

        if (param1.size() == 1) {
            int var6 = var1;
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.operation.success.single", param2.getFormattedDisplayName(), getFirstTargetName(param1), var6
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.operation.success.multiple", param2.getFormattedDisplayName(), param1.size()), true
            );
        }

        return var1;
    }

    private static int enableTrigger(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2) throws CommandSyntaxException {
        if (param2.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_NOT_TRIGGER.create();
        } else {
            Scoreboard var0 = param0.getServer().getScoreboard();
            int var1 = 0;

            for(ScoreHolder var2 : param1) {
                ScoreAccess var3 = var0.getOrCreatePlayerScore(var2, param2);
                if (var3.locked()) {
                    var3.unlock();
                    ++var1;
                }
            }

            if (var1 == 0) {
                throw ERROR_TRIGGER_ALREADY_ENABLED.create();
            } else {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        () -> Component.translatable(
                                "commands.scoreboard.players.enable.success.single", param2.getFormattedDisplayName(), getFirstTargetName(param1)
                            ),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        () -> Component.translatable("commands.scoreboard.players.enable.success.multiple", param2.getFormattedDisplayName(), param1.size()),
                        true
                    );
                }

                return var1;
            }
        }
    }

    private static int resetScores(CommandSourceStack param0, Collection<ScoreHolder> param1) {
        Scoreboard var0 = param0.getServer().getScoreboard();

        for(ScoreHolder var1 : param1) {
            var0.resetAllPlayerScores(var1);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", getFirstTargetName(param1)), true);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", param1.size()), true);
        }

        return param1.size();
    }

    private static int resetScore(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2) {
        Scoreboard var0 = param0.getServer().getScoreboard();

        for(ScoreHolder var1 : param1) {
            var0.resetSinglePlayerScore(var1, param2);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.reset.specific.single", param2.getFormattedDisplayName(), getFirstTargetName(param1)),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", param2.getFormattedDisplayName(), param1.size()), true
            );
        }

        return param1.size();
    }

    private static int setScore(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, int param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();

        for(ScoreHolder var1 : param1) {
            var0.getOrCreatePlayerScore(var1, param2).set(param3);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.set.success.single", param2.getFormattedDisplayName(), getFirstTargetName(param1), param3
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.set.success.multiple", param2.getFormattedDisplayName(), param1.size(), param3), true
            );
        }

        return param3 * param1.size();
    }

    private static int setScoreDisplay(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, @Nullable Component param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();

        for(ScoreHolder var1 : param1) {
            var0.getOrCreatePlayerScore(var1, param2).display(param3);
        }

        if (param3 == null) {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.players.display.name.clear.success.single", getFirstTargetName(param1), param2.getFormattedDisplayName()
                        ),
                    true
                );
            } else {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.players.display.name.clear.success.multiple", param1.size(), param2.getFormattedDisplayName()
                        ),
                    true
                );
            }
        } else if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.display.name.set.success.single", param3, getFirstTargetName(param1), param2.getFormattedDisplayName()
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.display.name.set.success.multiple", param3, param1.size(), param2.getFormattedDisplayName()
                    ),
                true
            );
        }

        return param1.size();
    }

    private static int setScoreNumberFormat(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, @Nullable NumberFormat param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();

        for(ScoreHolder var1 : param1) {
            var0.getOrCreatePlayerScore(var1, param2).numberFormatOverride(param3);
        }

        if (param3 == null) {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.players.display.numberFormat.clear.success.single",
                            getFirstTargetName(param1),
                            param2.getFormattedDisplayName()
                        ),
                    true
                );
            } else {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.players.display.numberFormat.clear.success.multiple", param1.size(), param2.getFormattedDisplayName()
                        ),
                    true
                );
            }
        } else if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.display.numberFormat.set.success.single", getFirstTargetName(param1), param2.getFormattedDisplayName()
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.display.numberFormat.set.success.multiple", param1.size(), param2.getFormattedDisplayName()
                    ),
                true
            );
        }

        return param1.size();
    }

    private static int addScore(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, int param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();
        int var1 = 0;

        for(ScoreHolder var2 : param1) {
            ScoreAccess var3 = var0.getOrCreatePlayerScore(var2, param2);
            var3.set(var3.get() + param3);
            var1 += var3.get();
        }

        if (param1.size() == 1) {
            int var4 = var1;
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.add.success.single", param3, param2.getFormattedDisplayName(), getFirstTargetName(param1), var4
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.add.success.multiple", param3, param2.getFormattedDisplayName(), param1.size()), true
            );
        }

        return var1;
    }

    private static int removeScore(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, int param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();
        int var1 = 0;

        for(ScoreHolder var2 : param1) {
            ScoreAccess var3 = var0.getOrCreatePlayerScore(var2, param2);
            var3.set(var3.get() - param3);
            var1 += var3.get();
        }

        if (param1.size() == 1) {
            int var4 = var1;
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.remove.success.single", param3, param2.getFormattedDisplayName(), getFirstTargetName(param1), var4
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.remove.success.multiple", param3, param2.getFormattedDisplayName(), param1.size()),
                true
            );
        }

        return var1;
    }

    private static int listTrackedPlayers(CommandSourceStack param0) {
        Collection<ScoreHolder> var0 = param0.getServer().getScoreboard().getTrackedPlayers();
        if (var0.isEmpty()) {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
        } else {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.players.list.success", var0.size(), ComponentUtils.formatList(var0, ScoreHolder::getFeedbackDisplayName)
                    ),
                false
            );
        }

        return var0.size();
    }

    private static int listTrackedPlayerScores(CommandSourceStack param0, ScoreHolder param1) {
        Object2IntMap<Objective> var0 = param0.getServer().getScoreboard().listPlayerScores(param1);
        if (var0.isEmpty()) {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", param1.getFeedbackDisplayName()), false);
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.players.list.entity.success", param1.getFeedbackDisplayName(), var0.size()), false
            );
            Object2IntMaps.fastForEach(
                var0,
                param1x -> param0.sendSuccess(
                        () -> Component.translatable(
                                "commands.scoreboard.players.list.entity.entry", ((Objective)param1x.getKey()).getFormattedDisplayName(), param1x.getIntValue()
                            ),
                        false
                    )
            );
        }

        return var0.size();
    }

    private static int clearDisplaySlot(CommandSourceStack param0, DisplaySlot param1) throws CommandSyntaxException {
        Scoreboard var0 = param0.getServer().getScoreboard();
        if (var0.getDisplayObjective(param1) == null) {
            throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
        } else {
            var0.setDisplayObjective(param1, null);
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", param1.getSerializedName()), true);
            return 0;
        }
    }

    private static int setDisplaySlot(CommandSourceStack param0, DisplaySlot param1, Objective param2) throws CommandSyntaxException {
        Scoreboard var0 = param0.getServer().getScoreboard();
        if (var0.getDisplayObjective(param1) == param2) {
            throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
        } else {
            var0.setDisplayObjective(param1, param2);
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.objectives.display.set", param1.getSerializedName(), param2.getDisplayName()), true
            );
            return 0;
        }
    }

    private static int setDisplayName(CommandSourceStack param0, Objective param1, Component param2) {
        if (!param1.getDisplayName().equals(param2)) {
            param1.setDisplayName(param2);
            param0.sendSuccess(
                () -> Component.translatable("commands.scoreboard.objectives.modify.displayname", param1.getName(), param1.getFormattedDisplayName()), true
            );
        }

        return 0;
    }

    private static int setDisplayAutoUpdate(CommandSourceStack param0, Objective param1, boolean param2) {
        if (param1.displayAutoUpdate() != param2) {
            param1.setDisplayAutoUpdate(param2);
            if (param2) {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.objectives.modify.displayAutoUpdate.enable", param1.getName(), param1.getFormattedDisplayName()
                        ),
                    true
                );
            } else {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.scoreboard.objectives.modify.displayAutoUpdate.disable", param1.getName(), param1.getFormattedDisplayName()
                        ),
                    true
                );
            }
        }

        return 0;
    }

    private static int setObjectiveFormat(CommandSourceStack param0, Objective param1, @Nullable NumberFormat param2) {
        param1.setNumberFormat(param2);
        if (param2 != null) {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", param1.getName()), true);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", param1.getName()), true);
        }

        return 0;
    }

    private static int setRenderType(CommandSourceStack param0, Objective param1, ObjectiveCriteria.RenderType param2) {
        if (param1.getRenderType() != param2) {
            param1.setRenderType(param2);
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", param1.getFormattedDisplayName()), true);
        }

        return 0;
    }

    private static int removeObjective(CommandSourceStack param0, Objective param1) {
        Scoreboard var0 = param0.getServer().getScoreboard();
        var0.removeObjective(param1);
        param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", param1.getFormattedDisplayName()), true);
        return var0.getObjectives().size();
    }

    private static int addObjective(CommandSourceStack param0, String param1, ObjectiveCriteria param2, Component param3) throws CommandSyntaxException {
        Scoreboard var0 = param0.getServer().getScoreboard();
        if (var0.getObjective(param1) != null) {
            throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
        } else {
            var0.addObjective(param1, param2, param3, param2.getDefaultRenderType(), false, null);
            Objective var1 = var0.getObjective(param1);
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", var1.getFormattedDisplayName()), true);
            return var0.getObjectives().size();
        }
    }

    private static int listObjectives(CommandSourceStack param0) {
        Collection<Objective> var0 = param0.getServer().getScoreboard().getObjectives();
        if (var0.isEmpty()) {
            param0.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
        } else {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.scoreboard.objectives.list.success", var0.size(), ComponentUtils.formatList(var0, Objective::getFormattedDisplayName)
                    ),
                false
            );
        }

        return var0.size();
    }

    @FunctionalInterface
    public interface NumberFormatCommandExecutor {
        int run(CommandContext<CommandSourceStack> var1, @Nullable NumberFormat var2) throws CommandSyntaxException;
    }
}
