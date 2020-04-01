package net.minecraft.world.level.dimension.special;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G03 extends SpecialDimensionBase {
    public G03(Level param0, DimensionType param1) {
        super(param0, param1, 1.5F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G03.Generator(this.level, fixedBiome(Biomes.BETWEEN), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
    }

    public static class BetweenBiome extends Biome {
        public BetweenBiome() {
            super(
                new Biome.BiomeBuilder()
                    .surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE)
                    .precipitation(Biome.Precipitation.NONE)
                    .biomeCategory(Biome.BiomeCategory.NONE)
                    .depth(0.1F)
                    .scale(0.2F)
                    .temperature(0.5F)
                    .downfall(0.5F)
                    .specialEffects(
                        new BiomeSpecialEffects.Builder()
                            .waterColor(4159204)
                            .waterFogColor(329011)
                            .fogColor(12638463)
                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                            .build()
                    )
                    .parent(null)
            );
            this.addStructureStart(Feature.SHIP.configured(FeatureConfiguration.NONE));
            this.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Feature.SHIP.configured(FeatureConfiguration.NONE));
        }
    }

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public int getSpawnHeight() {
            return 0;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 0;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T03;
        }
    }

    public static class ShipFeature extends StructureFeature<NoneFeatureConfiguration> {
        public ShipFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
            super(param0, param1);
        }

        @Override
        public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
            return (param3 & 1) == 0 && (param4 & 1) == 0;
        }

        @Override
        public StructureFeature.StructureStartFactory getStartFactory() {
            return G03.ShipStart::new;
        }

        @Override
        public String getFeatureName() {
            return "Ship";
        }

        @Override
        public int getLookupRange() {
            return 0;
        }
    }

    public static class ShipPiece extends TemplateStructurePiece {
        private static final ResourceLocation END_SHIP = new ResourceLocation("end_city/ship");

        public ShipPiece(StructureManager param0, BlockPos param1) {
            super(StructurePieceType.FLEET_PIECE, 0);
            this.templatePosition = param1;
            this.configure(param0);
        }

        public ShipPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.FLEET_PIECE, param1);
            this.configure(param0);
        }

        private void configure(StructureManager param0) {
            StructureTemplate var0 = param0.getOrCreate(END_SHIP);
            StructurePlaceSettings var1 = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).setIgnoreEntities(true);
            this.setup(var0, this.templatePosition, var1);
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, LevelAccessor param2, Random param3, BoundingBox param4) {
            if (param0.startsWith("Chest")) {
                BlockPos var0 = param1.below();
                if (param4.isInside(var0)) {
                    RandomizableContainerBlockEntity.setLootTable(param2, param3, var0, BuiltInLootTables.FLEET_ORDERS);
                }
            }

        }
    }

    static class ShipStart extends StructureStart {
        public ShipStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            this.pieces.add(new G03.ShipPiece(param1, new BlockPos(param2 * 16 + 9, 50, param3 * 16 + 9)));
            this.pieces.add(new G03.ShipPiece(param1, new BlockPos(param2 * 16 + 9, 100, param3 * 16 + 9)));
            this.pieces.add(new G03.ShipPiece(param1, new BlockPos(param2 * 16 + 9, 150, param3 * 16 + 9)));
            this.calculateBoundingBox();
        }
    }
}
