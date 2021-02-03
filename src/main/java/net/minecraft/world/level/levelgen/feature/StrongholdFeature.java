package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return StrongholdFeature.StrongholdStart::new;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        NoneFeatureConfiguration param8,
        LevelHeightAccessor param9
    ) {
        return param0.hasStronghold(new ChunkPos(param4, param5));
    }

    public static class StrongholdStart extends StructureStart<NoneFeatureConfiguration> {
        private final long seed;

        public StrongholdStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
            this.seed = param5;
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            int param3,
            int param4,
            Biome param5,
            NoneFeatureConfiguration param6,
            LevelHeightAccessor param7
        ) {
            int var0 = 0;

            StrongholdPieces.StartPiece var1;
            do {
                this.pieces.clear();
                this.boundingBox = BoundingBox.getUnknownBox();
                this.random.setLargeFeatureSeed(this.seed + (long)(var0++), param3, param4);
                StrongholdPieces.resetPieces();
                var1 = new StrongholdPieces.StartPiece(this.random, SectionPos.sectionToBlockCoord(param3, 2), SectionPos.sectionToBlockCoord(param4, 2));
                this.pieces.add(var1);
                var1.addChildren(var1, this.pieces, this.random);
                List<StructurePiece> var2 = var1.pendingChildren;

                while(!var2.isEmpty()) {
                    int var3 = this.random.nextInt(var2.size());
                    StructurePiece var4 = var2.remove(var3);
                    var4.addChildren(var1, this.pieces, this.random);
                }

                this.calculateBoundingBox();
                this.moveBelowSeaLevel(param1.getSeaLevel(), this.random, 10);
            } while(this.pieces.isEmpty() || var1.portalRoomPiece == null);

        }
    }
}
