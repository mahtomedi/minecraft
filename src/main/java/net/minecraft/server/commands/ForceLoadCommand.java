package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;

public class ForceLoadCommand {
    private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.forceload.toobig", param0, param1)
    );
    private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.forceload.query.failure", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.forceload.added.failure")
    );
    private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.forceload.removed.failure")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("forceload")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("from", ColumnPosArgument.columnPos())
                                .executes(
                                    param0x -> changeForceLoad(
                                            param0x.getSource(),
                                            ColumnPosArgument.getColumnPos(param0x, "from"),
                                            ColumnPosArgument.getColumnPos(param0x, "from"),
                                            true
                                        )
                                )
                                .then(
                                    Commands.argument("to", ColumnPosArgument.columnPos())
                                        .executes(
                                            param0x -> changeForceLoad(
                                                    param0x.getSource(),
                                                    ColumnPosArgument.getColumnPos(param0x, "from"),
                                                    ColumnPosArgument.getColumnPos(param0x, "to"),
                                                    true
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("from", ColumnPosArgument.columnPos())
                                .executes(
                                    param0x -> changeForceLoad(
                                            param0x.getSource(),
                                            ColumnPosArgument.getColumnPos(param0x, "from"),
                                            ColumnPosArgument.getColumnPos(param0x, "from"),
                                            false
                                        )
                                )
                                .then(
                                    Commands.argument("to", ColumnPosArgument.columnPos())
                                        .executes(
                                            param0x -> changeForceLoad(
                                                    param0x.getSource(),
                                                    ColumnPosArgument.getColumnPos(param0x, "from"),
                                                    ColumnPosArgument.getColumnPos(param0x, "to"),
                                                    false
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("all").executes(param0x -> removeAll(param0x.getSource())))
                )
                .then(
                    Commands.literal("query")
                        .executes(param0x -> listForceLoad(param0x.getSource()))
                        .then(
                            Commands.argument("pos", ColumnPosArgument.columnPos())
                                .executes(param0x -> queryForceLoad(param0x.getSource(), ColumnPosArgument.getColumnPos(param0x, "pos")))
                        )
                )
        );
    }

    private static int queryForceLoad(CommandSourceStack param0, ColumnPos param1) throws CommandSyntaxException {
        ChunkPos var0 = new ChunkPos(param1.x >> 4, param1.z >> 4);
        ResourceKey<DimensionType> var1 = param0.getLevel().dimension();
        boolean var2 = param0.getServer().getLevel(var1).getForcedChunks().contains(var0.toLong());
        if (var2) {
            param0.sendSuccess(new TranslatableComponent("commands.forceload.query.success", var0, var1.location()), false);
            return 1;
        } else {
            throw ERROR_NOT_TICKING.create(var0, var1.location());
        }
    }

    private static int listForceLoad(CommandSourceStack param0) {
        ResourceKey<DimensionType> var0 = param0.getLevel().dimension();
        LongSet var1 = param0.getServer().getLevel(var0).getForcedChunks();
        int var2 = var1.size();
        if (var2 > 0) {
            String var3 = Joiner.on(", ").join(var1.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if (var2 == 1) {
                param0.sendSuccess(new TranslatableComponent("commands.forceload.list.single", var0.location(), var3), false);
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.forceload.list.multiple", var2, var0.location(), var3), false);
            }
        } else {
            param0.sendFailure(new TranslatableComponent("commands.forceload.added.none", var0.location()));
        }

        return var2;
    }

    private static int removeAll(CommandSourceStack param0) {
        ResourceKey<DimensionType> var0 = param0.getLevel().dimension();
        ServerLevel var1 = param0.getServer().getLevel(var0);
        LongSet var2 = var1.getForcedChunks();
        var2.forEach(param1 -> var1.setChunkForced(ChunkPos.getX(param1), ChunkPos.getZ(param1), false));
        param0.sendSuccess(new TranslatableComponent("commands.forceload.removed.all", var0.location()), true);
        return 0;
    }

    private static int changeForceLoad(CommandSourceStack param0, ColumnPos param1, ColumnPos param2, boolean param3) throws CommandSyntaxException {
        int var0 = Math.min(param1.x, param2.x);
        int var1 = Math.min(param1.z, param2.z);
        int var2 = Math.max(param1.x, param2.x);
        int var3 = Math.max(param1.z, param2.z);
        if (var0 >= -30000000 && var1 >= -30000000 && var2 < 30000000 && var3 < 30000000) {
            int var4 = var0 >> 4;
            int var5 = var1 >> 4;
            int var6 = var2 >> 4;
            int var7 = var3 >> 4;
            long var8 = ((long)(var6 - var4) + 1L) * ((long)(var7 - var5) + 1L);
            if (var8 > 256L) {
                throw ERROR_TOO_MANY_CHUNKS.create(256, var8);
            } else {
                ResourceKey<DimensionType> var9 = param0.getLevel().dimension();
                ServerLevel var10 = param0.getServer().getLevel(var9);
                ChunkPos var11 = null;
                int var12 = 0;

                for(int var13 = var4; var13 <= var6; ++var13) {
                    for(int var14 = var5; var14 <= var7; ++var14) {
                        boolean var15 = var10.setChunkForced(var13, var14, param3);
                        if (var15) {
                            ++var12;
                            if (var11 == null) {
                                var11 = new ChunkPos(var13, var14);
                            }
                        }
                    }
                }

                if (var12 == 0) {
                    throw (param3 ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
                } else {
                    if (var12 == 1) {
                        param0.sendSuccess(
                            new TranslatableComponent("commands.forceload." + (param3 ? "added" : "removed") + ".single", var11, var9.location()), true
                        );
                    } else {
                        ChunkPos var16 = new ChunkPos(var4, var5);
                        ChunkPos var17 = new ChunkPos(var6, var7);
                        param0.sendSuccess(
                            new TranslatableComponent(
                                "commands.forceload." + (param3 ? "added" : "removed") + ".multiple", var12, var9.location(), var16, var17
                            ),
                            true
                        );
                    }

                    return var12;
                }
            }
        } else {
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        }
    }
}
