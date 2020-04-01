package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G19 extends SpecialDimensionBase {
    public G19(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G19.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        private StructureTemplate center;
        private StructureTemplate side;
        private StructureTemplate legs;

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void createStructures(BiomeManager param0, ChunkAccess param1, ChunkGenerator<?> param2, StructureManager param3) {
            if (this.center == null) {
                this.center = param3.getOrCreate(new ResourceLocation("b_center"));
            }

            if (this.side == null) {
                this.side = param3.getOrCreate(new ResourceLocation("b_side"));
            }

            if (this.legs == null) {
                this.legs = param3.getOrCreate(new ResourceLocation("b_legs"));
            }

            super.createStructures(param0, param1, param2, param3);
        }

        @Override
        public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        }

        @Override
        public int getSpawnHeight() {
            return 30;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos(16 * var0 + 5, 0, 16 * var1 + 5);
            StructurePlaceSettings var3 = new StructurePlaceSettings();
            int var4 = 32;

            for(int var5 = 0; var5 < 32; ++var5) {
                this.legs.placeInWorld(param0, var2, var2, var3, 20);
                var2.move(0, 4, 0);
            }

            this.center.placeInWorld(param0, var2, var2, var3, 20);
            WorldgenRandom var6 = new WorldgenRandom();
            var6.setBaseChunkSeed(2 * var0, 2 * var1 - 1);
            if (var6.nextBoolean()) {
                var2.set(16 * var0 + 10, 128, 16 * var1 + 4);
                this.side.placeInWorld(param0, var2, var2, new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_180), 20);
            }

            var6.setBaseChunkSeed(2 * var0, 2 * var1 + 1);
            if (var6.nextBoolean()) {
                var2.set(16 * var0 + 5, 128, 16 * var1 + 11);
                this.side.placeInWorld(param0, var2, var2, new StructurePlaceSettings(), 20);
            }

            var6.setBaseChunkSeed(2 * var0 - 1, 2 * var1);
            if (var6.nextBoolean()) {
                var2.set(16 * var0 + 4, 128, 16 * var1 + 5);
                this.side.placeInWorld(param0, var2, var2, new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90), 20);
            }

            var6.setBaseChunkSeed(2 * var0 + 1, 2 * var1);
            if (var6.nextBoolean()) {
                var2.set(16 * var0 + 11, 128, 16 * var1 + 10);
                this.side.placeInWorld(param0, var2, var2, new StructurePlaceSettings().setRotation(Rotation.COUNTERCLOCKWISE_90), 20);
            }

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
            return ChunkGeneratorType.T14;
        }
    }
}
