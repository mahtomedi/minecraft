package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G26 extends SpecialDimensionBase {
    public G26(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G26.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
        private final PerlinSimplexNoise toneNoise;
        private final PerlinSimplexNoise instrumentNoise;

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
            WorldgenRandom var0 = new WorldgenRandom(param0.getSeed());
            this.toneNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-4, 1));
            this.instrumentNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-5, 2));
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
            NoteBlockInstrument[] var1 = NoteBlockInstrument.values();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(int var3 = 0; var3 < 16; ++var3) {
                for(int var4 = 0; var4 < 16; ++var4) {
                    int var5 = (int)((this.toneNoise.getValue((double)(var0.x * 16 + var3), (double)(var0.z * 16 + var4), false) + 1.0) / 2.0 * 24.0);
                    int var6 = (int)(
                        (this.instrumentNoise.getValue((double)((float)(var0.x * 16 + var3) / 4.0F), (double)((float)(var0.z * 16 + var4) / 4.0F), false) + 1.0)
                            / 2.0
                            * (double)var1.length
                    );
                    param1.setBlockState(
                        var2.set(var3, 0, var4),
                        Blocks.NOTE_BLOCK.defaultBlockState().setValue(NoteBlock.INSTRUMENT, var1[var6]).setValue(NoteBlock.NOTE, Integer.valueOf(var5)),
                        false
                    );
                }
            }

        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
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
            return ChunkGeneratorType.T21;
        }
    }
}
