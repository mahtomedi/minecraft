package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

public class ResetChunksCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("resetchunks")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> resetChunks(param0x.getSource(), 0, true))
                .then(
                    Commands.argument("range", IntegerArgumentType.integer(0, 5))
                        .executes(param0x -> resetChunks(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "range"), true))
                        .then(
                            Commands.argument("skipOldChunks", BoolArgumentType.bool())
                                .executes(
                                    param0x -> resetChunks(
                                            param0x.getSource(),
                                            IntegerArgumentType.getInteger(param0x, "range"),
                                            BoolArgumentType.getBool(param0x, "skipOldChunks")
                                        )
                                )
                        )
                )
        );
    }

    private static int resetChunks(CommandSourceStack param0, int param1, boolean param2) {
        ServerLevel var0 = param0.getLevel();
        ServerChunkCache var1 = var0.getChunkSource();
        var1.chunkMap.debugReloadGenerator();
        Vec3 var2 = param0.getPosition();
        ChunkPos var3 = new ChunkPos(new BlockPos(var2));
        int var4 = var3.z - param1;
        int var5 = var3.z + param1;
        int var6 = var3.x - param1;
        int var7 = var3.x + param1;

        for(int var8 = var4; var8 <= var5; ++var8) {
            for(int var9 = var6; var9 <= var7; ++var9) {
                ChunkPos var10 = new ChunkPos(var9, var8);
                LevelChunk var11 = var1.getChunk(var9, var8, false);
                if (var11 != null && (!param2 || !var11.isOldNoiseGeneration())) {
                    for(BlockPos var12 : BlockPos.betweenClosed(
                        var10.getMinBlockX(),
                        var0.getMinBuildHeight(),
                        var10.getMinBlockZ(),
                        var10.getMaxBlockX(),
                        var0.getMaxBuildHeight() - 1,
                        var10.getMaxBlockZ()
                    )) {
                        var0.setBlock(var12, Blocks.AIR.defaultBlockState(), 16);
                    }
                }
            }
        }

        ProcessorMailbox<Runnable> var13 = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
        long var14 = System.currentTimeMillis();
        int var15 = (param1 * 2 + 1) * (param1 * 2 + 1);

        for(ChunkStatus var16 : ImmutableList.of(
            ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES
        )) {
            long var17 = System.currentTimeMillis();
            CompletableFuture<Unit> var18 = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, var13::tell);

            for(int var19 = var3.z - param1; var19 <= var3.z + param1; ++var19) {
                for(int var20 = var3.x - param1; var20 <= var3.x + param1; ++var20) {
                    ChunkPos var21 = new ChunkPos(var20, var19);
                    LevelChunk var22 = var1.getChunk(var20, var19, false);
                    if (var22 != null && (!param2 || !var22.isOldNoiseGeneration())) {
                        List<ChunkAccess> var23 = Lists.newArrayList();
                        int var24 = Math.max(1, var16.getRange());

                        for(int var25 = var21.z - var24; var25 <= var21.z + var24; ++var25) {
                            for(int var26 = var21.x - var24; var26 <= var21.x + var24; ++var26) {
                                ChunkAccess var27 = var1.getChunk(var26, var25, var16.getParent(), true);
                                ChunkAccess var28;
                                if (var27 instanceof ImposterProtoChunk) {
                                    var28 = new ImposterProtoChunk(((ImposterProtoChunk)var27).getWrapped(), true);
                                } else if (var27 instanceof LevelChunk) {
                                    var28 = new ImposterProtoChunk((LevelChunk)var27, true);
                                } else {
                                    var28 = var27;
                                }

                                var23.add(var28);
                            }
                        }

                        var18 = var18.thenComposeAsync(
                            param5 -> var16.generate(var13::tell, var0, var1.getGenerator(), var0.getStructureManager(), var1.getLightEngine(), param0x -> {
                                    throw new UnsupportedOperationException("Not creating full chunks here");
                                }, var23, true).thenApply(param1x -> {
                                    if (var16 == ChunkStatus.NOISE) {
                                        param1x.left().ifPresent(param0x -> Heightmap.primeHeightmaps(param0x, ChunkStatus.POST_FEATURES));
                                    }
    
                                    return Unit.INSTANCE;
                                }), var13::tell
                        );
                    }
                }
            }

            param0.getServer().managedBlock(var18::isDone);
            LOGGER.debug(var16.getName() + " took " + (System.currentTimeMillis() - var17) + " ms");
        }

        long var31 = System.currentTimeMillis();

        for(int var32 = var3.z - param1; var32 <= var3.z + param1; ++var32) {
            for(int var33 = var3.x - param1; var33 <= var3.x + param1; ++var33) {
                ChunkPos var34 = new ChunkPos(var33, var32);
                LevelChunk var35 = var1.getChunk(var33, var32, false);
                if (var35 != null && (!param2 || !var35.isOldNoiseGeneration())) {
                    for(BlockPos var36 : BlockPos.betweenClosed(
                        var34.getMinBlockX(),
                        var0.getMinBuildHeight(),
                        var34.getMinBlockZ(),
                        var34.getMaxBlockX(),
                        var0.getMaxBuildHeight() - 1,
                        var34.getMaxBlockZ()
                    )) {
                        var1.blockChanged(var36);
                    }
                }
            }
        }

        LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - var31) + " ms");
        long var37 = System.currentTimeMillis() - var14;
        param0.sendSuccess(
            new TextComponent(
                String.format(
                    "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", var15, var37, var15, (float)var37 / (float)var15
                )
            ),
            true
        );
        return 1;
    }
}
