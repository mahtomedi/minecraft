package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.function.Predicate;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@FunctionalInterface
public interface PieceGenerator<C extends FeatureConfiguration> {
    void generatePieces(StructurePiecesBuilder var1, C var2, PieceGenerator.Context var3);

    public static record Context(
        RegistryAccess registryAccess,
        ChunkGenerator chunkGenerator,
        StructureManager structureManager,
        ChunkPos chunkPos,
        Predicate<Biome> validBiome,
        LevelHeightAccessor heightAccessor,
        WorldgenRandom random,
        long seed
    ) {
        public boolean validBiomeOnTop(Heightmap.Types param0) {
            int var0 = this.chunkPos.getMiddleBlockX();
            int var1 = this.chunkPos.getMiddleBlockZ();
            int var2 = this.chunkGenerator.getFirstOccupiedHeight(var0, var1, param0, this.heightAccessor);
            Biome var3 = this.chunkGenerator.getNoiseBiome(QuartPos.fromBlock(var0), QuartPos.fromBlock(var2), QuartPos.fromBlock(var1));
            return this.validBiome.test(var3);
        }

        public int getLowestY(int param0, int param1) {
            int var0 = this.chunkPos.getMinBlockX();
            int var1 = this.chunkPos.getMinBlockZ();
            int[] var2 = this.getCornerHeights(var0, param0, var1, param1);
            return Math.min(Math.min(var2[0], var2[1]), Math.min(var2[2], var2[3]));
        }

        public int[] getCornerHeights(int param0, int param1, int param2, int param3) {
            return new int[]{
                this.chunkGenerator.getFirstOccupiedHeight(param0, param2, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
                this.chunkGenerator.getFirstOccupiedHeight(param0, param2 + param3, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
                this.chunkGenerator.getFirstOccupiedHeight(param0 + param1, param2, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
                this.chunkGenerator.getFirstOccupiedHeight(param0 + param1, param2 + param3, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor)
            };
        }
    }
}
