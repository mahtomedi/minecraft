package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class FixedBiomeSource extends BiomeSource {
    private final Biome biome;

    public FixedBiomeSource(FixedBiomeSourceSettings param0) {
        this.biome = param0.getBiome();
    }

    @Override
    public Biome getBiome(int param0, int param1) {
        return this.biome;
    }

    @Override
    public Biome[] getBiomeBlock(int param0, int param1, int param2, int param3, boolean param4) {
        Biome[] var0 = new Biome[param2 * param3];
        Arrays.fill(var0, 0, param2 * param3, this.biome);
        return var0;
    }

    @Nullable
    @Override
    public BlockPos findBiome(int param0, int param1, int param2, List<Biome> param3, Random param4) {
        return param3.contains(this.biome)
            ? new BlockPos(param0 - param2 + param4.nextInt(param2 * 2 + 1), 0, param1 - param2 + param4.nextInt(param2 * 2 + 1))
            : null;
    }

    @Override
    public boolean canGenerateStructure(StructureFeature<?> param0) {
        return this.supportedStructures.computeIfAbsent(param0, this.biome::isValidStart);
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            this.surfaceBlocks.add(this.biome.getSurfaceBuilderConfig().getTopMaterial());
        }

        return this.surfaceBlocks;
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2) {
        return Sets.newHashSet(this.biome);
    }
}
