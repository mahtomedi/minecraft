package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
    private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.place.feature.failed")
    );
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.place.structure.failed")
    );
    private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.place.template.invalid", param0)
    );
    private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.place.template.failed")
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = (param0, param1) -> {
        StructureTemplateManager var0 = param0.getSource().getLevel().getStructureManager();
        return SharedSuggestionProvider.suggestResource(var0.listTemplates(), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("place")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("feature")
                        .then(
                            Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE))
                                .executes(
                                    param0x -> placeFeature(
                                            param0x.getSource(),
                                            ResourceKeyArgument.getConfiguredFeature(param0x, "feature"),
                                            BlockPos.containing(param0x.getSource().getPosition())
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
                            Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL))
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
                                                            BlockPos.containing(param0x.getSource().getPosition())
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
                            Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE))
                                .executes(
                                    param0x -> placeStructure(
                                            param0x.getSource(),
                                            ResourceKeyArgument.getStructure(param0x, "structure"),
                                            BlockPos.containing(param0x.getSource().getPosition())
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
                .then(
                    Commands.literal("template")
                        .then(
                            Commands.argument("template", ResourceLocationArgument.id())
                                .suggests(SUGGEST_TEMPLATES)
                                .executes(
                                    param0x -> placeTemplate(
                                            param0x.getSource(),
                                            ResourceLocationArgument.getId(param0x, "template"),
                                            BlockPos.containing(param0x.getSource().getPosition()),
                                            Rotation.NONE,
                                            Mirror.NONE,
                                            1.0F,
                                            0
                                        )
                                )
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(
                                            param0x -> placeTemplate(
                                                    param0x.getSource(),
                                                    ResourceLocationArgument.getId(param0x, "template"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                    Rotation.NONE,
                                                    Mirror.NONE,
                                                    1.0F,
                                                    0
                                                )
                                        )
                                        .then(
                                            Commands.argument("rotation", TemplateRotationArgument.templateRotation())
                                                .executes(
                                                    param0x -> placeTemplate(
                                                            param0x.getSource(),
                                                            ResourceLocationArgument.getId(param0x, "template"),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                            TemplateRotationArgument.getRotation(param0x, "rotation"),
                                                            Mirror.NONE,
                                                            1.0F,
                                                            0
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("mirror", TemplateMirrorArgument.templateMirror())
                                                        .executes(
                                                            param0x -> placeTemplate(
                                                                    param0x.getSource(),
                                                                    ResourceLocationArgument.getId(param0x, "template"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                    TemplateRotationArgument.getRotation(param0x, "rotation"),
                                                                    TemplateMirrorArgument.getMirror(param0x, "mirror"),
                                                                    1.0F,
                                                                    0
                                                                )
                                                        )
                                                        .then(
                                                            Commands.argument("integrity", FloatArgumentType.floatArg(0.0F, 1.0F))
                                                                .executes(
                                                                    param0x -> placeTemplate(
                                                                            param0x.getSource(),
                                                                            ResourceLocationArgument.getId(param0x, "template"),
                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                            TemplateRotationArgument.getRotation(param0x, "rotation"),
                                                                            TemplateMirrorArgument.getMirror(param0x, "mirror"),
                                                                            FloatArgumentType.getFloat(param0x, "integrity"),
                                                                            0
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("seed", IntegerArgumentType.integer())
                                                                        .executes(
                                                                            param0x -> placeTemplate(
                                                                                    param0x.getSource(),
                                                                                    ResourceLocationArgument.getId(param0x, "template"),
                                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                                    TemplateRotationArgument.getRotation(param0x, "rotation"),
                                                                                    TemplateMirrorArgument.getMirror(param0x, "mirror"),
                                                                                    FloatArgumentType.getFloat(param0x, "integrity"),
                                                                                    IntegerArgumentType.getInteger(param0x, "seed")
                                                                                )
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

    public static int placeFeature(CommandSourceStack param0, Holder.Reference<ConfiguredFeature<?, ?>> param1, BlockPos param2) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        ConfiguredFeature<?, ?> var1 = param1.value();
        ChunkPos var2 = new ChunkPos(param2);
        checkLoaded(var0, new ChunkPos(var2.x - 1, var2.z - 1), new ChunkPos(var2.x + 1, var2.z + 1));
        if (!var1.place(var0, var0.getChunkSource().getGenerator(), var0.getRandom(), param2)) {
            throw ERROR_FEATURE_FAILED.create();
        } else {
            String var3 = param1.key().location().toString();
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

    public static int placeStructure(CommandSourceStack param0, Holder.Reference<Structure> param1, BlockPos param2) throws CommandSyntaxException {
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
            String var7 = param1.key().location().toString();
            param0.sendSuccess(Component.translatable("commands.place.structure.success", var7, param2.getX(), param2.getY(), param2.getZ()), true);
            return 1;
        }
    }

    public static int placeTemplate(
        CommandSourceStack param0, ResourceLocation param1, BlockPos param2, Rotation param3, Mirror param4, float param5, int param6
    ) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        StructureTemplateManager var1 = var0.getStructureManager();

        Optional<StructureTemplate> var2;
        try {
            var2 = var1.get(param1);
        } catch (ResourceLocationException var13) {
            throw ERROR_TEMPLATE_INVALID.create(param1);
        }

        if (var2.isEmpty()) {
            throw ERROR_TEMPLATE_INVALID.create(param1);
        } else {
            StructureTemplate var5 = var2.get();
            checkLoaded(var0, new ChunkPos(param2), new ChunkPos(param2.offset(var5.getSize())));
            StructurePlaceSettings var6 = new StructurePlaceSettings().setMirror(param4).setRotation(param3);
            if (param5 < 1.0F) {
                var6.clearProcessors().addProcessor(new BlockRotProcessor(param5)).setRandom(StructureBlockEntity.createRandom((long)param6));
            }

            boolean var7 = var5.placeInWorld(var0, param2, param2, var6, StructureBlockEntity.createRandom((long)param6), 2);
            if (!var7) {
                throw ERROR_TEMPLATE_FAILED.create();
            } else {
                param0.sendSuccess(Component.translatable("commands.place.template.success", param1, param2.getX(), param2.getY(), param2.getZ()), true);
                return 1;
            }
        }
    }

    private static void checkLoaded(ServerLevel param0, ChunkPos param1, ChunkPos param2) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(param1, param2).filter(param1x -> !param0.isLoaded(param1x.getWorldPosition())).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }
}
