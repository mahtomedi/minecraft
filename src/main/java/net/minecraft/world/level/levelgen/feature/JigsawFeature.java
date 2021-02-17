package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
    private final int startY;
    private final boolean doExpansionHack;
    private final boolean projectStartToHeightmap;

    public JigsawFeature(Codec<JigsawConfiguration> param0, int param1, boolean param2, boolean param3) {
        super(param0);
        this.startY = param1;
        this.doExpansionHack = param2;
        this.projectStartToHeightmap = param3;
    }

    @Override
    public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
        return (param0, param1, param2, param3, param4, param5) -> new JigsawFeature.FeatureStart(this, param1, param2, param3, param4, param5);
    }

    public static class FeatureStart extends NoiseAffectingStructureStart<JigsawConfiguration> {
        private final JigsawFeature feature;

        public FeatureStart(JigsawFeature param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
            this.feature = param0;
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            int param3,
            int param4,
            Biome param5,
            JigsawConfiguration param6,
            LevelHeightAccessor param7
        ) {
            BlockPos var0 = new BlockPos(SectionPos.sectionToBlockCoord(param3), this.feature.startY, SectionPos.sectionToBlockCoord(param4));
            Pools.bootstrap();
            JigsawPlacement.addPieces(
                param0,
                param6,
                PoolElementStructurePiece::new,
                param1,
                param2,
                var0,
                this.pieces,
                this.random,
                this.feature.doExpansionHack,
                this.feature.projectStartToHeightmap,
                param7
            );
            this.calculateBoundingBox();
        }
    }
}
