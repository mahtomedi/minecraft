package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
    public static String testStructuresDir = "gameteststructures";
    private static final int HOW_MANY_CHUNKS_TO_LOAD_IN_EACH_DIRECTION_OF_STRUCTURE = 4;

    public static Rotation getRotationForRotationSteps(int param0) {
        switch(param0) {
            case 0:
                return Rotation.NONE;
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + param0);
        }
    }

    public static int getRotationStepsForRotation(Rotation param0) {
        switch(param0) {
            case NONE:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case CLOCKWISE_180:
                return 2;
            case COUNTERCLOCKWISE_90:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + param0);
        }
    }

    public static void main(String[] param0) throws IOException {
        Bootstrap.bootStrap();
        Files.walk(Paths.get(testStructuresDir)).filter(param0x -> param0x.toString().endsWith(".snbt")).forEach(param0x -> {
            try {
                String var0 = new String(Files.readAllBytes(param0x), StandardCharsets.UTF_8);
                CompoundTag var1 = NbtUtils.snbtToStructure(var0);
                CompoundTag var2 = StructureUpdater.update(param0x.toString(), var1);
                NbtToSnbt.writeSnbt(param0x, NbtUtils.structureToSnbt(var2));
            } catch (IOException | CommandSyntaxException var4) {
                LOGGER.error("Something went wrong upgrading: {}", param0x, var4);
            }

        });
    }

    public static AABB getStructureBounds(StructureBlockEntity param0) {
        BlockPos var0 = param0.getBlockPos();
        BlockPos var1 = var0.offset(param0.getStructureSize().offset(-1, -1, -1));
        BlockPos var2 = StructureTemplate.transform(var1, Mirror.NONE, param0.getRotation(), var0);
        return new AABB(var0, var2);
    }

    public static BoundingBox getStructureBoundingBox(StructureBlockEntity param0) {
        BlockPos var0 = param0.getBlockPos();
        BlockPos var1 = var0.offset(param0.getStructureSize().offset(-1, -1, -1));
        BlockPos var2 = StructureTemplate.transform(var1, Mirror.NONE, param0.getRotation(), var0);
        return BoundingBox.fromCorners(var0, var2);
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPos param0, BlockPos param1, Rotation param2, ServerLevel param3) {
        BlockPos var0 = StructureTemplate.transform(param0.offset(param1), Mirror.NONE, param2, param0);
        param3.setBlockAndUpdate(var0, Blocks.COMMAND_BLOCK.defaultBlockState());
        CommandBlockEntity var1 = (CommandBlockEntity)param3.getBlockEntity(var0);
        var1.getCommandBlock().setCommand("test runthis");
        BlockPos var2 = StructureTemplate.transform(var0.offset(0, 0, -1), Mirror.NONE, param2, var0);
        param3.setBlockAndUpdate(var2, Blocks.STONE_BUTTON.defaultBlockState().rotate(param2));
    }

    public static void createNewEmptyStructureBlock(String param0, BlockPos param1, Vec3i param2, Rotation param3, ServerLevel param4) {
        BoundingBox var0 = getStructureBoundingBox(param1, param2, param3);
        clearSpaceForStructure(var0, param1.getY(), param4);
        param4.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var1 = (StructureBlockEntity)param4.getBlockEntity(param1);
        var1.setIgnoreEntities(false);
        var1.setStructureName(new ResourceLocation(param0));
        var1.setStructureSize(param2);
        var1.setMode(StructureMode.SAVE);
        var1.setShowBoundingBox(true);
    }

    public static StructureBlockEntity spawnStructure(String param0, BlockPos param1, Rotation param2, int param3, ServerLevel param4, boolean param5) {
        Vec3i var0 = getStructureTemplate(param0, param4).getSize();
        BoundingBox var1 = getStructureBoundingBox(param1, var0, param2);
        BlockPos var2;
        if (param2 == Rotation.NONE) {
            var2 = param1;
        } else if (param2 == Rotation.CLOCKWISE_90) {
            var2 = param1.offset(var0.getZ() - 1, 0, 0);
        } else if (param2 == Rotation.CLOCKWISE_180) {
            var2 = param1.offset(var0.getX() - 1, 0, var0.getZ() - 1);
        } else {
            if (param2 != Rotation.COUNTERCLOCKWISE_90) {
                throw new IllegalArgumentException("Invalid rotation: " + param2);
            }

            var2 = param1.offset(0, 0, var0.getX() - 1);
        }

        forceLoadChunks(param1, param4);
        clearSpaceForStructure(var1, param1.getY(), param4);
        StructureBlockEntity var7 = createStructureBlock(param0, var2, param2, param4, param5);
        param4.getBlockTicks().fetchTicksInArea(var1, true, false);
        param4.clearBlockEvents(var1);
        return var7;
    }

    private static void forceLoadChunks(BlockPos param0, ServerLevel param1) {
        ChunkPos var0 = new ChunkPos(param0);

        for(int var1 = -1; var1 < 4; ++var1) {
            for(int var2 = -1; var2 < 4; ++var2) {
                int var3 = var0.x + var1;
                int var4 = var0.z + var2;
                param1.setChunkForced(var3, var4, true);
            }
        }

    }

    public static void clearSpaceForStructure(BoundingBox param0, int param1, ServerLevel param2) {
        BoundingBox var0 = new BoundingBox(param0.minX() - 2, param0.minY() - 3, param0.minZ() - 3, param0.maxX() + 3, param0.maxY() + 20, param0.maxZ() + 3);
        BlockPos.betweenClosedStream(var0).forEach(param2x -> clearBlock(param1, param2x, param2));
        param2.getBlockTicks().fetchTicksInArea(var0, true, false);
        param2.clearBlockEvents(var0);
        AABB var1 = new AABB((double)var0.minX(), (double)var0.minY(), (double)var0.minZ(), (double)var0.maxX(), (double)var0.maxY(), (double)var0.maxZ());
        List<Entity> var2 = param2.getEntitiesOfClass(Entity.class, var1, param0x -> !(param0x instanceof Player));
        var2.forEach(Entity::discard);
    }

    public static BoundingBox getStructureBoundingBox(BlockPos param0, Vec3i param1, Rotation param2) {
        BlockPos var0 = param0.offset(param1).offset(-1, -1, -1);
        BlockPos var1 = StructureTemplate.transform(var0, Mirror.NONE, param2, param0);
        BoundingBox var2 = BoundingBox.fromCorners(param0, var1);
        int var3 = Math.min(var2.minX(), var2.maxX());
        int var4 = Math.min(var2.minZ(), var2.maxZ());
        return var2.move(param0.getX() - var3, 0, param0.getZ() - var4);
    }

    public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos param0, int param1, ServerLevel param2) {
        return findStructureBlocks(param0, param1, param2).stream().filter(param2x -> doesStructureContain(param2x, param0, param2)).findFirst();
    }

    @Nullable
    public static BlockPos findNearestStructureBlock(BlockPos param0, int param1, ServerLevel param2) {
        Comparator<BlockPos> var0 = Comparator.comparingInt(param1x -> param1x.distManhattan(param0));
        Collection<BlockPos> var1 = findStructureBlocks(param0, param1, param2);
        Optional<BlockPos> var2 = var1.stream().min(var0);
        return var2.orElse(null);
    }

    public static Collection<BlockPos> findStructureBlocks(BlockPos param0, int param1, ServerLevel param2) {
        Collection<BlockPos> var0 = Lists.newArrayList();
        AABB var1 = new AABB(param0);
        var1 = var1.inflate((double)param1);

        for(int var2 = (int)var1.minX; var2 <= (int)var1.maxX; ++var2) {
            for(int var3 = (int)var1.minY; var3 <= (int)var1.maxY; ++var3) {
                for(int var4 = (int)var1.minZ; var4 <= (int)var1.maxZ; ++var4) {
                    BlockPos var5 = new BlockPos(var2, var3, var4);
                    BlockState var6 = param2.getBlockState(var5);
                    if (var6.is(Blocks.STRUCTURE_BLOCK)) {
                        var0.add(var5);
                    }
                }
            }
        }

        return var0;
    }

    private static StructureTemplate getStructureTemplate(String param0, ServerLevel param1) {
        StructureManager var0 = param1.getStructureManager();
        Optional<StructureTemplate> var1 = var0.get(new ResourceLocation(param0));
        if (var1.isPresent()) {
            return var1.get();
        } else {
            String var2 = param0 + ".snbt";
            Path var3 = Paths.get(testStructuresDir, var2);
            CompoundTag var4 = tryLoadStructure(var3);
            if (var4 == null) {
                throw new RuntimeException("Could not find structure file " + var3 + ", and the structure is not available in the world structures either.");
            } else {
                return var0.readStructure(var4);
            }
        }
    }

    private static StructureBlockEntity createStructureBlock(String param0, BlockPos param1, Rotation param2, ServerLevel param3, boolean param4) {
        param3.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var0 = (StructureBlockEntity)param3.getBlockEntity(param1);
        var0.setMode(StructureMode.LOAD);
        var0.setRotation(param2);
        var0.setIgnoreEntities(false);
        var0.setStructureName(new ResourceLocation(param0));
        var0.loadStructure(param3, param4);
        if (var0.getStructureSize() != Vec3i.ZERO) {
            return var0;
        } else {
            StructureTemplate var1 = getStructureTemplate(param0, param3);
            var0.loadStructure(param3, param4, var1);
            if (var0.getStructureSize() == Vec3i.ZERO) {
                throw new RuntimeException("Failed to load structure " + param0);
            } else {
                return var0;
            }
        }
    }

    @Nullable
    private static CompoundTag tryLoadStructure(Path param0) {
        try {
            BufferedReader var0 = Files.newBufferedReader(param0);
            String var1 = IOUtils.toString((Reader)var0);
            return NbtUtils.snbtToStructure(var1);
        } catch (IOException var31) {
            return null;
        } catch (CommandSyntaxException var4) {
            throw new RuntimeException("Error while trying to load structure " + param0, var4);
        }
    }

    private static void clearBlock(int param0, BlockPos param1, ServerLevel param2) {
        BlockState var0 = null;
        FlatLevelGeneratorSettings var1 = FlatLevelGeneratorSettings.getDefault(param2.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
        List<BlockState> var2 = var1.getLayers();
        int var3 = param1.getY() - param2.getMinBuildHeight();
        if (param1.getY() < param0 && var3 > 0 && var3 <= var2.size()) {
            var0 = var2.get(var3 - 1);
        }

        if (var0 == null) {
            var0 = Blocks.AIR.defaultBlockState();
        }

        BlockInput var4 = new BlockInput(var0, Collections.emptySet(), null);
        var4.place(param2, param1, 2);
        param2.blockUpdated(param1, var0.getBlock());
    }

    private static boolean doesStructureContain(BlockPos param0, BlockPos param1, ServerLevel param2) {
        StructureBlockEntity var0 = (StructureBlockEntity)param2.getBlockEntity(param0);
        AABB var1 = getStructureBounds(var0).inflate(1.0);
        return var1.contains(Vec3.atCenterOf(param1));
    }
}
