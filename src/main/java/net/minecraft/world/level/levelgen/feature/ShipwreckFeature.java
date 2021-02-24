package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
    public ShipwreckFeature(Codec<ShipwreckConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<ShipwreckConfiguration> getStartFactory() {
        return ShipwreckFeature.FeatureStart::new;
    }

    public static class FeatureStart extends StructureStart<ShipwreckConfiguration> {
        public FeatureStart(StructureFeature<ShipwreckConfiguration> param0, ChunkPos param1, BoundingBox param2, int param3, long param4) {
            super(param0, param1, param2, param3, param4);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            ShipwreckConfiguration param5,
            LevelHeightAccessor param6
        ) {
            Rotation var0 = Rotation.getRandom(this.random);
            BlockPos var1 = new BlockPos(param3.getMinBlockX(), 90, param3.getMinBlockZ());
            ShipwreckPieces.addPieces(param2, var1, var0, this.pieces, this.random, param5);
            this.calculateBoundingBox();
        }
    }
}
