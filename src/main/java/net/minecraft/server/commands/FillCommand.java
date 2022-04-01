package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
    private static final int MAX_FILL_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.fill.toobig", param0, param1)
    );
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("fill")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("from", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("to", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("block", BlockStateArgument.block())
                                        .executes(
                                            param0x -> fillBlocks(
                                                    param0x.getSource(),
                                                    BoundingBox.fromCorners(
                                                        BlockPosArgument.getLoadedBlockPos(param0x, "from"), BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                    ),
                                                    BlockStateArgument.getBlock(param0x, "block"),
                                                    FillCommand.Mode.REPLACE,
                                                    null
                                                )
                                        )
                                        .then(
                                            Commands.literal("replace")
                                                .executes(
                                                    param0x -> fillBlocks(
                                                            param0x.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(param0x, "block"),
                                                            FillCommand.Mode.REPLACE,
                                                            null
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("filter", BlockPredicateArgument.blockPredicate())
                                                        .executes(
                                                            param0x -> fillBlocks(
                                                                    param0x.getSource(),
                                                                    BoundingBox.fromCorners(
                                                                        BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                        BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                                    ),
                                                                    BlockStateArgument.getBlock(param0x, "block"),
                                                                    FillCommand.Mode.REPLACE,
                                                                    BlockPredicateArgument.getBlockPredicate(param0x, "filter")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("keep")
                                                .executes(
                                                    param0x -> fillBlocks(
                                                            param0x.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(param0x, "block"),
                                                            FillCommand.Mode.REPLACE,
                                                            param0xx -> param0xx.getLevel().isEmptyBlock(param0xx.getPos())
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("outline")
                                                .executes(
                                                    param0x -> fillBlocks(
                                                            param0x.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(param0x, "block"),
                                                            FillCommand.Mode.OUTLINE,
                                                            null
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("hollow")
                                                .executes(
                                                    param0x -> fillBlocks(
                                                            param0x.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(param0x, "block"),
                                                            FillCommand.Mode.HOLLOW,
                                                            null
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("destroy")
                                                .executes(
                                                    param0x -> fillBlocks(
                                                            param0x.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(param0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(param0x, "block"),
                                                            FillCommand.Mode.DESTROY,
                                                            null
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int fillBlocks(
        CommandSourceStack param0, BoundingBox param1, BlockInput param2, FillCommand.Mode param3, @Nullable Predicate<BlockInWorld> param4
    ) throws CommandSyntaxException {
        int var0 = param1.getXSpan() * param1.getYSpan() * param1.getZSpan();
        if (var0 > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, var0);
        } else {
            List<BlockPos> var1 = Lists.newArrayList();
            ServerLevel var2 = param0.getLevel();
            int var3 = 0;

            for(BlockPos var4 : BlockPos.betweenClosed(param1.minX(), param1.minY(), param1.minZ(), param1.maxX(), param1.maxY(), param1.maxZ())) {
                if (param4 == null || param4.test(new BlockInWorld(var2, var4, true))) {
                    BlockInput var5 = param3.filter.filter(param1, var4, param2, var2);
                    if (var5 != null) {
                        BlockEntity var6 = var2.getBlockEntity(var4);
                        Clearable.tryClear(var6);
                        if (var5.place(var2, var4, 2)) {
                            var1.add(var4.immutable());
                            ++var3;
                        }
                    }
                }
            }

            for(BlockPos var7 : var1) {
                Block var8 = var2.getBlockState(var7).getBlock();
                var2.blockUpdated(var7, var8);
            }

            if (var3 == 0) {
                throw ERROR_FAILED.create();
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.fill.success", var3), true);
                return var3;
            }
        }
    }

    static enum Mode {
        REPLACE((param0, param1, param2, param3) -> param2),
        OUTLINE(
            (param0, param1, param2, param3) -> param1.getX() != param0.minX()
                        && param1.getX() != param0.maxX()
                        && param1.getY() != param0.minY()
                        && param1.getY() != param0.maxY()
                        && param1.getZ() != param0.minZ()
                        && param1.getZ() != param0.maxZ()
                    ? null
                    : param2
        ),
        HOLLOW(
            (param0, param1, param2, param3) -> param1.getX() != param0.minX()
                        && param1.getX() != param0.maxX()
                        && param1.getY() != param0.minY()
                        && param1.getY() != param0.maxY()
                        && param1.getZ() != param0.minZ()
                        && param1.getZ() != param0.maxZ()
                    ? FillCommand.HOLLOW_CORE
                    : param2
        ),
        DESTROY((param0, param1, param2, param3) -> {
            param3.destroyBlock(param1, true);
            return param2;
        });

        public final SetBlockCommand.Filter filter;

        private Mode(SetBlockCommand.Filter param0) {
            this.filter = param0;
        }
    }
}
