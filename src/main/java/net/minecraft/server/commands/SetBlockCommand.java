package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.setblock.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("setblock")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("block", BlockStateArgument.block())
                                .executes(
                                    param0x -> setBlock(
                                            param0x.getSource(),
                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                            BlockStateArgument.getBlock(param0x, "block"),
                                            SetBlockCommand.Mode.REPLACE,
                                            null
                                        )
                                )
                                .then(
                                    Commands.literal("destroy")
                                        .executes(
                                            param0x -> setBlock(
                                                    param0x.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                    BlockStateArgument.getBlock(param0x, "block"),
                                                    SetBlockCommand.Mode.DESTROY,
                                                    null
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("keep")
                                        .executes(
                                            param0x -> setBlock(
                                                    param0x.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                    BlockStateArgument.getBlock(param0x, "block"),
                                                    SetBlockCommand.Mode.REPLACE,
                                                    param0xx -> param0xx.getLevel().isEmptyBlock(param0xx.getPos())
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("replace")
                                        .executes(
                                            param0x -> setBlock(
                                                    param0x.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                    BlockStateArgument.getBlock(param0x, "block"),
                                                    SetBlockCommand.Mode.REPLACE,
                                                    null
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setBlock(
        CommandSourceStack param0, BlockPos param1, BlockInput param2, SetBlockCommand.Mode param3, @Nullable Predicate<BlockInWorld> param4
    ) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        if (param4 != null && !param4.test(new BlockInWorld(var0, param1, true))) {
            throw ERROR_FAILED.create();
        } else {
            boolean var1;
            if (param3 == SetBlockCommand.Mode.DESTROY) {
                var0.destroyBlock(param1, true);
                var1 = !param2.getState().isAir() || !var0.getBlockState(param1).isAir();
            } else {
                BlockEntity var2 = var0.getBlockEntity(param1);
                Clearable.tryClear(var2);
                var1 = true;
            }

            if (var1 && !param2.place(var0, param1, 2)) {
                throw ERROR_FAILED.create();
            } else {
                var0.blockUpdated(param1, param2.getState().getBlock());
                param0.sendSuccess(new TranslatableComponent("commands.setblock.success", param1.getX(), param1.getY(), param1.getZ()), true);
                return 1;
            }
        }
    }

    public interface Filter {
        @Nullable
        BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
    }

    public static enum Mode {
        REPLACE,
        DESTROY;
    }
}
