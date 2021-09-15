package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResetChunksCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("resetchunks")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> resetChunks(param0x.getSource(), 0))
                .then(
                    Commands.argument("range", IntegerArgumentType.integer(0, 5))
                        .executes(param0x -> resetChunks(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "range")))
                )
        );
    }

    private static int resetChunks(CommandSourceStack param0, int param1) {
        ServerLevel var0 = param0.getLevel();
        ServerChunkCache var1 = var0.getChunkSource();
        var1.chunkMap.debugReloadGenerator();
        Vec3 var2 = param0.getPosition();
        ChunkPos var3 = new ChunkPos(new BlockPos(var2));

        for(int var4 = var3.z - param1; var4 <= var3.z + param1; ++var4) {
            for(int var5 = var3.x - param1; var5 <= var3.x + param1; ++var5) {
                ChunkPos var6 = new ChunkPos(var5, var4);

                for(BlockPos var7 : BlockPos.betweenClosed(
                    var6.getMinBlockX(), var0.getMinBuildHeight(), var6.getMinBlockZ(), var6.getMaxBlockX(), var0.getMaxBuildHeight() - 1, var6.getMaxBlockZ()
                )) {
                    var0.setBlock(var7, Blocks.AIR.defaultBlockState(), 16);
                }
            }
        }

        ProcessorMailbox<Runnable> var8 = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
        long var9 = System.currentTimeMillis();
        int var10 = (param1 * 2 + 1) * (param1 * 2 + 1);

        for(ChunkStatus var11 : ImmutableList.of(
            ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES
        )) {
            long var12 = System.currentTimeMillis();
            CompletableFuture<Unit> var13 = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, var8::tell);

            for(int var14 = var3.z - param1; var14 <= var3.z + param1; ++var14) {
                for(int var15 = var3.x - param1; var15 <= var3.x + param1; ++var15) {
                    ChunkPos var16 = new ChunkPos(var15, var14);
                    List<ChunkAccess> var17 = Lists.newArrayList();
                    int var18 = Math.max(1, var11.getRange());

                    for(int var19 = var16.z - var18; var19 <= var16.z + var18; ++var19) {
                        for(int var20 = var16.x - var18; var20 <= var16.x + var18; ++var20) {
                            ChunkAccess var21 = var1.getChunk(var20, var19, var11.getParent(), true);
                            ChunkAccess var22;
                            if (var21 instanceof ImposterProtoChunk) {
                                var22 = new ImposterProtoChunk(((ImposterProtoChunk)var21).getWrapped(), true);
                            } else if (var21 instanceof LevelChunk) {
                                var22 = new ImposterProtoChunk((LevelChunk)var21, true);
                            } else {
                                var22 = var21;
                            }

                            var17.add(var22);
                        }
                    }

                    var13 = var13.thenComposeAsync(
                        param5 -> var11.generate(
                                    var8::tell, var0, var0.getChunkSource().getGenerator(), var0.getStructureManager(), var1.getLightEngine(), param0x -> {
                                        throw new UnsupportedOperationException("Not creating full chunks here");
                                    }, var17, true
                                )
                                .thenApply(param1x -> {
                                    if (var11 == ChunkStatus.NOISE) {
                                        param1x.left().ifPresent(param0x -> Heightmap.primeHeightmaps(param0x, ChunkStatus.POST_FEATURES));
                                    }
        
                                    return Unit.INSTANCE;
                                }),
                        var8::tell
                    );
                }
            }

            param0.getServer().managedBlock(var13::isDone);
            LOGGER.debug(var11.getName() + " took " + (System.currentTimeMillis() - var12) + " ms");
        }

        long var25 = System.currentTimeMillis();

        for(int var26 = var3.z - param1; var26 <= var3.z + param1; ++var26) {
            for(int var27 = var3.x - param1; var27 <= var3.x + param1; ++var27) {
                ChunkPos var28 = new ChunkPos(var27, var26);

                for(BlockPos var29 : BlockPos.betweenClosed(
                    var28.getMinBlockX(),
                    var0.getMinBuildHeight(),
                    var28.getMinBlockZ(),
                    var28.getMaxBlockX(),
                    var0.getMaxBuildHeight() - 1,
                    var28.getMaxBlockZ()
                )) {
                    var1.blockChanged(var29);
                }
            }
        }

        LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - var25) + " ms");
        long var30 = System.currentTimeMillis() - var9;
        param0.sendSuccess(
            new TextComponent(
                String.format(
                    "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", var10, var30, var10, (float)var30 / (float)var10
                )
            ),
            true
        );
        return 1;
    }
}
