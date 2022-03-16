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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
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
        StructureTemplateManager var6 = var1.getLevel().getServer().getStructureManager();
        StructureTemplate var7 = var6.getOrCreate(var4.fossilStructures.get(var5));
        StructureTemplate var8 = var6.getOrCreate(var4.overlayStructures.get(var5));
        ChunkPos var9 = new ChunkPos(var2);
        BoundingBox var10 = new BoundingBox(
            var9.getMinBlockX() - 16,
            var1.getMinBuildHeight(),
            var9.getMinBlockZ() - 16,
            var9.getMaxBlockX() + 16,
            var1.getMaxBuildHeight(),
            var9.getMaxBlockZ() + 16
        );
        StructurePlaceSettings var11 = new StructurePlaceSettings().setRotation(var3).setBoundingBox(var10).setRandom(var0);
        Vec3i var12 = var7.getSize(var3);
        BlockPos var13 = var2.offset(-var12.getX() / 2, 0, -var12.getZ() / 2);
        int var14 = var2.getY();

        for(int var15 = 0; var15 < var12.getX(); ++var15) {
            for(int var16 = 0; var16 < var12.getZ(); ++var16) {
                var14 = Math.min(var14, var1.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var13.getX() + var15, var13.getZ() + var16));
            }
        }

        int var17 = Math.max(var14 - 15 - var0.nextInt(10), var1.getMinBuildHeight() + 10);
        BlockPos var18 = var7.getZeroPositionWithTransform(var13.atY(var17), Mirror.NONE, var3);
        if (countEmptyCorners(var1, var7.getBoundingBox(var11, var18)) > var4.maxEmptyCornersAllowed) {
            return false;
        } else {
            var11.clearProcessors();
            var4.fossilProcessors.value().list().forEach(var11::addProcessor);
            var7.placeInWorld(var1, var18, var18, var11, var0, 4);
            var11.clearProcessors();
            var4.overlayProcessors.value().list().forEach(var11::addProcessor);
            var8.placeInWorld(var1, var18, var18, var11, var0, 4);
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
