package net.minecraft.world.level.dimension.special;

import com.mojang.math.OctahedralGroup;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.world.level.levelgen.feature.CharFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G07 extends SpecialDimensionBase {
    private static final String[] credits = readCredits();

    public G07(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    private static String[] readCredits() {
        try (
            InputStream var0 = G07.class.getResourceAsStream("/credits.txt");
            Reader var1 = new InputStreamReader(var0, StandardCharsets.UTF_8);
            BufferedReader var2 = new BufferedReader(var1);
        ) {
            return var2.lines().toArray(param0 -> new String[param0]);
        } catch (IOException var59) {
            return new String[0];
        }
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G07.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
        final SimpleStateProvider provider = new SimpleStateProvider(Blocks.SPONGE.defaultBlockState());

        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
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
            int var1 = var0.x * 2;
            int var2 = var0.z * 2;
            this.tryPrintChar(param1, var2, var1, 0, 0);
            this.tryPrintChar(param1, var2, var1, 1, 0);
            this.tryPrintChar(param1, var2, var1, 0, 1);
            this.tryPrintChar(param1, var2, var1, 1, 1);
        }

        private void tryPrintChar(ChunkAccess param0, int param1, int param2, int param3, int param4) {
            int var0 = param2 + param3;
            int var1 = param1 + param4;
            if (var1 >= 0 && var1 < G07.credits.length) {
                String var2 = G07.credits[var1];
                if (var0 >= 0 && var0 < var2.length()) {
                    char var3 = var2.charAt(var0);
                    CharFeature.place(
                        new BlockPos(8 * param3, 20, 8 * param4),
                        new CharConfiguration(this.provider, var3, OctahedralGroup.ROT_90_X_NEG),
                        param1x -> param0.setBlockState(param1x, Blocks.NETHERITE_BLOCK.defaultBlockState(), false)
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
            return ChunkGeneratorType.T07;
        }
    }
}
