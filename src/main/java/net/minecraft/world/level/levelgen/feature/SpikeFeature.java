package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
    private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(5L, TimeUnit.MINUTES)
        .build(new SpikeFeature.SpikeCacheLoader());

    public SpikeFeature(Function<Dynamic<?>, ? extends SpikeConfiguration> param0) {
        super(param0);
    }

    public static List<SpikeFeature.EndSpike> getSpikesForLevel(LevelAccessor param0) {
        Random var0 = new Random(param0.getSeed());
        long var1 = var0.nextLong() & 65535L;
        return SPIKE_CACHE.getUnchecked(var1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, SpikeConfiguration param4
    ) {
        List<SpikeFeature.EndSpike> var0 = param4.getSpikes();
        if (var0.isEmpty()) {
            var0 = getSpikesForLevel(param0);
        }

        for(SpikeFeature.EndSpike var1 : var0) {
            if (var1.isCenterWithinChunk(param3)) {
                this.placeSpike(param0, param2, param4, var1);
            }
        }

        return true;
    }

    private void placeSpike(LevelAccessor param0, Random param1, SpikeConfiguration param2, SpikeFeature.EndSpike param3) {
        int var0 = param3.getRadius();

        for(BlockPos var1 : BlockPos.betweenClosed(
            new BlockPos(param3.getCenterX() - var0, 0, param3.getCenterZ() - var0),
            new BlockPos(param3.getCenterX() + var0, param3.getHeight() + 10, param3.getCenterZ() + var0)
        )) {
            if (var1.closerThan(new BlockPos(param3.getCenterX(), var1.getY(), param3.getCenterZ()), (double)var0) && var1.getY() < param3.getHeight()) {
                this.setBlock(param0, var1, Blocks.OBSIDIAN.defaultBlockState());
            } else if (var1.getY() > 65) {
                this.setBlock(param0, var1, Blocks.AIR.defaultBlockState());
            }
        }

        if (param3.isGuarded()) {
            int var2 = -2;
            int var3 = 2;
            int var4 = 3;
            BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

            for(int var6 = -2; var6 <= 2; ++var6) {
                for(int var7 = -2; var7 <= 2; ++var7) {
                    for(int var8 = 0; var8 <= 3; ++var8) {
                        boolean var9 = Mth.abs(var6) == 2;
                        boolean var10 = Mth.abs(var7) == 2;
                        boolean var11 = var8 == 3;
                        if (var9 || var10 || var11) {
                            boolean var12 = var6 == -2 || var6 == 2 || var11;
                            boolean var13 = var7 == -2 || var7 == 2 || var11;
                            BlockState var14 = Blocks.IRON_BARS
                                .defaultBlockState()
                                .setValue(IronBarsBlock.NORTH, Boolean.valueOf(var12 && var7 != -2))
                                .setValue(IronBarsBlock.SOUTH, Boolean.valueOf(var12 && var7 != 2))
                                .setValue(IronBarsBlock.WEST, Boolean.valueOf(var13 && var6 != -2))
                                .setValue(IronBarsBlock.EAST, Boolean.valueOf(var13 && var6 != 2));
                            this.setBlock(param0, var5.set(param3.getCenterX() + var6, param3.getHeight() + var8, param3.getCenterZ() + var7), var14);
                        }
                    }
                }
            }
        }

        EndCrystal var15 = EntityType.END_CRYSTAL.create(param0.getLevel());
        var15.setBeamTarget(param2.getCrystalBeamTarget());
        var15.setInvulnerable(param2.isCrystalInvulnerable());
        var15.moveTo(
            (double)((float)param3.getCenterX() + 0.5F),
            (double)(param3.getHeight() + 1),
            (double)((float)param3.getCenterZ() + 0.5F),
            param1.nextFloat() * 360.0F,
            0.0F
        );
        param0.addFreshEntity(var15);
        this.setBlock(param0, new BlockPos(param3.getCenterX(), param3.getHeight(), param3.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
    }

    public static class EndSpike {
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final AABB topBoundingBox;

        public EndSpike(int param0, int param1, int param2, int param3, boolean param4) {
            this.centerX = param0;
            this.centerZ = param1;
            this.radius = param2;
            this.height = param3;
            this.guarded = param4;
            this.topBoundingBox = new AABB(
                (double)(param0 - param2), 0.0, (double)(param1 - param2), (double)(param0 + param2), 256.0, (double)(param1 + param2)
            );
        }

        public boolean isCenterWithinChunk(BlockPos param0) {
            return param0.getX() >> 4 == this.centerX >> 4 && param0.getZ() >> 4 == this.centerZ >> 4;
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public AABB getTopBoundingBox() {
            return this.topBoundingBox;
        }

        <T> Dynamic<T> serialize(DynamicOps<T> param0) {
            Builder<T, T> var0 = ImmutableMap.builder();
            var0.put(param0.createString("centerX"), param0.createInt(this.centerX));
            var0.put(param0.createString("centerZ"), param0.createInt(this.centerZ));
            var0.put(param0.createString("radius"), param0.createInt(this.radius));
            var0.put(param0.createString("height"), param0.createInt(this.height));
            var0.put(param0.createString("guarded"), param0.createBoolean(this.guarded));
            return new Dynamic<>(param0, param0.createMap(var0.build()));
        }

        public static <T> SpikeFeature.EndSpike deserialize(Dynamic<T> param0) {
            return new SpikeFeature.EndSpike(
                param0.get("centerX").asInt(0),
                param0.get("centerZ").asInt(0),
                param0.get("radius").asInt(0),
                param0.get("height").asInt(0),
                param0.get("guarded").asBoolean(false)
            );
        }
    }

    static class SpikeCacheLoader extends CacheLoader<Long, List<SpikeFeature.EndSpike>> {
        private SpikeCacheLoader() {
        }

        public List<SpikeFeature.EndSpike> load(Long param0) {
            List<Integer> var0 = IntStream.range(0, 10).boxed().collect(Collectors.toList());
            Collections.shuffle(var0, new Random(param0));
            List<SpikeFeature.EndSpike> var1 = Lists.newArrayList();

            for(int var2 = 0; var2 < 10; ++var2) {
                int var3 = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 10) * (double)var2)));
                int var4 = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 10) * (double)var2)));
                int var5 = var0.get(var2);
                int var6 = 2 + var5 / 3;
                int var7 = 76 + var5 * 3;
                boolean var8 = var5 == 1 || var5 == 2;
                var1.add(new SpikeFeature.EndSpike(var3, var4, var6, var7, var8));
            }

            return var1;
        }
    }
}
