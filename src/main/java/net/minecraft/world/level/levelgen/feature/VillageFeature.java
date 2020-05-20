package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature extends StructureFeature<JigsawConfiguration> {
    public VillageFeature(Codec<JigsawConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
        return VillageFeature.FeatureStart::new;
    }

    public static class FeatureStart extends BeardedStructureStart<JigsawConfiguration> {
        public FeatureStart(StructureFeature<JigsawConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, JigsawConfiguration param5) {
            BlockPos var0 = new BlockPos(param2 * 16, 0, param3 * 16);
            VillagePieces.addPieces(param0, param1, var0, this.pieces, this.random, param5);
            this.calculateBoundingBox();
        }
    }
}
