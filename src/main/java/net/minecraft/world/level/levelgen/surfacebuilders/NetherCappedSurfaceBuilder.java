package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public abstract class NetherCappedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private long seed;
    private ImmutableMap<BlockState, PerlinNoise> floorNoises = ImmutableMap.of();
    private ImmutableMap<BlockState, PerlinNoise> ceilingNoises = ImmutableMap.of();
    private PerlinNoise patchNoise;

    public NetherCappedSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    public void apply(
        Random param0,
        ChunkAccess param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        long param10,
        SurfaceBuilderBaseConfiguration param11
    ) {
        int var0 = param9 + 1;
        int var1 = param3 & 15;
        int var2 = param4 & 15;
        int var3 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        double var5 = 0.03125;
        boolean var6 = this.patchNoise.getValue((double)param3 * 0.03125, 109.0, (double)param4 * 0.03125) * 75.0 + param0.nextDouble() > 0.0;
        BlockState var7 = this.ceilingNoises
            .entrySet()
            .stream()
            .max(Comparator.comparing(param3x -> param3x.getValue().getValue((double)param3, (double)param9, (double)param4)))
            .get()
            .getKey();
        BlockState var8 = this.floorNoises
            .entrySet()
            .stream()
            .max(Comparator.comparing(param3x -> param3x.getValue().getValue((double)param3, (double)param9, (double)param4)))
            .get()
            .getKey();
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();
        BlockState var10 = param1.getBlockState(var9.set(var1, 128, var2));

        for(int var11 = 127; var11 >= 0; --var11) {
            var9.set(var1, var11, var2);
            BlockState var12 = param1.getBlockState(var9);
            if (var10.is(param7.getBlock()) && (var12.isAir() || var12 == param8)) {
                for(int var13 = 0; var13 < var3; ++var13) {
                    var9.move(Direction.UP);
                    if (!param1.getBlockState(var9).is(param7.getBlock())) {
                        break;
                    }

                    param1.setBlockState(var9, var7, false);
                }

                var9.set(var1, var11, var2);
            }

            if ((var10.isAir() || var10 == param8) && var12.is(param7.getBlock())) {
                for(int var14 = 0; var14 < var4 && param1.getBlockState(var9).is(param7.getBlock()); ++var14) {
                    if (var6 && var11 >= var0 - 4 && var11 <= var0 + 1) {
                        param1.setBlockState(var9, this.getPatchBlockState(), false);
                    } else {
                        param1.setBlockState(var9, var8, false);
                    }

                    var9.move(Direction.DOWN);
                }
            }

            var10 = var12;
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
