package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;

public class RandomCommand {
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType(
        Component.translatable("commands.random.error.range_too_large")
    );
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType(
        Component.translatable("commands.random.error.range_too_small")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("random")
                .then(drawRandomValueTree("value", false))
                .then(drawRandomValueTree("roll", true))
                .then(
                    Commands.literal("reset")
                        .requires(param0x -> param0x.hasPermission(2))
                        .then(
                            Commands.literal("*")
                                .executes(param0x -> resetAllSequences(param0x.getSource()))
                                .then(
                                    Commands.argument("seed", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> resetAllSequencesAndSetNewDefaults(
                                                    param0x.getSource(), IntegerArgumentType.getInteger(param0x, "seed"), true, true
                                                )
                                        )
                                        .then(
                                            Commands.argument("includeWorldSeed", BoolArgumentType.bool())
                                                .executes(
                                                    param0x -> resetAllSequencesAndSetNewDefaults(
                                                            param0x.getSource(),
                                                            IntegerArgumentType.getInteger(param0x, "seed"),
                                                            BoolArgumentType.getBool(param0x, "includeWorldSeed"),
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("includeSequenceId", BoolArgumentType.bool())
                                                        .executes(
                                                            param0x -> resetAllSequencesAndSetNewDefaults(
                                                                    param0x.getSource(),
                                                                    IntegerArgumentType.getInteger(param0x, "seed"),
                                                                    BoolArgumentType.getBool(param0x, "includeWorldSeed"),
                                                                    BoolArgumentType.getBool(param0x, "includeSequenceId")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("sequence", ResourceLocationArgument.id())
                                .suggests(RandomCommand::suggestRandomSequence)
                                .executes(param0x -> resetSequence(param0x.getSource(), ResourceLocationArgument.getId(param0x, "sequence")))
                                .then(
                                    Commands.argument("seed", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> resetSequence(
                                                    param0x.getSource(),
                                                    ResourceLocationArgument.getId(param0x, "sequence"),
                                                    IntegerArgumentType.getInteger(param0x, "seed"),
                                                    true,
                                                    true
                                                )
                                        )
                                        .then(
                                            Commands.argument("includeWorldSeed", BoolArgumentType.bool())
                                                .executes(
                                                    param0x -> resetSequence(
                                                            param0x.getSource(),
                                                            ResourceLocationArgument.getId(param0x, "sequence"),
                                                            IntegerArgumentType.getInteger(param0x, "seed"),
                                                            BoolArgumentType.getBool(param0x, "includeWorldSeed"),
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("includeSequenceId", BoolArgumentType.bool())
                                                        .executes(
                                                            param0x -> resetSequence(
                                                                    param0x.getSource(),
                                                                    ResourceLocationArgument.getId(param0x, "sequence"),
                                                                    IntegerArgumentType.getInteger(param0x, "seed"),
                                                                    BoolArgumentType.getBool(param0x, "includeWorldSeed"),
                                                                    BoolArgumentType.getBool(param0x, "includeSequenceId")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> drawRandomValueTree(String param0, boolean param1) {
        return Commands.literal(param0)
            .then(
                Commands.argument("range", RangeArgument.intRange())
                    .executes(param1x -> randomSample(param1x.getSource(), RangeArgument.Ints.getRange(param1x, "range"), null, param1))
                    .then(
                        Commands.argument("sequence", ResourceLocationArgument.id())
                            .suggests(RandomCommand::suggestRandomSequence)
                            .requires(param0x -> param0x.hasPermission(2))
                            .executes(
                                param1x -> randomSample(
                                        param1x.getSource(),
                                        RangeArgument.Ints.getRange(param1x, "range"),
                                        ResourceLocationArgument.getId(param1x, "sequence"),
                                        param1
                                    )
                            )
                    )
            );
    }

    private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandSourceStack> param0x, SuggestionsBuilder param1) {
        List<String> var0 = Lists.newArrayList();
        param0x.getSource().getLevel().getRandomSequences().forAllSequences((param1x, param2) -> var0.add(param1x.toString()));
        return SharedSuggestionProvider.suggest(var0, param1);
    }

    private static int randomSample(CommandSourceStack param0, MinMaxBounds.Ints param1, @Nullable ResourceLocation param2, boolean param3) throws CommandSyntaxException {
        RandomSource var0;
        if (param2 != null) {
            var0 = param0.getLevel().getRandomSequence(param2);
        } else {
            var0 = param0.getLevel().getRandom();
        }

        int var2 = param1.min().orElse(Integer.MIN_VALUE);
        int var3 = param1.max().orElse(Integer.MAX_VALUE);
        long var4 = (long)var3 - (long)var2;
        if (var4 == 0L) {
            throw ERROR_RANGE_TOO_SMALL.create();
        } else if (var4 >= 2147483647L) {
            throw ERROR_RANGE_TOO_LARGE.create();
        } else {
            int var5 = Mth.randomBetweenInclusive(var0, var2, var3);
            if (param3) {
                param0.getServer()
                    .getPlayerList()
                    .broadcastSystemMessage(Component.translatable("commands.random.roll", param0.getDisplayName(), var5, var2, var3), false);
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.random.sample.success", var5), false);
            }

            return var5;
        }
    }

    private static int resetSequence(CommandSourceStack param0, ResourceLocation param1) throws CommandSyntaxException {
        param0.getLevel().getRandomSequences().reset(param1);
        param0.sendSuccess(() -> Component.translatable("commands.random.reset.success", param1), false);
        return 1;
    }

    private static int resetSequence(CommandSourceStack param0, ResourceLocation param1, int param2, boolean param3, boolean param4) throws CommandSyntaxException {
        param0.getLevel().getRandomSequences().reset(param1, param2, param3, param4);
        param0.sendSuccess(() -> Component.translatable("commands.random.reset.success", param1), false);
        return 1;
    }

    private static int resetAllSequences(CommandSourceStack param0) {
        int var0 = param0.getLevel().getRandomSequences().clear();
        param0.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", var0), false);
        return var0;
    }

    private static int resetAllSequencesAndSetNewDefaults(CommandSourceStack param0, int param1, boolean param2, boolean param3) {
        RandomSequences var0 = param0.getLevel().getRandomSequences();
        var0.setSeedDefaults(param1, param2, param3);
        int var1 = var0.clear();
        param0.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", var1), false);
        return var1;
    }
}
