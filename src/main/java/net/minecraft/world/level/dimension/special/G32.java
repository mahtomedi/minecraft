package net.minecraft.world.level.dimension.special;

import java.util.BitSet;
import java.util.function.IntPredicate;
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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G32 extends SpecialDimensionBase {
    private static final BitSet TUNNNEL_N = new BitSet();
    private static final BitSet TUNNNEL_S = new BitSet();
    private static final BitSet TUNNNEL_W = new BitSet();
    private static final BitSet TUNNNEL_E = new BitSet();
    private static final BitSet TUNNNEL_U = new BitSet();
    private static final BitSet TUNNNEL_D = new BitSet();
    private static final BitSet CHAMBER = new BitSet();

    private static int sectionPosToInt(int param0, int param1, int param2) {
        return param0 << 8 | param1 << 4 | param2;
    }

    public G32(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G32.Generator(this.level, fixedBiome(Biomes.THE_END), NoneGeneratorSettings.INSTANCE);
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

    static {
        for(int var0 = 6; var0 < 10; ++var0) {
            for(int var1 = 6; var1 < 10; ++var1) {
                for(int var2 = 0; var2 <= 8; ++var2) {
                    TUNNNEL_N.set(sectionPosToInt(var0, var1, var2));
                    TUNNNEL_S.set(sectionPosToInt(var0, var1, 15 - var2));
                    TUNNNEL_W.set(sectionPosToInt(var2, var1, var0));
                    TUNNNEL_E.set(sectionPosToInt(15 - var2, var1, var0));
                    TUNNNEL_U.set(sectionPosToInt(var0, 15 - var2, var1));
                    TUNNNEL_D.set(sectionPosToInt(var0, var2, var1));
                }
            }
        }

        for(int var3 = 5; var3 < 11; ++var3) {
            for(int var4 = 5; var4 < 11; ++var4) {
                for(int var5 = 5; var5 < 11; ++var5) {
                    CHAMBER.set(sectionPosToInt(var3, var4, var5));
                }
            }
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
        public void applyBiomeDecoration(WorldGenRegion param0) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public int getSpawnHeight() {
            return 100;
        }

        private static IntPredicate appendSide(WorldgenRandom param0, int param1, int param2, int param3, IntPredicate param4, BitSet param5) {
            if (param2 < 0) {
                return param4;
            } else {
                param0.setBaseChunkSeed(param1, param3);
                param0.setBaseChunkSeed(param0.nextInt(), param2);
                return param0.nextBoolean() ? param4.or(param5::get) : param4;
            }
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
            ChunkPos var0 = param1.getPos();

            for(int var1 = 0; var1 < 16; ++var1) {
                WorldgenRandom var2 = new WorldgenRandom();
                int var3 = 2 * var0.x;
                int var4 = 2 * var1;
                int var5 = 2 * var0.z;
                IntPredicate var6 = param0x -> false;
                var6 = appendSide(var2, var3 + 1, var4, var5, var6, G32.TUNNNEL_E);
                var6 = appendSide(var2, var3 - 1, var4, var5, var6, G32.TUNNNEL_W);
                var6 = appendSide(var2, var3, var4, var5 + 1, var6, G32.TUNNNEL_S);
                var6 = appendSide(var2, var3, var4, var5 - 1, var6, G32.TUNNNEL_N);
                var6 = appendSide(var2, var3, var4 + 1, var5, var6, G32.TUNNNEL_U);
                var6 = appendSide(var2, var3, var4 - 1, var5, var6, G32.TUNNNEL_D);
                if (var6.test(G32.sectionPosToInt(8, 8, 8))) {
                    var6 = var6.or(G32.CHAMBER::get);
                }

                BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

                for(int var8 = 0; var8 < 16; ++var8) {
                    for(int var9 = 0; var9 < 16; ++var9) {
                        for(int var10 = 0; var10 < 16; ++var10) {
                            if (!var6.test(G32.sectionPosToInt(var8, var9, var10))) {
                                int var11 = 16 * var1 + var9;
                                param1.setBlockState(var7.set(var8, var11, var10), Blocks.SEA_LANTERN.defaultBlockState(), false);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 100;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T28;
        }
    }
}
