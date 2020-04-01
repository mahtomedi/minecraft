package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
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

public class G21 extends SpecialDimensionBase {
    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    public G21(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G21.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return Vec3.ZERO;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return true;
    }

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        private StructureTemplate template;

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void createStructures(BiomeManager param0, ChunkAccess param1, ChunkGenerator<?> param2, StructureManager param3) {
            if (this.template == null) {
                this.template = param3.getOrCreate(new ResourceLocation("command.com"));
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
            ChunkPos var0 = param1.getPos();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = 0; var2 < 16; ++var2) {
                for(int var3 = 0; var3 < 16; ++var3) {
                    for(int var4 = 0; var4 < 16; ++var4) {
                        param1.setBlockState(var1.set(var2, var4, var3), Blocks.BLACK_CONCRETE.defaultBlockState(), false);
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
            if (var0 == 0 && var1 == 0) {
                BlockPos var2 = new BlockPos(0, 15, 0);
                this.template.placeInWorld(param0, var2, var2, new StructurePlaceSettings(), 4);
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
            return ChunkGeneratorType.T16;
        }
    }
}
