package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.clone.toobig", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = param0 -> !param0.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("clone")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("begin", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("end", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("destination", BlockPosArgument.blockPos())
                                        .executes(
                                            param0x -> clone(
                                                    param0x.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                    param0xx -> true,
                                                    CloneCommands.Mode.NORMAL
                                                )
                                        )
                                        .then(
                                            Commands.literal("replace")
                                                .executes(
                                                    param0x -> clone(
                                                            param0x.getSource(),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                            param0xx -> true,
                                                            CloneCommands.Mode.NORMAL
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("force")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    param0xx -> true,
                                                                    CloneCommands.Mode.FORCE
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("move")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    param0xx -> true,
                                                                    CloneCommands.Mode.MOVE
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("normal")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    param0xx -> true,
                                                                    CloneCommands.Mode.NORMAL
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("masked")
                                                .executes(
                                                    param0x -> clone(
                                                            param0x.getSource(),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                            FILTER_AIR,
                                                            CloneCommands.Mode.NORMAL
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("force")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    FILTER_AIR,
                                                                    CloneCommands.Mode.FORCE
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("move")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    FILTER_AIR,
                                                                    CloneCommands.Mode.MOVE
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("normal")
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    FILTER_AIR,
                                                                    CloneCommands.Mode.NORMAL
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("filtered")
                                                .then(
                                                    Commands.argument("filter", BlockPredicateArgument.blockPredicate())
                                                        .executes(
                                                            param0x -> clone(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                    BlockPredicateArgument.getBlockPredicate(param0x, "filter"),
                                                                    CloneCommands.Mode.NORMAL
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("force")
                                                                .executes(
                                                                    param0x -> clone(
                                                                            param0x.getSource(),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                            BlockPredicateArgument.getBlockPredicate(param0x, "filter"),
                                                                            CloneCommands.Mode.FORCE
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("move")
                                                                .executes(
                                                                    param0x -> clone(
                                                                            param0x.getSource(),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                            BlockPredicateArgument.getBlockPredicate(param0x, "filter"),
                                                                            CloneCommands.Mode.MOVE
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("normal")
                                                                .executes(
                                                                    param0x -> clone(
                                                                            param0x.getSource(),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "begin"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "end"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "destination"),
                                                                            BlockPredicateArgument.getBlockPredicate(param0x, "filter"),
                                                                            CloneCommands.Mode.NORMAL
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

    private static int clone(
        CommandSourceStack param0, BlockPos param1, BlockPos param2, BlockPos param3, Predicate<BlockInWorld> param4, CloneCommands.Mode param5
    ) throws CommandSyntaxException {
        BoundingBox var0 = new BoundingBox(param1, param2);
        BlockPos var1 = param3.offset(var0.getLength());
        BoundingBox var2 = new BoundingBox(param3, var1);
        if (!param5.canOverlap() && var2.intersects(var0)) {
            throw ERROR_OVERLAP.create();
        } else {
            int var3 = var0.getXSpan() * var0.getYSpan() * var0.getZSpan();
            if (var3 > 32768) {
                throw ERROR_AREA_TOO_LARGE.create(32768, var3);
            } else {
                ServerLevel var4 = param0.getLevel();
                if (var4.hasChunksAt(param1, param2) && var4.hasChunksAt(param3, var1)) {
                    List<CloneCommands.CloneBlockInfo> var5 = Lists.newArrayList();
                    List<CloneCommands.CloneBlockInfo> var6 = Lists.newArrayList();
                    List<CloneCommands.CloneBlockInfo> var7 = Lists.newArrayList();
                    Deque<BlockPos> var8 = Lists.newLinkedList();
                    BlockPos var9 = new BlockPos(var2.x0 - var0.x0, var2.y0 - var0.y0, var2.z0 - var0.z0);

                    for(int var10 = var0.z0; var10 <= var0.z1; ++var10) {
                        for(int var11 = var0.y0; var11 <= var0.y1; ++var11) {
                            for(int var12 = var0.x0; var12 <= var0.x1; ++var12) {
                                BlockPos var13 = new BlockPos(var12, var11, var10);
                                BlockPos var14 = var13.offset(var9);
                                BlockInWorld var15 = new BlockInWorld(var4, var13, false);
                                BlockState var16 = var15.getState();
                                if (param4.test(var15)) {
                                    BlockEntity var17 = var4.getBlockEntity(var13);
                                    if (var17 != null) {
                                        CompoundTag var18 = var17.save(new CompoundTag());
                                        var6.add(new CloneCommands.CloneBlockInfo(var14, var16, var18));
                                        var8.addLast(var13);
                                    } else if (!var16.isSolidRender(var4, var13) && !var16.isCollisionShapeFullBlock(var4, var13)) {
                                        var7.add(new CloneCommands.CloneBlockInfo(var14, var16, null));
                                        var8.addFirst(var13);
                                    } else {
                                        var5.add(new CloneCommands.CloneBlockInfo(var14, var16, null));
                                        var8.addLast(var13);
                                    }
                                }
                            }
                        }
                    }

                    if (param5 == CloneCommands.Mode.MOVE) {
                        for(BlockPos var19 : var8) {
                            BlockEntity var20 = var4.getBlockEntity(var19);
                            Clearable.tryClear(var20);
                            var4.setBlock(var19, Blocks.BARRIER.defaultBlockState(), 2);
                        }

                        for(BlockPos var21 : var8) {
                            var4.setBlock(var21, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }

                    List<CloneCommands.CloneBlockInfo> var22 = Lists.newArrayList();
                    var22.addAll(var5);
                    var22.addAll(var6);
                    var22.addAll(var7);
                    List<CloneCommands.CloneBlockInfo> var23 = Lists.reverse(var22);

                    for(CloneCommands.CloneBlockInfo var24 : var23) {
                        BlockEntity var25 = var4.getBlockEntity(var24.pos);
                        Clearable.tryClear(var25);
                        var4.setBlock(var24.pos, Blocks.BARRIER.defaultBlockState(), 2);
                    }

                    int var26 = 0;

                    for(CloneCommands.CloneBlockInfo var27 : var22) {
                        if (var4.setBlock(var27.pos, var27.state, 2)) {
                            ++var26;
                        }
                    }

                    for(CloneCommands.CloneBlockInfo var28 : var6) {
                        BlockEntity var29 = var4.getBlockEntity(var28.pos);
                        if (var28.tag != null && var29 != null) {
                            var28.tag.putInt("x", var28.pos.getX());
                            var28.tag.putInt("y", var28.pos.getY());
                            var28.tag.putInt("z", var28.pos.getZ());
                            var29.load(var28.tag);
                            var29.setChanged();
                        }

                        var4.setBlock(var28.pos, var28.state, 2);
                    }

                    for(CloneCommands.CloneBlockInfo var30 : var23) {
                        var4.blockUpdated(var30.pos, var30.state.getBlock());
                    }

                    var4.getBlockTicks().copy(var0, var9);
                    if (var26 == 0) {
                        throw ERROR_FAILED.create();
                    } else {
                        param0.sendSuccess(new TranslatableComponent("commands.clone.success", var26), true);
                        return var26;
                    }
                } else {
                    throw BlockPosArgument.ERROR_NOT_LOADED.create();
                }
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
