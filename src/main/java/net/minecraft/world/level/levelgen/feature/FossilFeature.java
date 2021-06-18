package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.mutable.MutableInt;

public class FossilFeature extends Feature<FossilFeatureConfiguration> {
    public FossilFeature(Codec<FossilFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> param0) {
        Random var0 = param0.random();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        Rotation var3 = Rotation.getRandom(var0);
        FossilFeatureConfiguration var4 = param0.config();
        int var5 = var0.nextInt(var4.fossilStructures.size());
        StructureManager var6 = var1.getLevel().getServer().getStructureManager();
        StructureTemplate var7 = var6.getOrCreate(var4.fossilStructures.get(var5));
        StructureTemplate var8 = var6.getOrCreate(var4.overlayStructures.get(var5));
        ChunkPos var9 = new ChunkPos(var2);
        BoundingBox var10 = new BoundingBox(
            var9.getMinBlockX(), var1.getMinBuildHeight(), var9.getMinBlockZ(), var9.getMaxBlockX(), var1.getMaxBuildHeight(), var9.getMaxBlockZ()
        );
        StructurePlaceSettings var11 = new StructurePlaceSettings().setRotation(var3).setBoundingBox(var10).setRandom(var0);
        Vec3i var12 = var7.getSize(var3);
        int var13 = var0.nextInt(16 - var12.getX());
        int var14 = var0.nextInt(16 - var12.getZ());
        int var15 = var1.getMaxBuildHeight();

        for(int var16 = 0; var16 < var12.getX(); ++var16) {
            for(int var17 = 0; var17 < var12.getZ(); ++var17) {
                var15 = Math.min(var15, var1.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var2.getX() + var16 + var13, var2.getZ() + var17 + var14));
            }
        }

        int var18 = Math.max(var15 - 15 - var0.nextInt(10), var1.getMinBuildHeight() + 10);
        BlockPos var19 = var7.getZeroPositionWithTransform(var2.offset(var13, 0, var14).atY(var18), Mirror.NONE, var3);
        if (countEmptyCorners(var1, var7.getBoundingBox(var11, var19)) > var4.maxEmptyCornersAllowed) {
            return false;
        } else {
            var11.clearProcessors();
            var4.fossilProcessors.get().list().forEach(param1 -> var11.addProcessor(param1));
            var7.placeInWorld(var1, var19, var19, var11, var0, 4);
            var11.clearProcessors();
            var4.overlayProcessors.get().list().forEach(param1 -> var11.addProcessor(param1));
            var8.placeInWorld(var1, var19, var19, var11, var0, 4);
            return true;
        }
    }

    private static int countEmptyCorners(WorldGenLevel param0, BoundingBox param1) {
        MutableInt var0 = new MutableInt(0);
        param1.forAllCorners(param2 -> {
            BlockState var0x = param0.getBlockState(param2);
            if (var0x.isAir() || var0x.is(Blocks.LAVA) || var0x.is(Blocks.WATER)) {
                var0.add(1);
            }

        });
        return var0.getValue();
    }
}
