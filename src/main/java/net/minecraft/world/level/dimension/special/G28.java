package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G28 extends SpecialDimensionBase {
    public G28(Level param0, DimensionType param1) {
        super(param0, param1, 1.5F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G28.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
        public void applyBiomeDecoration(WorldGenRegion param0) {
            if (param0.getCenterX() == 31415 && param0.getCenterZ() == 92653) {
                ChunkAccess var0 = param0.getChunk(param0.getCenterX(), param0.getCenterZ());
                BlockPos var1 = new BlockPos(param0.getCenterX() * 16, 100, param0.getCenterZ() * 16);
                var0.setBlockState(new BlockPos(param0.getCenterX() * 16, 99, param0.getCenterZ() * 16), Blocks.GRASS_BLOCK.defaultBlockState(), false);
                var0.setBlockState(var1, Blocks.ACACIA_SIGN.defaultBlockState(), false);
                SignBlockEntity var2 = new SignBlockEntity();
                var2.setMessage(1, new TextComponent("Ha! I lied!"));
                var2.setMessage(2, new TextComponent("This isn't nothing!"));
                var0.setBlockEntity(var1, var2);
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
            return ChunkGeneratorType.T23;
        }
    }
}
