package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
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
        ChunkPos param4,
        Biome param5,
        ChunkPos param6,
        NoneFeatureConfiguration param7,
        LevelHeightAccessor param8
    ) {
        return param0.hasStronghold(param4);
    }

    public static class StrongholdStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
        private final long seed;

        public StrongholdStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, BoundingBox param2, int param3, long param4) {
            super(param0, param1, param2, param3, param4);
            this.seed = param4;
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            NoneFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            int var0 = 0;

            StrongholdPieces.StartPiece var1;
            do {
                this.pieces.clear();
                this.boundingBox = BoundingBox.getUnknownBox();
                this.random.setLargeFeatureSeed(this.seed + (long)(var0++), param3.x, param3.z);
                StrongholdPieces.resetPieces();
                var1 = new StrongholdPieces.StartPiece(this.random, param3.getBlockX(2), param3.getBlockZ(2));
                this.pieces.add(var1);
                var1.addChildren(var1, this.pieces, this.random);
                List<StructurePiece> var2 = var1.pendingChildren;

                while(!var2.isEmpty()) {
                    int var3 = this.random.nextInt(var2.size());
                    StructurePiece var4 = var2.remove(var3);
                    var4.addChildren(var1, this.pieces, this.random);
                }

                this.calculateBoundingBox();
                this.moveBelowSeaLevel(param1.getSeaLevel(), param1.getMinY(), this.random, 10);
            } while(this.pieces.isEmpty() || var1.portalRoomPiece == null);

        }
    }
}
