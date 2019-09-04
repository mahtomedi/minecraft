package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public abstract class RandomScatteredFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
    public RandomScatteredFeature(Function<Dynamic<?>, ? extends C> param0) {
        super(param0);
    }

    @Override
    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        int var0 = this.getSpacing(param0);
        int var1 = this.getSeparation(param0);
        int var2 = param2 + var0 * param4;
        int var3 = param3 + var0 * param5;
        int var4 = var2 < 0 ? var2 - var0 + 1 : var2;
        int var5 = var3 < 0 ? var3 - var0 + 1 : var3;
        int var6 = var4 / var0;
        int var7 = var5 / var0;
        ((WorldgenRandom)param1).setLargeFeatureWithSalt(param0.getSeed(), var6, var7, this.getRandomSalt());
        var6 *= var0;
        var7 *= var0;
        var6 += param1.nextInt(var0 - var1);
        var7 += param1.nextInt(var0 - var1);
        return new ChunkPos(var6, var7);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, param2, param3, param4, 0, 0);
        return param3 == var0.x && param4 == var0.z && param1.isBiomeValidStartForStructure(param5, this);
    }

    protected int getSpacing(ChunkGenerator<?> param0) {
        return param0.getSettings().getTemplesSpacing();
    }

    protected int getSeparation(ChunkGenerator<?> param0) {
        return param0.getSettings().getTemplesSeparation();
    }

    protected abstract int getRandomSalt();
}
