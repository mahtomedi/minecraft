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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G13 extends SpecialDimensionBase {
    public G13(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G13.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 12000.0F;
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
        private StructureTemplate llama;

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void createStructures(BiomeManager param0, ChunkAccess param1, ChunkGenerator<?> param2, StructureManager param3) {
            if (this.llama == null) {
                this.llama = param3.getOrCreate(new ResourceLocation("llama"));
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
            BlockState var0 = Blocks.BEDROCK.defaultBlockState();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = 0; var2 < 16; ++var2) {
                for(int var3 = 0; var3 < 256; ++var3) {
                    for(int var4 = 0; var4 < 16; ++var4) {
                        param1.setBlockState(var1.set(var2, var3, var4), var0, false);
                    }
                }
            }

        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            BlockPos var2 = new BlockPos(16 * var0 + 8, 64, 16 * var1 + 8);
            this.llama.placeInWorld(param0, var2, var2, new StructurePlaceSettings(), 4);
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
            return ChunkGeneratorType.T09;
        }
    }
}
