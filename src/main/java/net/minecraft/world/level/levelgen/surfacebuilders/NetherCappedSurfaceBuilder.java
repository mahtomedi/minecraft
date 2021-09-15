package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public abstract class NetherCappedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private long seed;
    private ImmutableMap<BlockState, PerlinNoise> floorNoises = ImmutableMap.of();
    private ImmutableMap<BlockState, PerlinNoise> ceilingNoises = ImmutableMap.of();
    private PerlinNoise patchNoise;

    public NetherCappedSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    public void apply(
        Random param0,
        BlockColumn param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        int param10,
        long param11,
        SurfaceBuilderBaseConfiguration param12
    ) {
        int var0 = param9 + 1;
        int var1 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var2 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        double var3 = 0.03125;
        boolean var4 = this.patchNoise.getValue((double)param3 * 0.03125, 109.0, (double)param4 * 0.03125) * 75.0 + param0.nextDouble() > 0.0;
        BlockState var5 = this.ceilingNoises
            .entrySet()
            .stream()
            .max(Comparator.comparing(param3x -> param3x.getValue().getValue((double)param3, (double)param9, (double)param4)))
            .get()
            .getKey();
        BlockState var6 = this.floorNoises
            .entrySet()
            .stream()
            .max(Comparator.comparing(param3x -> param3x.getValue().getValue((double)param3, (double)param9, (double)param4)))
            .get()
            .getKey();
        BlockState var7 = param1.getBlock(128);

        for(int var8 = 127; var8 >= param10; --var8) {
            BlockState var9 = param1.getBlock(var8);
            if (var7.is(param7.getBlock()) && (var9.isAir() || var9 == param8)) {
                for(int var10 = 0; var10 < var1 && param1.getBlock(var8 + var10).is(param7.getBlock()); ++var10) {
                    param1.setBlock(var8 + var10, var5);
                }
            }

            if ((var7.isAir() || var7 == param8) && var9.is(param7.getBlock())) {
                for(int var11 = 0; var11 < var2 && param1.getBlock(var8 - var11).is(param7.getBlock()); ++var11) {
                    if (var4 && var8 >= var0 - 4 && var8 <= var0 + 1) {
                        param1.setBlock(var8 - var11, this.getPatchBlockState());
                    } else {
                        param1.setBlock(var8 - var11, var6);
                    }
                }
            }

            var7 = var9;
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.patchNoise == null || this.floorNoises.isEmpty() || this.ceilingNoises.isEmpty()) {
            this.floorNoises = initPerlinNoises(this.getFloorBlockStates(), param0);
            this.ceilingNoises = initPerlinNoises(this.getCeilingBlockStates(), param0 + (long)this.floorNoises.size());
            this.patchNoise = new PerlinNoise(new WorldgenRandom(param0 + (long)this.floorNoises.size() + (long)this.ceilingNoises.size()), ImmutableList.of(0));
        }

        this.seed = param0;
    }

    private static ImmutableMap<BlockState, PerlinNoise> initPerlinNoises(ImmutableList<BlockState> param0, long param1) {
        Builder<BlockState, PerlinNoise> var0 = new Builder<>();

        for(BlockState var1 : param0) {
            var0.put(var1, new PerlinNoise(new WorldgenRandom(param1), ImmutableList.of(-4)));
            ++param1;
        }

        return var0.build();
    }

    protected abstract ImmutableList<BlockState> getFloorBlockStates();

    protected abstract ImmutableList<BlockState> getCeilingBlockStates();

    protected abstract BlockState getPatchBlockState();
}
