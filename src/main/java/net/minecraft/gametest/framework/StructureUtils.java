package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;

public class StructureUtils {
    public static String testStructuresDir = "gameteststructures";

    public static AABB getStructureBounds(StructureBlockEntity param0) {
        BlockPos var0 = param0.getBlockPos().offset(param0.getStructurePos());
        return new AABB(var0, var0.offset(param0.getStructureSize()));
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPos param0, ServerLevel param1) {
        param1.setBlockAndUpdate(param0, Blocks.COMMAND_BLOCK.defaultBlockState());
        CommandBlockEntity var0 = (CommandBlockEntity)param1.getBlockEntity(param0);
        var0.getCommandBlock().setCommand("test runthis");
        param1.setBlockAndUpdate(param0.offset(0, 0, -1), Blocks.STONE_BUTTON.defaultBlockState());
    }

    public static void createNewEmptyStructureBlock(String param0, BlockPos param1, BlockPos param2, int param3, ServerLevel param4) {
        BoundingBox var0 = createStructureBoundingBox(param1, param2, param3);
        clearSpaceForStructure(var0, param1.getY(), param4);
        param4.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var1 = (StructureBlockEntity)param4.getBlockEntity(param1);
        var1.setIgnoreEntities(false);
        var1.setStructureName(new ResourceLocation(param0));
        var1.setStructureSize(param2);
        var1.setMode(StructureMode.SAVE);
        var1.setShowBoundingBox(true);
    }

    public static StructureBlockEntity spawnStructure(String param0, BlockPos param1, int param2, ServerLevel param3, boolean param4) {
        BoundingBox var0 = createStructureBoundingBox(param1, getStructureTemplate(param0, param3).getSize(), param2);
        forceLoadChunks(param1, param3);
        clearSpaceForStructure(var0, param1.getY(), param3);
        StructureBlockEntity var1 = createStructureBlock(param0, param1, param3, param4);
        param3.getBlockTicks().fetchTicksInArea(var0, true, false);
        param3.clearBlockEvents(var0);
        return var1;
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
        BlockPos.betweenClosedStream(param0).forEach(param2x -> clearBlock(param1, param2x, param2));
        param2.getBlockTicks().fetchTicksInArea(param0, true, false);
        param2.clearBlockEvents(param0);
        AABB var0 = new AABB((double)param0.x0, (double)param0.y0, (double)param0.z0, (double)param0.x1, (double)param0.y1, (double)param0.z1);
        List<Entity> var1 = param2.getEntitiesOfClass(Entity.class, var0, param0x -> !(param0x instanceof Player));
        var1.forEach(Entity::remove);
    }

    public static BoundingBox createStructureBoundingBox(BlockPos param0, BlockPos param1, int param2) {
        BlockPos var0 = param0.offset(-param2, -3, -param2);
        BlockPos var1 = param0.offset(param1).offset(param2 - 1, 30, param2 - 1);
        return BoundingBox.createProper(var0.getX(), var0.getY(), var0.getZ(), var1.getX(), var1.getY(), var1.getZ());
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
                    if (var6.getBlock() == Blocks.STRUCTURE_BLOCK) {
                        var0.add(var5);
                    }
                }
            }
        }

        return var0;
    }

    private static StructureTemplate getStructureTemplate(String param0, ServerLevel param1) {
        StructureManager var0 = param1.getStructureManager();
        StructureTemplate var1 = var0.get(new ResourceLocation(param0));
        if (var1 != null) {
            return var1;
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

    private static StructureBlockEntity createStructureBlock(String param0, BlockPos param1, ServerLevel param2, boolean param3) {
        param2.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var0 = (StructureBlockEntity)param2.getBlockEntity(param1);
        var0.setMode(StructureMode.LOAD);
        var0.setIgnoreEntities(false);
        var0.setStructureName(new ResourceLocation(param0));
        var0.loadStructure(param3);
        if (var0.getStructureSize() != BlockPos.ZERO) {
            return var0;
        } else {
            StructureTemplate var1 = getStructureTemplate(param0, param2);
            var0.loadStructure(param3, var1);
            if (var0.getStructureSize() == BlockPos.ZERO) {
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
            return TagParser.parseTag(var1);
        } catch (IOException var31) {
            return null;
        } catch (CommandSyntaxException var4) {
            throw new RuntimeException("Error while trying to load structure " + param0, var4);
        }
    }

    private static void clearBlock(int param0, BlockPos param1, ServerLevel param2) {
        ChunkGeneratorSettings var0 = param2.getChunkSource().getGenerator().getSettings();
        BlockState var2;
        if (var0 instanceof FlatLevelGeneratorSettings) {
            BlockState[] var1 = ((FlatLevelGeneratorSettings)var0).getLayers();
            if (param1.getY() < param0) {
                var2 = var1[param1.getY() - 1];
            } else {
                var2 = Blocks.AIR.defaultBlockState();
            }
        } else if (param1.getY() == param0 - 1) {
            var2 = param2.getBiome(param1).getSurfaceBuilderConfig().getTopMaterial();
        } else if (param1.getY() < param0 - 1) {
            var2 = param2.getBiome(param1).getSurfaceBuilderConfig().getUnderMaterial();
        } else {
            var2 = Blocks.AIR.defaultBlockState();
        }

        BlockInput var7 = new BlockInput(var2, Collections.emptySet(), null);
        var7.place(param2, param1, 2);
        param2.blockUpdated(param1, var2.getBlock());
    }

    private static boolean doesStructureContain(BlockPos param0, BlockPos param1, ServerLevel param2) {
        StructureBlockEntity var0 = (StructureBlockEntity)param2.getBlockEntity(param0);
        AABB var1 = getStructureBounds(var0);
        return var1.contains(Vec3.atLowerCornerOf(param1));
    }
}
