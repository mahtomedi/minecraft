package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.clone.toobig", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = param0 -> !param0.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("clone")
                .requires(param0x -> param0x.hasPermission(2))
                .then(beginEndDestinationAndModeSuffix(param1, param0x -> param0x.getSource().getLevel()))
                .then(
                    Commands.literal("from")
                        .then(
                            Commands.argument("sourceDimension", DimensionArgument.dimension())
                                .then(beginEndDestinationAndModeSuffix(param1, param0x -> DimensionArgument.getDimension(param0x, "sourceDimension")))
                        )
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(
        CommandBuildContext param0, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> param1
    ) {
        return Commands.argument("begin", BlockPosArgument.blockPos())
            .then(
                Commands.argument("end", BlockPosArgument.blockPos())
                    .then(destinationAndModeSuffix(param0, param1, param0x -> param0x.getSource().getLevel()))
                    .then(
                        Commands.literal("to")
                            .then(
                                Commands.argument("targetDimension", DimensionArgument.dimension())
                                    .then(destinationAndModeSuffix(param0, param1, param0x -> DimensionArgument.getDimension(param0x, "targetDimension")))
                            )
                    )
            );
    }

    private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(
        CommandContext<CommandSourceStack> param0, ServerLevel param1, String param2
    ) throws CommandSyntaxException {
        BlockPos var0 = BlockPosArgument.getLoadedBlockPos(param0, param1, param2);
        return new CloneCommands.DimensionAndPosition(param1, var0);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(
        CommandBuildContext param0,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> param1,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> param2
    ) {
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> var0 = param1x -> getLoadedDimensionAndPosition(
                param1x, param1.apply(param1x), "begin"
            );
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> var1 = param1x -> getLoadedDimensionAndPosition(
                param1x, param1.apply(param1x), "end"
            );
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> var2 = param1x -> getLoadedDimensionAndPosition(
                param1x, param2.apply(param1x), "destination"
            );
        return Commands.argument("destination", BlockPosArgument.blockPos())
            .executes(
                param3 -> clone(param3.getSource(), var0.apply(param3), var1.apply(param3), var2.apply(param3), param0x -> true, CloneCommands.Mode.NORMAL)
            )
            .then(
                wrapWithCloneMode(
                    var0,
                    var1,
                    var2,
                    param0x -> param0xx -> true,
                    Commands.literal("replace")
                        .executes(
                            param3 -> clone(
                                    param3.getSource(), var0.apply(param3), var1.apply(param3), var2.apply(param3), param0x -> true, CloneCommands.Mode.NORMAL
                                )
                        )
                )
            )
            .then(
                wrapWithCloneMode(
                    var0,
                    var1,
                    var2,
                    param0x -> FILTER_AIR,
                    Commands.literal("masked")
                        .executes(
                            param3 -> clone(
                                    param3.getSource(), var0.apply(param3), var1.apply(param3), var2.apply(param3), FILTER_AIR, CloneCommands.Mode.NORMAL
                                )
                        )
                )
            )
            .then(
                Commands.literal("filtered")
                    .then(
                        wrapWithCloneMode(
                            var0,
                            var1,
                            var2,
                            param0x -> BlockPredicateArgument.getBlockPredicate(param0x, "filter"),
                            Commands.argument("filter", BlockPredicateArgument.blockPredicate(param0))
                                .executes(
                                    param3 -> clone(
                                            param3.getSource(),
                                            var0.apply(param3),
                                            var1.apply(param3),
                                            var2.apply(param3),
                                            BlockPredicateArgument.getBlockPredicate(param3, "filter"),
                                            CloneCommands.Mode.NORMAL
                                        )
                                )
                        )
                    )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> param0,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> param1,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> param2,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> param3,
        ArgumentBuilder<CommandSourceStack, ?> param4
    ) {
        return param4.then(
                Commands.literal("force")
                    .executes(
                        param4x -> clone(
                                param4x.getSource(),
                                param0.apply(param4x),
                                param1.apply(param4x),
                                param2.apply(param4x),
                                param3.apply(param4x),
                                CloneCommands.Mode.FORCE
                            )
                    )
            )
            .then(
                Commands.literal("move")
                    .executes(
                        param4x -> clone(
                                param4x.getSource(),
                                param0.apply(param4x),
                                param1.apply(param4x),
                                param2.apply(param4x),
                                param3.apply(param4x),
                                CloneCommands.Mode.MOVE
                            )
                    )
            )
            .then(
                Commands.literal("normal")
                    .executes(
                        param4x -> clone(
                                param4x.getSource(),
                                param0.apply(param4x),
                                param1.apply(param4x),
                                param2.apply(param4x),
                                param3.apply(param4x),
                                CloneCommands.Mode.NORMAL
                            )
                    )
            );
    }

    private static int clone(
        CommandSourceStack param0,
        CloneCommands.DimensionAndPosition param1,
        CloneCommands.DimensionAndPosition param2,
        CloneCommands.DimensionAndPosition param3,
        Predicate<BlockInWorld> param4,
        CloneCommands.Mode param5
    ) throws CommandSyntaxException {
        BlockPos var0 = param1.position();
        BlockPos var1 = param2.position();
        BoundingBox var2 = BoundingBox.fromCorners(var0, var1);
        BlockPos var3 = param3.position();
        BlockPos var4 = var3.offset(var2.getLength());
        BoundingBox var5 = BoundingBox.fromCorners(var3, var4);
        ServerLevel var6 = param1.dimension();
        ServerLevel var7 = param3.dimension();
        if (!param5.canOverlap() && var6 == var7 && var5.intersects(var2)) {
            throw ERROR_OVERLAP.create();
        } else {
            int var8 = var2.getXSpan() * var2.getYSpan() * var2.getZSpan();
            int var9 = param0.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
            if (var8 > var9) {
                throw ERROR_AREA_TOO_LARGE.create(var9, var8);
            } else if (var6.hasChunksAt(var0, var1) && var7.hasChunksAt(var3, var4)) {
                List<CloneCommands.CloneBlockInfo> var10 = Lists.newArrayList();
                List<CloneCommands.CloneBlockInfo> var11 = Lists.newArrayList();
                List<CloneCommands.CloneBlockInfo> var12 = Lists.newArrayList();
                Deque<BlockPos> var13 = Lists.newLinkedList();
                BlockPos var14 = new BlockPos(var5.minX() - var2.minX(), var5.minY() - var2.minY(), var5.minZ() - var2.minZ());

                for(int var15 = var2.minZ(); var15 <= var2.maxZ(); ++var15) {
                    for(int var16 = var2.minY(); var16 <= var2.maxY(); ++var16) {
                        for(int var17 = var2.minX(); var17 <= var2.maxX(); ++var17) {
                            BlockPos var18 = new BlockPos(var17, var16, var15);
                            BlockPos var19 = var18.offset(var14);
                            BlockInWorld var20 = new BlockInWorld(var6, var18, false);
                            BlockState var21 = var20.getState();
                            if (param4.test(var20)) {
                                BlockEntity var22 = var6.getBlockEntity(var18);
                                if (var22 != null) {
                                    CompoundTag var23 = var22.saveWithoutMetadata();
                                    var11.add(new CloneCommands.CloneBlockInfo(var19, var21, var23));
                                    var13.addLast(var18);
                                } else if (!var21.isSolidRender(var6, var18) && !var21.isCollisionShapeFullBlock(var6, var18)) {
                                    var12.add(new CloneCommands.CloneBlockInfo(var19, var21, null));
                                    var13.addFirst(var18);
                                } else {
                                    var10.add(new CloneCommands.CloneBlockInfo(var19, var21, null));
                                    var13.addLast(var18);
                                }
                            }
                        }
                    }
                }

                if (param5 == CloneCommands.Mode.MOVE) {
                    for(BlockPos var24 : var13) {
                        BlockEntity var25 = var6.getBlockEntity(var24);
                        Clearable.tryClear(var25);
                        var6.setBlock(var24, Blocks.BARRIER.defaultBlockState(), 2);
                    }

                    for(BlockPos var26 : var13) {
                        var6.setBlock(var26, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                List<CloneCommands.CloneBlockInfo> var27 = Lists.newArrayList();
                var27.addAll(var10);
                var27.addAll(var11);
                var27.addAll(var12);
                List<CloneCommands.CloneBlockInfo> var28 = Lists.reverse(var27);

                for(CloneCommands.CloneBlockInfo var29 : var28) {
                    BlockEntity var30 = var7.getBlockEntity(var29.pos);
                    Clearable.tryClear(var30);
                    var7.setBlock(var29.pos, Blocks.BARRIER.defaultBlockState(), 2);
                }

                int var31 = 0;

                for(CloneCommands.CloneBlockInfo var32 : var27) {
                    if (var7.setBlock(var32.pos, var32.state, 2)) {
                        ++var31;
                    }
                }

                for(CloneCommands.CloneBlockInfo var33 : var11) {
                    BlockEntity var34 = var7.getBlockEntity(var33.pos);
                    if (var33.tag != null && var34 != null) {
                        var34.load(var33.tag);
                        var34.setChanged();
                    }

                    var7.setBlock(var33.pos, var33.state, 2);
                }

                for(CloneCommands.CloneBlockInfo var35 : var28) {
                    var7.blockUpdated(var35.pos, var35.state.getBlock());
                }

                var7.getBlockTicks().copyAreaFrom(var6.getBlockTicks(), var2, var14);
                if (var31 == 0) {
                    throw ERROR_FAILED.create();
                } else {
                    param0.sendSuccess(Component.translatable("commands.clone.success", var31), true);
                    return var31;
                }
            } else {
                throw BlockPosArgument.ERROR_NOT_LOADED.create();
            }
        }
    }

    static class CloneBlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        @Nullable
        public final CompoundTag tag;

        public CloneBlockInfo(BlockPos param0, BlockState param1, @Nullable CompoundTag param2) {
            this.pos = param0;
            this.state = param1;
            this.tag = param2;
        }
    }

    @FunctionalInterface
    interface CommandFunction<T, R> {
        R apply(T var1) throws CommandSyntaxException;
    }

    static record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean param0) {
            this.canOverlap = param0;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }
}
