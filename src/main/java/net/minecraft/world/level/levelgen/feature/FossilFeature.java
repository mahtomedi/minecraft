package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FossilFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation SPINE_1 = new ResourceLocation("fossil/spine_1");
    private static final ResourceLocation SPINE_2 = new ResourceLocation("fossil/spine_2");
    private static final ResourceLocation SPINE_3 = new ResourceLocation("fossil/spine_3");
    private static final ResourceLocation SPINE_4 = new ResourceLocation("fossil/spine_4");
    private static final ResourceLocation SPINE_1_COAL = new ResourceLocation("fossil/spine_1_coal");
    private static final ResourceLocation SPINE_2_COAL = new ResourceLocation("fossil/spine_2_coal");
    private static final ResourceLocation SPINE_3_COAL = new ResourceLocation("fossil/spine_3_coal");
    private static final ResourceLocation SPINE_4_COAL = new ResourceLocation("fossil/spine_4_coal");
    private static final ResourceLocation SKULL_1 = new ResourceLocation("fossil/skull_1");
    private static final ResourceLocation SKULL_2 = new ResourceLocation("fossil/skull_2");
    private static final ResourceLocation SKULL_3 = new ResourceLocation("fossil/skull_3");
    private static final ResourceLocation SKULL_4 = new ResourceLocation("fossil/skull_4");
    private static final ResourceLocation SKULL_1_COAL = new ResourceLocation("fossil/skull_1_coal");
    private static final ResourceLocation SKULL_2_COAL = new ResourceLocation("fossil/skull_2_coal");
    private static final ResourceLocation SKULL_3_COAL = new ResourceLocation("fossil/skull_3_coal");
    private static final ResourceLocation SKULL_4_COAL = new ResourceLocation("fossil/skull_4_coal");
    private static final ResourceLocation[] fossils = new ResourceLocation[]{SPINE_1, SPINE_2, SPINE_3, SPINE_4, SKULL_1, SKULL_2, SKULL_3, SKULL_4};
    private static final ResourceLocation[] fossilsCoal = new ResourceLocation[]{
        SPINE_1_COAL, SPINE_2_COAL, SPINE_3_COAL, SPINE_4_COAL, SKULL_1_COAL, SKULL_2_COAL, SKULL_3_COAL, SKULL_4_COAL
    };

    public FossilFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        Rotation var0 = Rotation.getRandom(param2);
        int var1 = param2.nextInt(fossils.length);
        StructureManager var2 = ((ServerLevel)param0.getLevel()).getServer().getStructureManager();
        StructureTemplate var3 = var2.getOrCreate(fossils[var1]);
        StructureTemplate var4 = var2.getOrCreate(fossilsCoal[var1]);
        ChunkPos var5 = new ChunkPos(param3);
        BoundingBox var6 = new BoundingBox(var5.getMinBlockX(), 0, var5.getMinBlockZ(), var5.getMaxBlockX(), 256, var5.getMaxBlockZ());
        StructurePlaceSettings var7 = new StructurePlaceSettings()
            .setRotation(var0)
            .setBoundingBox(var6)
            .setRandom(param2)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        BlockPos var8 = var3.getSize(var0);
        int var9 = param2.nextInt(16 - var8.getX());
        int var10 = param2.nextInt(16 - var8.getZ());
        int var11 = 256;

        for(int var12 = 0; var12 < var8.getX(); ++var12) {
            for(int var13 = 0; var13 < var8.getZ(); ++var13) {
                var11 = Math.min(var11, param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, param3.getX() + var12 + var9, param3.getZ() + var13 + var10));
            }
        }

        int var14 = Math.max(var11 - 15 - param2.nextInt(10), 10);
        BlockPos var15 = var3.getZeroPositionWithTransform(param3.offset(var9, var14, var10), Mirror.NONE, var0);
        BlockRotProcessor var16 = new BlockRotProcessor(0.9F);
        var7.clearProcessors().addProcessor(var16);
        var3.placeInWorld(param0, var15, var15, var7, param2, 4);
        var7.popProcessor(var16);
        BlockRotProcessor var17 = new BlockRotProcessor(0.1F);
        var7.clearProcessors().addProcessor(var17);
        var4.placeInWorld(param0, var15, var15, var7, param2, 4);
        return true;
    }
}
