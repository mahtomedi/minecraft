package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.execution.tasks.IsolatedCall;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
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
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public class ExecuteCommand {
    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("commands.execute.blocks.toobig", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.execute.conditional.fail")
    );
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.execute.conditional.fail_count", param0)
    );
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("commands.execute.function.instantiationFailure", param0, param1)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (param0, param1) -> {
        LootDataManager var0 = param0.getSource().getServer().getLootData();
        return SharedSuggestionProvider.suggestResource(var0.getKeys(LootDataType.PREDICATE), param1);
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
                            param0x.getSource().withLevel((ServerLevel)var1x.level()).withPosition(var1x.position()).withRotation(var1x.getRotationVector())
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
                        .then(Commands.literal("over").then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect(var0, param0x -> {
                            Vec3 var0x = param0x.getSource().getPosition();
                            ServerLevel var1x = param0x.getSource().getLevel();
                            double var2x = var0x.x();
                            double var3 = var0x.z();
                            if (!var1x.hasChunk(SectionPos.blockToSectionCoord(var2x), SectionPos.blockToSectionCoord(var3))) {
                                throw BlockPosArgument.ERROR_NOT_LOADED.create();
                            } else {
                                int var4 = var1x.getHeight(HeightmapTypeArgument.getHeightmap(param0x, "heightmap"), Mth.floor(var2x), Mth.floor(var3));
                                return param0x.getSource().withPosition(new Vec3(var2x, (double)var4, var3));
                            }
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
                .then(
                    Commands.literal("summon")
                        .then(
                            Commands.argument("entity", ResourceArgument.resource(param1, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                .redirect(
                                    var0, param0x -> spawnEntityAndRedirect(param0x.getSource(), ResourceArgument.getSummonableEntityType(param0x, "entity"))
                                )
                        )
                )
                .then(createRelationOperations(var0, Commands.literal("on")))
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

    private static CommandSourceStack storeValue(CommandSourceStack param0, Collection<ScoreHolder> param1, Objective param2, boolean param3) {
        Scoreboard var0 = param0.getServer().getScoreboard();
        return param0.withCallback((param4, param5) -> {
            for(ScoreHolder var0x : param1) {
                ScoreAccess var1x = var0.getOrCreatePlayerScore(var0x, param2);
                int var2x = param3 ? param5 : (param4 ? 1 : 0);
                var1x.set(var2x);
            }

        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeValue(CommandSourceStack param0, CustomBossEvent param1, boolean param2, boolean param3) {
        return param0.withCallback((param3x, param4) -> {
            int var0x = param3 ? param4 : (param3x ? 1 : 0);
            if (param2) {
                param1.setValue(var0x);
            } else {
                param1.setMax(var0x);
            }

        }, CommandResultCallback::chain);
    }

    private static CommandSourceStack storeData(
        CommandSourceStack param0, DataAccessor param1, NbtPathArgument.NbtPath param2, IntFunction<Tag> param3, boolean param4
    ) {
        return param0.withCallback((param4x, param5) -> {
            try {
                CompoundTag var0x = param1.getData();
                int var1x = param4 ? param5 : (param4x ? 1 : 0);
                param2.set(var0x, param3.apply(var1x));
                param1.setData(var0x);
            } catch (CommandSyntaxException var8) {
            }

        }, CommandResultCallback::chain);
    }

    private static boolean isChunkLoaded(ServerLevel param0, BlockPos param1) {
        ChunkPos var0 = new ChunkPos(param1);
        LevelChunk var1 = param0.getChunkSource().getChunkNow(var0.x, var0.z);
        if (var1 == null) {
            return false;
        } else {
            return var1.getFullStatus() == FullChunkStatus.ENTITY_TICKING && param0.areEntitiesLoaded(var0.toLong());
        }
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
                Commands.literal("loaded")
                    .then(
                        addConditional(
                            param0,
                            Commands.argument("pos", BlockPosArgument.blockPos()),
                            param2,
                            param0x -> isChunkLoaded(param0x.getSource().getLevel(), BlockPosArgument.getBlockPos(param0x, "pos"))
                        )
                    )
            )
            .then(
                Commands.literal("dimension")
                    .then(
                        addConditional(
                            param0,
                            Commands.argument("dimension", DimensionArgument.dimension()),
                            param2,
                            param0x -> DimensionArgument.getDimension(param0x, "dimension") == param0x.getSource().getLevel()
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
                                                            param0x -> checkScore(param0x, (param0xx, param1x) -> param0xx == param1x)
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
            )
            .then(
                Commands.literal("function")
                    .then(
                        Commands.argument("name", FunctionArgument.functions())
                            .suggests(FunctionCommand.SUGGEST_FUNCTION)
                            .fork(param0, new ExecuteCommand.ExecuteIfFunctionCustomModifier(param2))
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
                param1x.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", var0x), false);
                return var0x;
            } else {
                throw ERROR_CONDITIONAL_FAILED.create();
            }
        } : param1x -> {
            int var0x = param1.test(param1x);
            if (var0x == 0) {
                param1x.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            } else {
                throw ERROR_CONDITIONAL_FAILED_COUNT.create(var0x);
            }
        };
    }

    private static int checkMatchingData(DataAccessor param0, NbtPathArgument.NbtPath param1) throws CommandSyntaxException {
        return param1.countMatching(param0.getData());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> param0, ExecuteCommand.IntBiPredicate param1) throws CommandSyntaxException {
        ScoreHolder var0 = ScoreHolderArgument.getName(param0, "target");
        Objective var1 = ObjectiveArgument.getObjective(param0, "targetObjective");
        ScoreHolder var2 = ScoreHolderArgument.getName(param0, "source");
        Objective var3 = ObjectiveArgument.getObjective(param0, "sourceObjective");
        Scoreboard var4 = param0.getSource().getServer().getScoreboard();
        ReadOnlyScoreInfo var5 = var4.getPlayerScoreInfo(var0, var1);
        ReadOnlyScoreInfo var6 = var4.getPlayerScoreInfo(var2, var3);
        return var5 != null && var6 != null ? param1.test(var5.value(), var6.value()) : false;
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> param0, MinMaxBounds.Ints param1) throws CommandSyntaxException {
        ScoreHolder var0 = ScoreHolderArgument.getName(param0, "target");
        Objective var1 = ObjectiveArgument.getObjective(param0, "targetObjective");
        Scoreboard var2 = param0.getSource().getServer().getScoreboard();
        ReadOnlyScoreInfo var3 = var2.getPlayerScoreInfo(var0, var1);
        return var3 == null ? false : param1.matches(var3.value());
    }

    private static boolean checkCustomPredicate(CommandSourceStack param0, LootItemCondition param1) {
        ServerLevel var0 = param0.getLevel();
        LootParams var1 = new LootParams.Builder(var0)
            .withParameter(LootContextParams.ORIGIN, param0.getPosition())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, param0.getEntity())
            .create(LootContextParamSets.COMMAND);
        LootContext var2 = new LootContext.Builder(var1).create(Optional.empty());
        var2.pushVisitedElement(LootContext.createVisitedEntry(param1));
        return param1.test(var2);
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> param0, boolean param1, boolean param2) {
        return (Collection<CommandSourceStack>)(param2 == param1 ? Collections.singleton(param0.getSource()) : Collections.emptyList());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
        CommandNode<CommandSourceStack> param0, ArgumentBuilder<CommandSourceStack, ?> param1, boolean param2, ExecuteCommand.CommandPredicate param3
    ) {
        return param1.fork(param0, param2x -> expect(param2x, param2, param3.test(param2x))).executes(param2x -> {
            if (param2 == param3.test(param2x)) {
                param2x.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
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
            param0.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", var0.getAsInt()), false);
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
            param0.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
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

    private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> param0) {
        return param1 -> {
            CommandSourceStack var0x = param1.getSource();
            Entity var1 = var0x.getEntity();
            return (Collection<CommandSourceStack>)(var1 == null
                ? List.of()
                : param0.apply(var1).filter(param0x -> !param0x.isRemoved()).map(param1x -> List.of(var0x.withEntity(param1x))).orElse(List.of()));
        };
    }

    private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> param0) {
        return param1 -> {
            CommandSourceStack var0x = param1.getSource();
            Entity var1 = var0x.getEntity();
            return var1 == null ? List.of() : param0.apply(var1).filter(param0x -> !param0x.isRemoved()).map(var0x::withEntity).toList();
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(
        CommandNode<CommandSourceStack> param0, LiteralArgumentBuilder<CommandSourceStack> param1
    ) {
        return param1.then(
                Commands.literal("owner")
                    .fork(
                        param0,
                        expandOneToOneEntityRelation(
                            param0x -> param0x instanceof OwnableEntity var0x ? Optional.ofNullable(var0x.getOwner()) : Optional.empty()
                        )
                    )
            )
            .then(
                Commands.literal("leasher")
                    .fork(
                        param0,
                        expandOneToOneEntityRelation(param0x -> param0x instanceof Mob var0x ? Optional.ofNullable(var0x.getLeashHolder()) : Optional.empty())
                    )
            )
            .then(
                Commands.literal("target")
                    .fork(
                        param0,
                        expandOneToOneEntityRelation(param0x -> param0x instanceof Targeting var0x ? Optional.ofNullable(var0x.getTarget()) : Optional.empty())
                    )
            )
            .then(
                Commands.literal("attacker")
                    .fork(
                        param0,
                        expandOneToOneEntityRelation(
                            param0x -> param0x instanceof Attackable var0x ? Optional.ofNullable(var0x.getLastAttacker()) : Optional.empty()
                        )
                    )
            )
            .then(Commands.literal("vehicle").fork(param0, expandOneToOneEntityRelation(param0x -> Optional.ofNullable(param0x.getVehicle()))))
            .then(Commands.literal("controller").fork(param0, expandOneToOneEntityRelation(param0x -> Optional.ofNullable(param0x.getControllingPassenger()))))
            .then(
                Commands.literal("origin")
                    .fork(
                        param0,
                        expandOneToOneEntityRelation(
                            param0x -> param0x instanceof TraceableEntity var0x ? Optional.ofNullable(var0x.getOwner()) : Optional.empty()
                        )
                    )
            )
            .then(Commands.literal("passengers").fork(param0, expandOneToManyEntityRelation(param0x -> param0x.getPassengers().stream())));
    }

    private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack param0, Holder.Reference<EntityType<?>> param1) throws CommandSyntaxException {
        Entity var0 = SummonCommand.createEntity(param0, param1, param0.getPosition(), new CompoundTag(), true);
        return param0.withEntity(var0);
    }

    public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(
        T param0,
        List<T> param1,
        Function<T, T> param2,
        IntPredicate param3,
        ContextChain<T> param4,
        @Nullable CompoundTag param5,
        ExecutionControl<T> param6,
        ExecuteCommand.CommandGetter<T, Collection<CommandFunction<T>>> param7,
        ChainModifiers param8
    ) {
        List<T> var0 = new ArrayList<>(param1.size());

        Collection<CommandFunction<T>> var1;
        try {
            var1 = param7.get(param4.getTopContext().copyFor(param0));
        } catch (CommandSyntaxException var18) {
            param0.handleError(var18, param8.isForked(), param6.tracer());
            return;
        }

        int var4 = var1.size();
        if (var4 != 0) {
            List<InstantiatedFunction<T>> var5 = new ArrayList<>(var4);

            try {
                for(CommandFunction<T> var6 : var1) {
                    try {
                        var5.add(var6.instantiate(param5, param0.dispatcher(), param0));
                    } catch (FunctionInstantiationException var17) {
                        throw ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE.create(var6.id(), var17.messageComponent());
                    }
                }
            } catch (CommandSyntaxException var19) {
                param0.handleError(var19, param8.isForked(), param6.tracer());
            }

            for(T var9 : param1) {
                T var10 = param2.apply(var9.clearCallbacks());
                CommandResultCallback var11 = (param3x, param4x) -> {
                    if (param3.test(param4x)) {
                        var0.add(var9);
                    }

                };
                param6.queueNext(new IsolatedCall<>(param2x -> {
                    for(InstantiatedFunction<T> var0x : var5) {
                        param2x.queueNext(new CallFunction<>(var0x, param2x.currentFrame().returnValueConsumer(), true).bind(var10));
                    }

                    param2x.queueNext(FallthroughTask.instance());
                }, var11));
            }

            ContextChain<T> var12 = param4.nextStage();
            String var13 = param4.getTopContext().getInput();
            param6.queueNext(new BuildContexts.Continuation<>(var13, var12, param8, param0, var0));
        }
    }

    @FunctionalInterface
    public interface CommandGetter<T, R> {
        R get(CommandContext<T> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface CommandNumericPredicate {
        int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface CommandPredicate {
        boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    static class ExecuteIfFunctionCustomModifier implements CustomModifierExecutor.ModifierAdapter<CommandSourceStack> {
        private final IntPredicate check;

        ExecuteIfFunctionCustomModifier(boolean param0) {
            this.check = param0 ? param0x -> param0x != 0 : param0x -> param0x == 0;
        }

        public void apply(
            CommandSourceStack param0,
            List<CommandSourceStack> param1,
            ContextChain<CommandSourceStack> param2,
            ChainModifiers param3,
            ExecutionControl<CommandSourceStack> param4
        ) {
            ExecuteCommand.scheduleFunctionConditionsAndTest(
                param0,
                param1,
                FunctionCommand::modifySenderForExecution,
                this.check,
                param2,
                null,
                param4,
                param0x -> FunctionArgument.getFunctions(param0x, "name"),
                param3
            );
        }
    }

    @FunctionalInterface
    interface IntBiPredicate {
        boolean test(int var1, int var2);
    }
}
