package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> SWAMPHUT_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.WITCH, 1, 1, 1));
    private static final List<Biome.SpawnerData> SWAMPHUT_ANIMALS = Lists.newArrayList(new Biome.SpawnerData(EntityType.CAT, 1, 1, 1));

    public SwamplandHutFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Swamp_Hut";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return SwamplandHutFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 14357620;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return SWAMPHUT_ENEMIES;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialAnimals() {
        return SWAMPHUT_ANIMALS;
    }

    public boolean isSwamphut(StructureFeatureManager param0, BlockPos param1) {
        StructureStart var0 = this.getStructureAt(param0, param1, true);
        if (var0.isValid() && var0 instanceof SwamplandHutFeature.FeatureStart) {
            StructurePiece var1 = var0.getPieces().get(0);
            return var1 instanceof SwamplandHutPiece;
        } else {
            return false;
        }
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4) {
            SwamplandHutPiece var0 = new SwamplandHutPiece(this.random, param2 * 16, param3 * 16);
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
