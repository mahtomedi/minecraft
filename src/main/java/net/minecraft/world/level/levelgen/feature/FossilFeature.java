package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        Random var0 = param0.random();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        Rotation var3 = Rotation.getRandom(var0);
        int var4 = var0.nextInt(fossils.length);
        StructureManager var5 = var1.getLevel().getServer().getStructureManager();
        StructureTemplate var6 = var5.getOrCreate(fossils[var4]);
        StructureTemplate var7 = var5.getOrCreate(fossilsCoal[var4]);
        ChunkPos var8 = new ChunkPos(var2);
        BoundingBox var9 = new BoundingBox(
            var8.getMinBlockX(), var1.getMinBuildHeight(), var8.getMinBlockZ(), var8.getMaxBlockX(), var1.getMaxBuildHeight(), var8.getMaxBlockZ()
        );
        StructurePlaceSettings var10 = new StructurePlaceSettings()
            .setRotation(var3)
            .setBoundingBox(var9)
            .setRandom(var0)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        BlockPos var11 = var6.getSize(var3);
        int var12 = var0.nextInt(16 - var11.getX());
        int var13 = var0.nextInt(16 - var11.getZ());
        int var14 = var1.getMaxBuildHeight();

        for(int var15 = 0; var15 < var11.getX(); ++var15) {
            for(int var16 = 0; var16 < var11.getZ(); ++var16) {
                var14 = Math.min(var14, var1.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var2.getX() + var15 + var12, var2.getZ() + var16 + var13));
            }
        }

        int var17 = Math.max(var14 - 15 - var0.nextInt(10), var1.getMinBuildHeight() + 10);
        BlockPos var18 = var6.getZeroPositionWithTransform(var2.offset(var12, var17, var13), Mirror.NONE, var3);
        BlockRotProcessor var19 = new BlockRotProcessor(0.9F);
        var10.clearProcessors().addProcessor(var19);
        var6.placeInWorld(var1, var18, var18, var10, var0, 4);
        var10.popProcessor(var19);
        BlockRotProcessor var20 = new BlockRotProcessor(0.1F);
        var10.clearProcessors().addProcessor(var20);
        var7.placeInWorld(var1, var18, var18, var10, var0, 4);
        return true;
    }
}
