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
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
    private static final int MAX_CHUNK_LIMIT = 256;
    private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.forceload.toobig", param0, param1)
    );
    private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.forceload.query.failure", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.added.failure"));
    private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(
        Component.translatable("commands.forceload.removed.failure")
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
        ChunkPos var0 = param1.toChunkPos();
        ServerLevel var1 = param0.getLevel();
        ResourceKey<Level> var2 = var1.dimension();
        boolean var3 = var1.getForcedChunks().contains(var0.toLong());
        if (var3) {
            param0.sendSuccess(() -> Component.translatable("commands.forceload.query.success", var0, var2.location()), false);
            return 1;
        } else {
            throw ERROR_NOT_TICKING.create(var0, var2.location());
        }
    }

    private static int listForceLoad(CommandSourceStack param0) {
        ServerLevel var0 = param0.getLevel();
        ResourceKey<Level> var1 = var0.dimension();
        LongSet var2 = var0.getForcedChunks();
        int var3 = var2.size();
        if (var3 > 0) {
            String var4 = Joiner.on(", ").join(var2.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if (var3 == 1) {
                param0.sendSuccess(() -> Component.translatable("commands.forceload.list.single", var1.location(), var4), false);
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.forceload.list.multiple", var3, var1.location(), var4), false);
            }
        } else {
            param0.sendFailure(Component.translatable("commands.forceload.added.none", var1.location()));
        }

        return var3;
    }

    private static int removeAll(CommandSourceStack param0) {
        ServerLevel var0 = param0.getLevel();
        ResourceKey<Level> var1 = var0.dimension();
        LongSet var2 = var0.getForcedChunks();
        var2.forEach(param1 -> var0.setChunkForced(ChunkPos.getX(param1), ChunkPos.getZ(param1), false));
        param0.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", var1.location()), true);
        return 0;
    }

    private static int changeForceLoad(CommandSourceStack param0, ColumnPos param1, ColumnPos param2, boolean param3) throws CommandSyntaxException {
        int var0 = Math.min(param1.x(), param2.x());
        int var1 = Math.min(param1.z(), param2.z());
        int var2 = Math.max(param1.x(), param2.x());
        int var3 = Math.max(param1.z(), param2.z());
        if (var0 >= -30000000 && var1 >= -30000000 && var2 < 30000000 && var3 < 30000000) {
            int var4 = SectionPos.blockToSectionCoord(var0);
            int var5 = SectionPos.blockToSectionCoord(var1);
            int var6 = SectionPos.blockToSectionCoord(var2);
            int var7 = SectionPos.blockToSectionCoord(var3);
            long var8 = ((long)(var6 - var4) + 1L) * ((long)(var7 - var5) + 1L);
            if (var8 > 256L) {
                throw ERROR_TOO_MANY_CHUNKS.create(256, var8);
            } else {
                ServerLevel var9 = param0.getLevel();
                ResourceKey<Level> var10 = var9.dimension();
                ChunkPos var11 = null;
                int var12 = 0;

                for(int var13 = var4; var13 <= var6; ++var13) {
                    for(int var14 = var5; var14 <= var7; ++var14) {
                        boolean var15 = var9.setChunkForced(var13, var14, param3);
                        if (var15) {
                            ++var12;
                            if (var11 == null) {
                                var11 = new ChunkPos(var13, var14);
                            }
                        }
                    }
                }

                ChunkPos var16 = var11;
                if (var12 == 0) {
                    throw (param3 ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
                } else {
                    if (var12 == 1) {
                        param0.sendSuccess(
                            () -> Component.translatable("commands.forceload." + (param3 ? "added" : "removed") + ".single", var16, var10.location()), true
                        );
                    } else {
                        ChunkPos var17 = new ChunkPos(var4, var5);
                        ChunkPos var18 = new ChunkPos(var6, var7);
                        param0.sendSuccess(
                            () -> Component.translatable(
                                    "commands.forceload." + (param3 ? "added" : "removed") + ".multiple", var16, var10.location(), var17, var18
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
