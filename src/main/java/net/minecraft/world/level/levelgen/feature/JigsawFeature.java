package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
    final int startY;
    final boolean doExpansionHack;
    final boolean projectStartToHeightmap;

    public JigsawFeature(Codec<JigsawConfiguration> param0, int param1, boolean param2, boolean param3) {
        super(param0);
        this.startY = param1;
        this.doExpansionHack = param2;
        this.projectStartToHeightmap = param3;
    }

    @Override
    public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
        return (param0, param1, param2, param3) -> new JigsawFeature.FeatureStart(this, param1, param2, param3);
    }

    public static class FeatureStart extends NoiseAffectingStructureStart<JigsawConfiguration> {
        private final JigsawFeature feature;

        public FeatureStart(JigsawFeature param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
            this.feature = param0;
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            JigsawConfiguration param5,
            LevelHeightAccessor param6
        ) {
            BlockPos var0 = new BlockPos(param3.getMinBlockX(), this.feature.startY, param3.getMinBlockZ());
            Pools.bootstrap();
            JigsawPlacement.addPieces(
                param0,
                param5,
                PoolElementStructurePiece::new,
                param1,
                param2,
                var0,
                this,
                this.random,
                this.feature.doExpansionHack,
                this.feature.projectStartToHeightmap,
                param6
            );
        }
    }
}
