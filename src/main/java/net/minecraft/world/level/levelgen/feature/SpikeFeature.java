package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
    public static final int NUMBER_OF_SPIKES = 10;
    private static final int SPIKE_DISTANCE = 42;
    private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(5L, TimeUnit.MINUTES)
        .build(new SpikeFeature.SpikeCacheLoader());

    public SpikeFeature(Codec<SpikeConfiguration> param0) {
        super(param0);
    }

    public static List<SpikeFeature.EndSpike> getSpikesForLevel(WorldGenLevel param0) {
        Random var0 = new Random(param0.getSeed());
        long var1 = var0.nextLong() & 65535L;
        return SPIKE_CACHE.getUnchecked(var1);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpikeConfiguration> param0) {
        SpikeConfiguration var0 = param0.config();
        WorldGenLevel var1 = param0.level();
        Random var2 = param0.random();
        BlockPos var3 = param0.origin();
        List<SpikeFeature.EndSpike> var4 = var0.getSpikes();
        if (var4.isEmpty()) {
            var4 = getSpikesForLevel(var1);
        }

        for(SpikeFeature.EndSpike var5 : var4) {
            if (var5.isCenterWithinChunk(var3)) {
                this.placeSpike(var1, var2, var0, var5);
            }
        }

        return true;
    }

    private void placeSpike(ServerLevelAccessor param0, Random param1, SpikeConfiguration param2, SpikeFeature.EndSpike param3) {
        int var0 = param3.getRadius();

        for(BlockPos var1 : BlockPos.betweenClosed(
            new BlockPos(param3.getCenterX() - var0, param0.getMinBuildHeight(), param3.getCenterZ() - var0),
            new BlockPos(param3.getCenterX() + var0, param3.getHeight() + 10, param3.getCenterZ() + var0)
        )) {
            if (var1.distSqr((double)param3.getCenterX(), (double)var1.getY(), (double)param3.getCenterZ(), false) <= (double)(var0 * var0 + 1)
                && var1.getY() < param3.getHeight()) {
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
        var15.moveTo((double)param3.getCenterX() + 0.5, (double)(param3.getHeight() + 1), (double)param3.getCenterZ() + 0.5, param1.nextFloat() * 360.0F, 0.0F);
        param0.addFreshEntity(var15);
        this.setBlock(param0, new BlockPos(param3.getCenterX(), param3.getHeight(), param3.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
    }

    public static class EndSpike {
        public static final Codec<SpikeFeature.EndSpike> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("centerX").orElse(0).forGetter(param0x -> param0x.centerX),
                        Codec.INT.fieldOf("centerZ").orElse(0).forGetter(param0x -> param0x.centerZ),
                        Codec.INT.fieldOf("radius").orElse(0).forGetter(param0x -> param0x.radius),
                        Codec.INT.fieldOf("height").orElse(0).forGetter(param0x -> param0x.height),
                        Codec.BOOL.fieldOf("guarded").orElse(false).forGetter(param0x -> param0x.guarded)
                    )
                    .apply(param0, SpikeFeature.EndSpike::new)
        );
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
                (double)(param0 - param2),
                (double)DimensionType.MIN_Y,
                (double)(param1 - param2),
                (double)(param0 + param2),
                (double)DimensionType.MAX_Y,
                (double)(param1 + param2)
            );
        }

        public boolean isCenterWithinChunk(BlockPos param0) {
            return SectionPos.blockToSectionCoord(param0.getX()) == SectionPos.blockToSectionCoord(this.centerX)
                && SectionPos.blockToSectionCoord(param0.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
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
