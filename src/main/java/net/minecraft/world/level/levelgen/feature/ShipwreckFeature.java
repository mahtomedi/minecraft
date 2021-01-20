package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
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
        public FeatureStart(StructureFeature<ShipwreckConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0, ChunkGenerator param1, StructureManager param2, int param3, int param4, Biome param5, ShipwreckConfiguration param6
        ) {
            Rotation var0 = Rotation.getRandom(this.random);
            BlockPos var1 = new BlockPos(SectionPos.sectionToBlockCoord(param3), 90, SectionPos.sectionToBlockCoord(param4));
            ShipwreckPieces.addPieces(param2, var1, var0, this.pieces, this.random, param6);
            this.calculateBoundingBox();
        }
    }
}
