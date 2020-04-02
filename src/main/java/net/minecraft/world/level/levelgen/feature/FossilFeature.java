package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
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

    public FossilFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        NoneFeatureConfiguration param5
    ) {
        Random var0 = param0.getRandom();
        Rotation[] var1 = Rotation.values();
        Rotation var2 = var1[var0.nextInt(var1.length)];
        int var3 = var0.nextInt(fossils.length);
        StructureManager var4 = ((ServerLevel)param0.getLevel()).getLevelStorage().getStructureManager();
        StructureTemplate var5 = var4.getOrCreate(fossils[var3]);
        StructureTemplate var6 = var4.getOrCreate(fossilsCoal[var3]);
        ChunkPos var7 = new ChunkPos(param4);
        BoundingBox var8 = new BoundingBox(var7.getMinBlockX(), 0, var7.getMinBlockZ(), var7.getMaxBlockX(), 256, var7.getMaxBlockZ());
        StructurePlaceSettings var9 = new StructurePlaceSettings()
            .setRotation(var2)
            .setBoundingBox(var8)
            .setRandom(var0)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        BlockPos var10 = var5.getSize(var2);
        int var11 = var0.nextInt(16 - var10.getX());
        int var12 = var0.nextInt(16 - var10.getZ());
        int var13 = 256;

        for(int var14 = 0; var14 < var10.getX(); ++var14) {
            for(int var15 = 0; var15 < var10.getZ(); ++var15) {
                var13 = Math.min(var13, param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, param4.getX() + var14 + var11, param4.getZ() + var15 + var12));
            }
        }

        int var16 = Math.max(var13 - 15 - var0.nextInt(10), 10);
        BlockPos var17 = var5.getZeroPositionWithTransform(param4.offset(var11, var16, var12), Mirror.NONE, var2);
        BlockRotProcessor var18 = new BlockRotProcessor(0.9F);
        var9.clearProcessors().addProcessor(var18);
        var5.placeInWorld(param0, var17, var17, var9, 4);
        var9.popProcessor(var18);
        BlockRotProcessor var19 = new BlockRotProcessor(0.1F);
        var9.clearProcessors().addProcessor(var19);
        var6.placeInWorld(param0, var17, var17, var9, 4);
        return true;
    }
}
