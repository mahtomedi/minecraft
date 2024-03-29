package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class StructureUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
    public static String testStructuresDir = "gameteststructures";

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

    public static AABB getStructureBounds(StructureBlockEntity param0) {
        return AABB.of(getStructureBoundingBox(param0));
    }

    public static BoundingBox getStructureBoundingBox(StructureBlockEntity param0) {
        BlockPos var0 = getStructureOrigin(param0);
        BlockPos var1 = getTransformedFarCorner(var0, param0.getStructureSize(), param0.getRotation());
        return BoundingBox.fromCorners(var0, var1);
    }

    public static BlockPos getStructureOrigin(StructureBlockEntity param0) {
        return param0.getBlockPos().offset(param0.getStructurePos());
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
        BoundingBox var0 = getStructureBoundingBox(param1.above(), param2, param3);
        clearSpaceForStructure(var0, param4);
        param4.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var1 = (StructureBlockEntity)param4.getBlockEntity(param1);
        var1.setIgnoreEntities(false);
        var1.setStructureName(new ResourceLocation(param0));
        var1.setStructureSize(param2);
        var1.setMode(StructureMode.SAVE);
        var1.setShowBoundingBox(true);
    }

    public static StructureBlockEntity prepareTestStructure(GameTestInfo param0, BlockPos param1, Rotation param2, ServerLevel param3) {
        Vec3i var0 = param3.getStructureManager()
            .get(new ResourceLocation(param0.getStructureName()))
            .orElseThrow(() -> new IllegalStateException("Missing test structure: " + param0.getStructureName()))
            .getSize();
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

        forceLoadChunks(var1, param3);
        clearSpaceForStructure(var1, param3);
        return createStructureBlock(param0, var2.below(), param2, param3);
    }

    private static void forceLoadChunks(BoundingBox param0, ServerLevel param1) {
        param0.intersectingChunks().forEach(param1x -> param1.setChunkForced(param1x.x, param1x.z, true));
    }

    public static void clearSpaceForStructure(BoundingBox param0, ServerLevel param1) {
        int var0 = param0.minY() - 1;
        BoundingBox var1 = new BoundingBox(param0.minX() - 2, param0.minY() - 3, param0.minZ() - 3, param0.maxX() + 3, param0.maxY() + 20, param0.maxZ() + 3);
        BlockPos.betweenClosedStream(var1).forEach(param2 -> clearBlock(var0, param2, param1));
        param1.getBlockTicks().clearArea(var1);
        param1.clearBlockEvents(var1);
        AABB var2 = new AABB((double)var1.minX(), (double)var1.minY(), (double)var1.minZ(), (double)var1.maxX(), (double)var1.maxY(), (double)var1.maxZ());
        List<Entity> var3 = param1.getEntitiesOfClass(Entity.class, var2, param0x -> !(param0x instanceof Player));
        var3.forEach(Entity::discard);
    }

    public static BlockPos getTransformedFarCorner(BlockPos param0, Vec3i param1, Rotation param2) {
        BlockPos var0 = param0.offset(param1).offset(-1, -1, -1);
        return StructureTemplate.transform(var0, Mirror.NONE, param2, param0);
    }

    public static BoundingBox getStructureBoundingBox(BlockPos param0, Vec3i param1, Rotation param2) {
        BlockPos var0 = getTransformedFarCorner(param0, param1, param2);
        BoundingBox var1 = BoundingBox.fromCorners(param0, var0);
        int var2 = Math.min(var1.minX(), var1.maxX());
        int var3 = Math.min(var1.minZ(), var1.maxZ());
        return var1.move(param0.getX() - var2, 0, param0.getZ() - var3);
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
        BoundingBox var1 = new BoundingBox(param0).inflatedBy(param1);
        BlockPos.betweenClosedStream(var1).forEach(param2x -> {
            if (param2.getBlockState(param2x).is(Blocks.STRUCTURE_BLOCK)) {
                var0.add(param2x.immutable());
            }

        });
        return var0;
    }

    private static StructureBlockEntity createStructureBlock(GameTestInfo param0, BlockPos param1, Rotation param2, ServerLevel param3) {
        param3.setBlockAndUpdate(param1, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity var0 = (StructureBlockEntity)param3.getBlockEntity(param1);
        var0.setMode(StructureMode.LOAD);
        var0.setRotation(param2);
        var0.setIgnoreEntities(false);
        var0.setStructureName(new ResourceLocation(param0.getStructureName()));
        var0.setMetaData(param0.getTestName());
        if (!var0.loadStructureInfo(param3)) {
            throw new RuntimeException("Failed to load structure info for test: " + param0.getTestName() + ". Structure name: " + param0.getStructureName());
        } else {
            return var0;
        }
    }

    private static void clearBlock(int param0, BlockPos param1, ServerLevel param2) {
        BlockState var0;
        if (param1.getY() < param0) {
            var0 = Blocks.STONE.defaultBlockState();
        } else {
            var0 = Blocks.AIR.defaultBlockState();
        }

        BlockInput var2 = new BlockInput(var0, Collections.emptySet(), null);
        var2.place(param2, param1, 2);
        param2.blockUpdated(param1, var0.getBlock());
    }

    private static boolean doesStructureContain(BlockPos param0, BlockPos param1, ServerLevel param2) {
        StructureBlockEntity var0 = (StructureBlockEntity)param2.getBlockEntity(param0);
        return getStructureBoundingBox(var0).isInside(param1);
    }
}
