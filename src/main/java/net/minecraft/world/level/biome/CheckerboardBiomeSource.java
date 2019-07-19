package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class CheckerboardBiomeSource extends BiomeSource {
    private final Biome[] allowedBiomes;
    private final int bitShift;

    public CheckerboardBiomeSource(CheckerboardBiomeSourceSettings param0) {
        this.allowedBiomes = param0.getAllowedBiomes();
        this.bitShift = param0.getSize() + 4;
    }

    @Override
    public Biome getBiome(int param0, int param1) {
        return this.allowedBiomes[Math.abs(((param0 >> this.bitShift) + (param1 >> this.bitShift)) % this.allowedBiomes.length)];
    }

    @Override
    public Biome[] getBiomeBlock(int param0, int param1, int param2, int param3, boolean param4) {
        Biome[] var0 = new Biome[param2 * param3];

        for(int var1 = 0; var1 < param3; ++var1) {
            for(int var2 = 0; var2 < param2; ++var2) {
                int var3 = Math.abs(((param0 + var1 >> this.bitShift) + (param1 + var2 >> this.bitShift)) % this.allowedBiomes.length);
                Biome var4 = this.allowedBiomes[var3];
                var0[var1 * param2 + var2] = var4;
            }
        }

        return var0;
    }

    @Nullable
    @Override
    public BlockPos findBiome(int param0, int param1, int param2, List<Biome> param3, Random param4) {
        return null;
    }

    @Override
    public boolean canGenerateStructure(StructureFeature<?> param0) {
        return this.supportedStructures.computeIfAbsent(param0, param0x -> {
            for(Biome var0 : this.allowedBiomes) {
                if (var0.isValidStart(param0x)) {
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for(Biome var0 : this.allowedBiomes) {
                this.surfaceBlocks.add(var0.getSurfaceBuilderConfig().getTopMaterial());
            }
        }

        return this.surfaceBlocks;
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2) {
        return Sets.newHashSet(this.allowedBiomes);
    }
}
