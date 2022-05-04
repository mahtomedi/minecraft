package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class PlaceCommand {
    private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.place.feature.failed")
    );
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.place.structure.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("place")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("feature")
                        .then(
                            Commands.argument("feature", ResourceKeyArgument.key(Registry.CONFIGURED_FEATURE_REGISTRY))
                                .executes(
                                    param0x -> placeFeature(
                                            param0x.getSource(),
                                            ResourceKeyArgument.getConfiguredFeature(param0x, "feature"),
                                            new BlockPos(param0x.getSource().getPosition())
                                        )
                                )
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(
                                            param0x -> placeFeature(
                                                    param0x.getSource(),
                                                    ResourceKeyArgument.getConfiguredFeature(param0x, "feature"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("jigsaw")
                        .then(
                            Commands.argument("pool", ResourceKeyArgument.key(Registry.TEMPLATE_POOL_REGISTRY))
                                .then(
                                    Commands.argument("target", ResourceLocationArgument.id())
                                        .then(
                                            Commands.argument("max_depth", IntegerArgumentType.integer(1, 7))
                                                .executes(
                                                    param0x -> placeJigsaw(
                                                            param0x.getSource(),
                                                            ResourceKeyArgument.getStructureTemplatePool(param0x, "pool"),
                                                            ResourceLocationArgument.getId(param0x, "target"),
                                                            IntegerArgumentType.getInteger(param0x, "max_depth"),
                                                            new BlockPos(param0x.getSource().getPosition())
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("position", BlockPosArgument.blockPos())
                                                        .executes(
                                                            param0x -> placeJigsaw(
                                                                    param0x.getSource(),
                                                                    ResourceKeyArgument.getStructureTemplatePool(param0x, "pool"),
                                                                    ResourceLocationArgument.getId(param0x, "target"),
                                                                    IntegerArgumentType.getInteger(param0x, "max_depth"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "position")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("structure")
                        .then(
                            Commands.argument("structure", ResourceKeyArgument.key(Registry.STRUCTURE_REGISTRY))
                                .executes(
                                    param0x -> placeStructure(
                                            param0x.getSource(),
                                            ResourceKeyArgument.getStructure(param0x, "structure"),
                                            new BlockPos(param0x.getSource().getPosition())
                                        )
                                )
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(
                                            param0x -> placeStructure(
                                                    param0x.getSource(),
                                                    ResourceKeyArgument.getStructure(param0x, "structure"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    public static int placeFeature(CommandSourceStack param0, Holder<ConfiguredFeature<?, ?>> param1, BlockPos param2) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        ConfiguredFeature<?, ?> var1 = param1.value();
        ChunkPos var2 = new ChunkPos(param2);
        checkLoaded(var0, new ChunkPos(var2.x - 1, var2.z - 1), new ChunkPos(var2.x + 1, var2.z + 1));
        if (!var1.place(var0, var0.getChunkSource().getGenerator(), var0.getRandom(), param2)) {
            throw ERROR_FEATURE_FAILED.create();
        } else {
            String var3 = param1.unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]");
            param0.sendSuccess(Component.translatable("commands.place.feature.success", var3, param2.getX(), param2.getY(), param2.getZ()), true);
            return 1;
        }
    }

    public static int placeJigsaw(CommandSourceStack param0, Holder<StructureTemplatePool> param1, ResourceLocation param2, int param3, BlockPos param4) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        if (!JigsawPlacement.generateJigsaw(var0, param1, param2, param3, param4, false)) {
            throw ERROR_JIGSAW_FAILED.create();
        } else {
            param0.sendSuccess(Component.translatable("commands.place.jigsaw.success", param4.getX(), param4.getY(), param4.getZ()), true);
            return 1;
        }
    }

    public static int placeStructure(CommandSourceStack param0, Holder<Structure> param1, BlockPos param2) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        Structure var1 = param1.value();
        ChunkGenerator var2 = var0.getChunkSource().getGenerator();
        StructureStart var3 = var1.generate(
            param0.registryAccess(),
            var2,
            var2.getBiomeSource(),
            var0.getChunkSource().randomState(),
            var0.getStructureManager(),
            var0.getSeed(),
            new ChunkPos(param2),
            0,
            var0,
            param0x -> true
        );
        if (!var3.isValid()) {
            throw ERROR_STRUCTURE_FAILED.create();
        } else {
            BoundingBox var4 = var3.getBoundingBox();
            ChunkPos var5 = new ChunkPos(SectionPos.blockToSectionCoord(var4.minX()), SectionPos.blockToSectionCoord(var4.minZ()));
            ChunkPos var6 = new ChunkPos(SectionPos.blockToSectionCoord(var4.maxX()), SectionPos.blockToSectionCoord(var4.maxZ()));
            checkLoaded(var0, var5, var6);
            ChunkPos.rangeClosed(var5, var6)
                .forEach(
                    param3 -> var3.placeInChunk(
                            var0,
                            var0.structureManager(),
                            var2,
                            var0.getRandom(),
                            new BoundingBox(
                                param3.getMinBlockX(),
                                var0.getMinBuildHeight(),
                                param3.getMinBlockZ(),
                                param3.getMaxBlockX(),
                                var0.getMaxBuildHeight(),
                                param3.getMaxBlockZ()
                            ),
                            param3
                        )
                );
            String var7 = param1.unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]");
            param0.sendSuccess(Component.translatable("commands.place.structure.success", var7, param2.getX(), param2.getY(), param2.getZ()), true);
            return 1;
        }
    }

    private static void checkLoaded(ServerLevel param0, ChunkPos param1, ChunkPos param2) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(param1, param2).filter(param1x -> !param0.isLoaded(param1x.getWorldPosition())).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }
}
