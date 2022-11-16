package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ExecuteCommand {
    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.execute.blocks.toobig", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.execute.conditional.fail")
    );
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.execute.conditional.fail_count", param0)
    );
    private static final BinaryOperator<ResultConsumer<CommandSourceStack>> CALLBACK_CHAINER = (param0, param1) -> (param2, param3, param4) -> {
            param0.onCommandComplete(param2, param3, param4);
            param1.onCommandComplete(param2, param3, param4);
        };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (param0, param1) -> {
        PredicateManager var0 = param0.getSource().getServer().getPredicateManager();
        return SharedSuggestionProvider.suggestResource(var0.getKeys(), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(Commands.literal("execute").requires(param0x -> param0x.hasPermission(2)));
        param0.register(
            Commands.literal("execute")
                .requires(param0x -> param0x.hasPermission(2))
                .then(Commands.literal("run").redirect(param0.getRoot()))
                .then(addConditionals(var0, Commands.literal("if"), true, param1))
                .then(addConditionals(var0, Commands.literal("unless"), false, param1))
                .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(var0, param0x -> {
                    List<CommandSourceStack> var0x = Lists.newArrayList();
        
                    for(Entity var1x : EntityArgument.getOptionalEntities(param0x, "targets")) {
                        var0x.add(param0x.getSource().withEntity(var1x));
                    }
        
                    return var0x;
                })))
                .then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork(var0, param0x -> {
                    List<CommandSourceStack> var0x = Lists.newArrayList();
        
                    for(Entity var1x : EntityArgument.getOptionalEntities(param0x, "targets")) {
                        var0x.add(
                            param0x.getSource().withLevel((ServerLevel)var1x.level).withPosition(var1x.position()).withRotation(var1x.getRotationVector())
                        );
                    }
        
                    return var0x;
                })))
                .then(
                    Commands.literal("store")
                        .then(wrapStores(var0, Commands.literal("result"), true))
                        .then(wrapStores(var0, Commands.literal("success"), false))
                )
                .then(
                    Commands.literal("positioned")
                        .then(
                            Commands.argument("pos", Vec3Argument.vec3())
                                .redirect(
                                    var0,
                                    param0x -> param0x.getSource()
                                            .withPosition(Vec3Argument.getVec3(param0x, "pos"))
                                            .withAnchor(EntityAnchorArgument.Anchor.FEET)
                                )
                        )
                        .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(var0, param0x -> {
                            List<CommandSourceStack> var0x = Lists.newArrayList();
                
                            for(Entity var1x : EntityArgument.getOptionalEntities(param0x, "targets")) {
                                var0x.add(param0x.getSource().withPosition(var1x.position()));
                            }
                
                            return var0x;
                        })))
                )
                .then(
                    Commands.literal("rotated")
                        .then(
                            Commands.argument("rot", RotationArgument.rotation())
                                .redirect(
                                    var0,
                                    param0x -> param0x.getSource().withRotation(RotationArgument.getRotation(param0x, "rot").getRotation(param0x.getSource()))
                                )
                        )
                        .then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(var0, param0x -> {
                            List<CommandSourceStack> var0x = Lists.newArrayList();
                
                            for(Entity var1x : EntityArgument.getOptionalEntities(param0x, "targets")) {
                                var0x.add(param0x.getSource().withRotation(var1x.getRotationVector()));
                            }
                
                            return var0x;
                        })))
                )
                .then(
                    Commands.literal("facing")
                        .then(
                            Commands.literal("entity")
                                .then(
                                    Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork(var0, param0x -> {
                                            List<CommandSourceStack> var0x = Lists.newArrayList();
                                            EntityAnchorArgument.Anchor var1x = EntityAnchorArgument.getAnchor(param0x, "anchor");
                                
                                            for(Entity var2x : EntityArgument.getOptionalEntities(param0x, "targets")) {
                                                var0x.add(param0x.getSource().facing(var2x, var1x));
                                            }
                                
                                            return var0x;
                                        }))
                                )
                        )
                        .then(
                            Commands.argument("pos", Vec3Argument.vec3())
                                .redirect(var0, param0x -> param0x.getSource().facing(Vec3Argument.getVec3(param0x, "pos")))
                        )
                )
                .then(
                    Commands.literal("align")
                        .then(
                            Commands.argument("axes", SwizzleArgument.swizzle())
                                .redirect(
                                    var0,
                                    param0x -> param0x.getSource()
                                            .withPosition(param0x.getSource().getPosition().align(SwizzleArgument.getSwizzle(param0x, "axes")))
                                )
                        )
                )
                .then(
                    Commands.literal("anchored")
                        .then(
                            Commands.argument("anchor", EntityAnchorArgument.anchor())
                                .redirect(var0, param0x -> param0x.getSource().withAnchor(EntityAnchorArgument.getAnchor(param0x, "anchor")))
                        )
                )
                .then(
                    Commands.literal("in")
                        .then(
                            Commands.argument("dimension", DimensionArgument.dimension())
                                .redirect(var0, param0x -> param0x.getSource().withLevel(DimensionArgument.getDimension(param0x, "dimension")))
                        )
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(
        LiteralCommandNode<CommandSourceStack> param0, LiteralArgumentBuilder<CommandSourceStack> param1, boolean param2
    ) {
        param1.then(
            Commands.literal("score")
                .then(
                    Commands.argument("targets", ScoreHolderArgument.scoreHolders())
                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                        .then(
                            Commands.argument("objective", ObjectiveArgument.objective())
                                .redirect(
                                    param0,
                                    param1x -> storeValue(
                                            param1x.getSource(),
                                            ScoreHolderArgument.getNamesWithDefaultWildcard(param1x, "targets"),
                                            ObjectiveArgument.getObjective(param1x, "objective"),
                                            param2
                                        )
                                )
                        )
                )
        );
        param1.then(
            Commands.literal("bossbar")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .suggests(BossBarCommands.SUGGEST_BOSS_BAR)
                        .then(
                            Commands.literal("value")
                                .redirect(param0, param1x -> storeValue(param1x.getSource(), BossBarCommands.getBossBar(param1x), true, param2))
                        )
                        .then(
                            Commands.literal("max")
                                .redirect(param0, param1x -> storeValue(param1x.getSource(), BossBarCommands.getBossBar(param1x), false, param2))
                        )
                )
        );

        for(DataCommands.DataProvider var0 : DataCommands.TARGET_PROVIDERS) {
            var0.wrap(
                param1,
                param3 -> param3.then(
                        Commands.argument("path", NbtPathArgument.nbtPath())
                            .then(
                                Commands.literal("int")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> IntTag.valueOf((int)((double)param1x * DoubleArgumentType.getDouble(param2x, "scale"))),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("float")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> FloatTag.valueOf((float)((double)param1x * DoubleArgumentType.getDouble(param2x, "scale"))),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("short")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> ShortTag.valueOf(
                                                                (short)((int)((double)param1x * DoubleArgumentType.getDouble(param2x, "scale")))
                                                            ),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("long")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> LongTag.valueOf((long)((double)param1x * DoubleArgumentType.getDouble(param2x, "scale"))),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("double")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> DoubleTag.valueOf((double)param1x * DoubleArgumentType.getDouble(param2x, "scale")),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("byte")
                                    .then(
                                        Commands.argument("scale", DoubleArgumentType.doubleArg())
                                            .redirect(
                                                param0,
                                                param2x -> storeData(
                                                        param2x.getSource(),
                                                        var0.access(param2x),
                                                        NbtPathArgument.getPath(param2x, "path"),
                                                        param1x -> ByteTag.valueOf(
                                                                (byte)((int)((double)param1x * DoubleArgumentType.getDouble(param2x, "scale")))
                                                            ),
                                                        param2
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }

        return param1;
    }

    private static CommandSourceStack storeValue(CommandSourceStack param0, Collection<String> param1, Objective param2, boolean param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();
        return param0.withCallback((param4, param5, param6) -> {
            for(String var0x : param1) {
                Score var1x = var0.getOrCreatePlayerScore(var0x, param2);
                int var2x = param3 ? param6 : (param5 ? 1 : 0);
                var1x.setScore(var2x);
            }

        }, CALLBACK_CHAINER);
    }

    private static CommandSourceStack storeValue(CommandSourceStack param0, CustomBossEvent param1, boolean param2, boolean param3) {
        return param0.withCallback((param3x, param4, param5) -> {
            int var0x = param3 ? param5 : (param4 ? 1 : 0);
            if (param2) {
                param1.setValue(var0x);
            } else {
                param1.setMax(var0x);
            }

        }, CALLBACK_CHAINER);
    }

    private static CommandSourceStack storeData(
        CommandSourceStack param0, DataAccessor param1, NbtPathArgument.NbtPath param2, IntFunction<Tag> param3, boolean param4
    ) {
        return param0.withCallback((param4x, param5, param6) -> {
            try {
                CompoundTag var0x = param1.getData();
                int var1x = param4 ? param6 : (param5 ? 1 : 0);
                param2.set(var0x, () -> param3.apply(var1x));
                param1.setData(var0x);
            } catch (CommandSyntaxException var9) {
            }

        }, CALLBACK_CHAINER);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(
        CommandNode<CommandSourceStack> param0, LiteralArgumentBuilder<CommandSourceStack> param1, boolean param2, CommandBuildContext param3
    ) {
        param1.then(
                Commands.literal("block")
                    .then(
                        Commands.argument("pos", BlockPosArgument.blockPos())
                            .then(
                                addConditional(
                                    param0,
                                    Commands.argument("block", BlockPredicateArgument.blockPredicate(param3)),
                                    param2,
                                    param0x -> BlockPredicateArgument.getBlockPredicate(param0x, "block")
                                            .test(new BlockInWorld(param0x.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(param0x, "pos"), true))
                                )
                            )
                    )
            )
            .then(
                Commands.literal("biome")
                    .then(
                        Commands.argument("pos", BlockPosArgument.blockPos())
                            .then(
                                addConditional(
                                    param0,
                                    Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(param3, Registries.BIOME)),
                                    param2,
                                    param0x -> ResourceOrTagArgument.getResourceOrTag(param0x, "biome", Registries.BIOME)
                                            .test(param0x.getSource().getLevel().getBiome(BlockPosArgument.getLoadedBlockPos(param0x, "pos")))
                                )
                            )
                    )
            )
            .then(
                Commands.literal("score")
                    .then(
                        Commands.argument("target", ScoreHolderArgument.scoreHolder())
                            .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                            .then(
                                Commands.argument("targetObjective", ObjectiveArgument.objective())
                                    .then(
                                        Commands.literal("=")
                                            .then(
                                                Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                    .then(
                                                        addConditional(
                                                            param0,
                                                            Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                            param2,
                                                            param0x -> checkScore(param0x, Integer::equals)
                                                        )
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("<")
                                            .then(
                                                Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                    .then(
                                                        addConditional(
                                                            param0,
                                                            Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                            param2,
                                                            param0x -> checkScore(param0x, (param0xx, param1x) -> param0xx < param1x)
                                                        )
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("<=")
                                            .then(
                                                Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                    .then(
                                                        addConditional(
                                                            param0,
                                                            Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                            param2,
                                                            param0x -> checkScore(param0x, (param0xx, param1x) -> param0xx <= param1x)
                                                        )
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal(">")
                                            .then(
                                                Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                    .then(
                                                        addConditional(
                                                            param0,
                                                            Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                            param2,
                                                            param0x -> checkScore(param0x, (param0xx, param1x) -> param0xx > param1x)
                                                        )
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal(">=")
                                            .then(
                                                Commands.argument("source", ScoreHolderArgument.scoreHolder())
                                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                    .then(
                                                        addConditional(
                                                            param0,
                                                            Commands.argument("sourceObjective", ObjectiveArgument.objective()),
                                                            param2,
                                                            param0x -> checkScore(param0x, (param0xx, param1x) -> param0xx >= param1x)
                                                        )
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("matches")
                                            .then(
                                                addConditional(
                                                    param0,
                                                    Commands.argument("range", RangeArgument.intRange()),
                                                    param2,
                                                    param0x -> checkScore(param0x, RangeArgument.Ints.getRange(param0x, "range"))
                                                )
                                            )
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("blocks")
                    .then(
                        Commands.argument("start", BlockPosArgument.blockPos())
                            .then(
                                Commands.argument("end", BlockPosArgument.blockPos())
                                    .then(
                                        Commands.argument("destination", BlockPosArgument.blockPos())
                                            .then(addIfBlocksConditional(param0, Commands.literal("all"), param2, false))
                                            .then(addIfBlocksConditional(param0, Commands.literal("masked"), param2, true))
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("entity")
                    .then(
                        Commands.argument("entities", EntityArgument.entities())
                            .fork(param0, param1x -> expect(param1x, param2, !EntityArgument.getOptionalEntities(param1x, "entities").isEmpty()))
                            .executes(createNumericConditionalHandler(param2, param0x -> EntityArgument.getOptionalEntities(param0x, "entities").size()))
                    )
            )
            .then(
                Commands.literal("predicate")
                    .then(
                        addConditional(
                            param0,
                            Commands.argument("predicate", ResourceLocationArgument.id()).suggests(SUGGEST_PREDICATE),
                            param2,
                            param0x -> checkCustomPredicate(param0x.getSource(), ResourceLocationArgument.getPredicate(param0x, "predicate"))
                        )
                    )
            );

        for(DataCommands.DataProvider var0 : DataCommands.SOURCE_PROVIDERS) {
            param1.then(
                var0.wrap(
                    Commands.literal("data"),
                    param3x -> param3x.then(
                            Commands.argument("path", NbtPathArgument.nbtPath())
                                .fork(
                                    param0,
                                    param2x -> expect(param2x, param2, checkMatchingData(var0.access(param2x), NbtPathArgument.getPath(param2x, "path")) > 0)
                                )
                                .executes(
                                    createNumericConditionalHandler(
                                        param2, param1x -> checkMatchingData(var0.access(param1x), NbtPathArgument.getPath(param1x, "path"))
                                    )
                                )
                        )
                )
            );
        }

        return param1;
    }

    private static Command<CommandSourceStack> createNumericConditionalHandler(boolean param0, ExecuteCommand.CommandNumericPredicate param1) {
        return param0 ? param1x -> {
            int var0x = param1.test(param1x);
            if (var0x > 0) {
                param1x.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass_count", var0x), false);
                return var0x;
            } else {
                throw ERROR_CONDITIONAL_FAILED.create();
            }
        } : param1x -> {
            int var0x = param1.test(param1x);
            if (var0x == 0) {
                param1x.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            } else {
                throw ERROR_CONDITIONAL_FAILED_COUNT.create(var0x);
            }
        };
    }

    private static int checkMatchingData(DataAccessor param0, NbtPathArgument.NbtPath param1) throws CommandSyntaxException {
        return param1.countMatching(param0.getData());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> param0, BiPredicate<Integer, Integer> param1) throws CommandSyntaxException {
        String var0 = ScoreHolderArgument.getName(param0, "target");
        Objective var1 = ObjectiveArgument.getObjective(param0, "targetObjective");
        String var2 = ScoreHolderArgument.getName(param0, "source");
        Objective var3 = ObjectiveArgument.getObjective(param0, "sourceObjective");
        Scoreboard var4 = param0.getSource().getServer().getScoreboard();
        if (var4.hasPlayerScore(var0, var1) && var4.hasPlayerScore(var2, var3)) {
            Score var5 = var4.getOrCreatePlayerScore(var0, var1);
            Score var6 = var4.getOrCreatePlayerScore(var2, var3);
            return param1.test(var5.getScore(), var6.getScore());
        } else {
            return false;
        }
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> param0, MinMaxBounds.Ints param1) throws CommandSyntaxException {
        String var0 = ScoreHolderArgument.getName(param0, "target");
        Objective var1 = ObjectiveArgument.getObjective(param0, "targetObjective");
        Scoreboard var2 = param0.getSource().getServer().getScoreboard();
        return !var2.hasPlayerScore(var0, var1) ? false : param1.matches(var2.getOrCreatePlayerScore(var0, var1).getScore());
    }

    private static boolean checkCustomPredicate(CommandSourceStack param0, LootItemCondition param1) {
        ServerLevel var0 = param0.getLevel();
        LootContext.Builder var1 = new LootContext.Builder(var0)
            .withParameter(LootContextParams.ORIGIN, param0.getPosition())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, param0.getEntity());
        return param1.test(var1.create(LootContextParamSets.COMMAND));
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> param0, boolean param1, boolean param2) {
        return (Collection<CommandSourceStack>)(param2 == param1 ? Collections.singleton(param0.getSource()) : Collections.emptyList());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
        CommandNode<CommandSourceStack> param0, ArgumentBuilder<CommandSourceStack, ?> param1, boolean param2, ExecuteCommand.CommandPredicate param3
    ) {
        return param1.fork(param0, param2x -> expect(param2x, param2, param3.test(param2x))).executes(param2x -> {
            if (param2 == param3.test(param2x)) {
                param2x.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            } else {
                throw ERROR_CONDITIONAL_FAILED.create();
            }
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(
        CommandNode<CommandSourceStack> param0, ArgumentBuilder<CommandSourceStack, ?> param1, boolean param2, boolean param3
    ) {
        return param1.fork(param0, param2x -> expect(param2x, param2, checkRegions(param2x, param3).isPresent()))
            .executes(param2 ? param1x -> checkIfRegions(param1x, param3) : param1x -> checkUnlessRegions(param1x, param3));
    }

    private static int checkIfRegions(CommandContext<CommandSourceStack> param0, boolean param1) throws CommandSyntaxException {
        OptionalInt var0 = checkRegions(param0, param1);
        if (var0.isPresent()) {
            param0.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass_count", var0.getAsInt()), false);
            return var0.getAsInt();
        } else {
            throw ERROR_CONDITIONAL_FAILED.create();
        }
    }

    private static int checkUnlessRegions(CommandContext<CommandSourceStack> param0, boolean param1) throws CommandSyntaxException {
        OptionalInt var0 = checkRegions(param0, param1);
        if (var0.isPresent()) {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(var0.getAsInt());
        } else {
            param0.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
            return 1;
        }
    }

    private static OptionalInt checkRegions(CommandContext<CommandSourceStack> param0, boolean param1) throws CommandSyntaxException {
        return checkRegions(
            param0.getSource().getLevel(),
            BlockPosArgument.getLoadedBlockPos(param0, "start"),
            BlockPosArgument.getLoadedBlockPos(param0, "end"),
            BlockPosArgument.getLoadedBlockPos(param0, "destination"),
            param1
        );
    }

    private static OptionalInt checkRegions(ServerLevel param0, BlockPos param1, BlockPos param2, BlockPos param3, boolean param4) throws CommandSyntaxException {
        BoundingBox var0 = BoundingBox.fromCorners(param1, param2);
        BoundingBox var1 = BoundingBox.fromCorners(param3, param3.offset(var0.getLength()));
        BlockPos var2 = new BlockPos(var1.minX() - var0.minX(), var1.minY() - var0.minY(), var1.minZ() - var0.minZ());
        int var3 = var0.getXSpan() * var0.getYSpan() * var0.getZSpan();
        if (var3 > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, var3);
        } else {
            int var4 = 0;

            for(int var5 = var0.minZ(); var5 <= var0.maxZ(); ++var5) {
                for(int var6 = var0.minY(); var6 <= var0.maxY(); ++var6) {
                    for(int var7 = var0.minX(); var7 <= var0.maxX(); ++var7) {
                        BlockPos var8 = new BlockPos(var7, var6, var5);
                        BlockPos var9 = var8.offset(var2);
                        BlockState var10 = param0.getBlockState(var8);
                        if (!param4 || !var10.is(Blocks.AIR)) {
                            if (var10 != param0.getBlockState(var9)) {
                                return OptionalInt.empty();
                            }

                            BlockEntity var11 = param0.getBlockEntity(var8);
                            BlockEntity var12 = param0.getBlockEntity(var9);
                            if (var11 != null) {
                                if (var12 == null) {
                                    return OptionalInt.empty();
                                }

                                if (var12.getType() != var11.getType()) {
                                    return OptionalInt.empty();
                                }

                                CompoundTag var13 = var11.saveWithoutMetadata();
                                CompoundTag var14 = var12.saveWithoutMetadata();
                                if (!var13.equals(var14)) {
                                    return OptionalInt.empty();
                                }
                            }

                            ++var4;
                        }
                    }
                }
            }

            return OptionalInt.of(var4);
        }
    }

    @FunctionalInterface
    interface CommandNumericPredicate {
        int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface CommandPredicate {
        boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}
