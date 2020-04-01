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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G17 extends SpecialDimensionBase {
    public G17(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G17.Generator(this.level, normalBiomes(this.level.getSeed()), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return NormalDimension.getTimeOfDayI(param0, 24000.0);
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
        private StructureTemplate box;

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void createStructures(BiomeManager param0, ChunkAccess param1, ChunkGenerator<?> param2, StructureManager param3) {
            if (this.box == null) {
                this.box = param3.getOrCreate(new ResourceLocation("9x9"));
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
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

            for(int var1 = 0; var1 < 16; ++var1) {
                for(int var2 = 0; var2 < 16; ++var2) {
                    for(int var3 = 0; var3 < 64; ++var3) {
                        param1.setBlockState(var0.set(var1, var3, var2), Blocks.COBBLESTONE.defaultBlockState(), false);
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

            for(int var2 = 0; var2 < 16; ++var2) {
                int var3 = 16 * var0 + var2;
                if (Math.floorMod(var3, 10) == 0) {
                    for(int var4 = 0; var4 < 16; ++var4) {
                        int var5 = 16 * var1 + var4;
                        if (Math.floorMod(var5, 10) == 0) {
                            BlockPos var6 = new BlockPos(var3, 64, var5);
                            this.box.placeInWorld(param0, var6, var6, new StructurePlaceSettings(), 4);
                        }
                    }
                }
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
            return ChunkGeneratorType.T12;
        }
    }
}
