package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class Climate {
    private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
    private static final float QUANTIZATION_FACTOR = 10000.0F;
    @VisibleForTesting
    protected static final int PARAMETER_COUNT = 7;

    public static Climate.TargetPoint target(float param0, float param1, float param2, float param3, float param4, float param5) {
        return new Climate.TargetPoint(
            quantizeCoord(param0), quantizeCoord(param1), quantizeCoord(param2), quantizeCoord(param3), quantizeCoord(param4), quantizeCoord(param5)
        );
    }

    public static Climate.ParameterPoint parameters(float param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        return new Climate.ParameterPoint(
            Climate.Parameter.point(param0),
            Climate.Parameter.point(param1),
            Climate.Parameter.point(param2),
            Climate.Parameter.point(param3),
            Climate.Parameter.point(param4),
            Climate.Parameter.point(param5),
            quantizeCoord(param6)
        );
    }

    public static Climate.ParameterPoint parameters(
        Climate.Parameter param0,
        Climate.Parameter param1,
        Climate.Parameter param2,
        Climate.Parameter param3,
        Climate.Parameter param4,
        Climate.Parameter param5,
        float param6
    ) {
        return new Climate.ParameterPoint(param0, param1, param2, param3, param4, param5, quantizeCoord(param6));
    }

    public static long quantizeCoord(float param0) {
        return (long)(param0 * 10000.0F);
    }

    public static float unquantizeCoord(long param0) {
        return (float)param0 / 10000.0F;
    }

    public static Climate.Sampler empty() {
        DensityFunction var0 = DensityFunctions.zero();
        return new Climate.Sampler(var0, var0, var0, var0, var0, var0, List.of());
    }

    public static BlockPos findSpawnPosition(List<Climate.ParameterPoint> param0, Climate.Sampler param1) {
        return (new Climate.SpawnFinder(param0, param1)).result.location();
    }

    interface DistanceMetric<T> {
        long distance(Climate.RTree.Node<T> var1, long[] var2);
    }

    public static record Parameter(long min, long max) {
        public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(
            Codec.floatRange(-2.0F, 2.0F),
            "min",
            "max",
            (param0, param1) -> param0.compareTo(param1) > 0
                    ? DataResult.error("Cannon construct interval, min > max (" + param0 + " > " + param1 + ")")
                    : DataResult.success(new Climate.Parameter(Climate.quantizeCoord(param0), Climate.quantizeCoord(param1))),
            param0 -> Climate.unquantizeCoord(param0.min()),
            param0 -> Climate.unquantizeCoord(param0.max())
        );

        public static Climate.Parameter point(float param0) {
            return span(param0, param0);
        }

        public static Climate.Parameter span(float param0, float param1) {
            if (param0 > param1) {
                throw new IllegalArgumentException("min > max: " + param0 + " " + param1);
            } else {
                return new Climate.Parameter(Climate.quantizeCoord(param0), Climate.quantizeCoord(param1));
            }
        }

        public static Climate.Parameter span(Climate.Parameter param0, Climate.Parameter param1) {
            if (param0.min() > param1.max()) {
                throw new IllegalArgumentException("min > max: " + param0 + " " + param1);
            } else {
                return new Climate.Parameter(param0.min(), param1.max());
            }
        }

        public String toString() {
            return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
        }

        public long distance(long param0) {
            long var0 = param0 - this.max;
            long var1 = this.min - param0;
            return var0 > 0L ? var0 : Math.max(var1, 0L);
        }

        public long distance(Climate.Parameter param0) {
            long var0 = param0.min() - this.max;
            long var1 = this.min - param0.max();
            return var0 > 0L ? var0 : Math.max(var1, 0L);
        }

        public Climate.Parameter span(@Nullable Climate.Parameter param0) {
            return param0 == null ? this : new Climate.Parameter(Math.min(this.min, param0.min()), Math.max(this.max, param0.max()));
        }
    }

    public static class ParameterList<T> {
        private final List<Pair<Climate.ParameterPoint, T>> values;
        private final Climate.RTree<T> index;

        public ParameterList(List<Pair<Climate.ParameterPoint, T>> param0) {
            this.values = param0;
            this.index = Climate.RTree.create(param0);
        }

        public List<Pair<Climate.ParameterPoint, T>> values() {
            return this.values;
        }

        public T findValue(Climate.TargetPoint param0) {
            return this.findValueIndex(param0);
        }

        @VisibleForTesting
        public T findValueBruteForce(Climate.TargetPoint param0) {
            Iterator<Pair<Climate.ParameterPoint, T>> var0 = this.values().iterator();
            Pair<Climate.ParameterPoint, T> var1 = var0.next();
            long var2 = ((Climate.ParameterPoint)var1.getFirst()).fitness(param0);
            T var3 = var1.getSecond();

            while(var0.hasNext()) {
                Pair<Climate.ParameterPoint, T> var4 = var0.next();
                long var5 = ((Climate.ParameterPoint)var4.getFirst()).fitness(param0);
                if (var5 < var2) {
                    var2 = var5;
                    var3 = var4.getSecond();
                }
            }

            return var3;
        }

        public T findValueIndex(Climate.TargetPoint param0) {
            return this.findValueIndex(param0, Climate.RTree.Node::distance);
        }

        protected T findValueIndex(Climate.TargetPoint param0, Climate.DistanceMetric<T> param1) {
            return this.index.search(param0, param1);
        }
    }

    public static record ParameterPoint(
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter depth,
        Climate.Parameter weirdness,
        long offset
    ) {
        public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Climate.Parameter.CODEC.fieldOf("temperature").forGetter(param0x -> param0x.temperature),
                        Climate.Parameter.CODEC.fieldOf("humidity").forGetter(param0x -> param0x.humidity),
                        Climate.Parameter.CODEC.fieldOf("continentalness").forGetter(param0x -> param0x.continentalness),
                        Climate.Parameter.CODEC.fieldOf("erosion").forGetter(param0x -> param0x.erosion),
                        Climate.Parameter.CODEC.fieldOf("depth").forGetter(param0x -> param0x.depth),
                        Climate.Parameter.CODEC.fieldOf("weirdness").forGetter(param0x -> param0x.weirdness),
                        Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("offset")
                            .xmap(Climate::quantizeCoord, Climate::unquantizeCoord)
                            .forGetter(param0x -> param0x.offset)
                    )
                    .apply(param0, Climate.ParameterPoint::new)
        );

        long fitness(Climate.TargetPoint param0) {
            return Mth.square(this.temperature.distance(param0.temperature))
                + Mth.square(this.humidity.distance(param0.humidity))
                + Mth.square(this.continentalness.distance(param0.continentalness))
                + Mth.square(this.erosion.distance(param0.erosion))
                + Mth.square(this.depth.distance(param0.depth))
                + Mth.square(this.weirdness.distance(param0.weirdness))
                + Mth.square(this.offset);
        }

        protected List<Climate.Parameter> parameterSpace() {
            return ImmutableList.of(
                this.temperature,
                this.humidity,
                this.continentalness,
                this.erosion,
                this.depth,
                this.weirdness,
                new Climate.Parameter(this.offset, this.offset)
            );
        }
    }

    protected static final class RTree<T> {
        private static final int CHILDREN_PER_NODE = 10;
        private final Climate.RTree.Node<T> root;
        private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

        private RTree(Climate.RTree.Node<T> param0) {
            this.root = param0;
        }

        public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, T>> param0) {
            if (param0.isEmpty()) {
                throw new IllegalArgumentException("Need at least one value to build the search tree.");
            } else {
                int var0 = ((Climate.ParameterPoint)param0.get(0).getFirst()).parameterSpace().size();
                if (var0 != 7) {
                    throw new IllegalStateException("Expecting parameter space to be 7, got " + var0);
                } else {
                    List<Climate.RTree.Leaf<T>> var1 = param0.stream()
                        .map(param0x -> new Climate.RTree.Leaf((Climate.ParameterPoint)param0x.getFirst(), param0x.getSecond()))
                        .collect(Collectors.toCollection(ArrayList::new));
                    return new Climate.RTree<>(build(var0, var1));
                }
            }
        }

        private static <T> Climate.RTree.Node<T> build(int param0, List<? extends Climate.RTree.Node<T>> param1) {
            if (param1.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            } else if (param1.size() == 1) {
                return param1.get(0);
            } else if (param1.size() <= 10) {
                param1.sort(Comparator.comparingLong(param1x -> {
                    long var0x = 0L;

                    for(int var1x = 0; var1x < param0; ++var1x) {
                        Climate.Parameter var2x = param1x.parameterSpace[var1x];
                        var0x += Math.abs((var2x.min() + var2x.max()) / 2L);
                    }

                    return var0x;
                }));
                return new Climate.RTree.SubTree<>(param1);
            } else {
                long var0 = Long.MAX_VALUE;
                int var1 = -1;
                List<Climate.RTree.SubTree<T>> var2 = null;

                for(int var3 = 0; var3 < param0; ++var3) {
                    sort(param1, param0, var3, false);
                    List<Climate.RTree.SubTree<T>> var4 = bucketize(param1);
                    long var5 = 0L;

                    for(Climate.RTree.SubTree<T> var6 : var4) {
                        var5 += cost(var6.parameterSpace);
                    }

                    if (var0 > var5) {
                        var0 = var5;
                        var1 = var3;
                        var2 = var4;
                    }
                }

                sort(var2, param0, var1, true);
                return new Climate.RTree.SubTree<>(var2.stream().map(param1x -> build(param0, Arrays.asList(param1x.children))).collect(Collectors.toList()));
            }
        }

        private static <T> void sort(List<? extends Climate.RTree.Node<T>> param0, int param1, int param2, boolean param3) {
            Comparator<Climate.RTree.Node<T>> var0 = comparator(param2, param3);

            for(int var1 = 1; var1 < param1; ++var1) {
                var0 = var0.thenComparing(comparator((param2 + var1) % param1, param3));
            }

            param0.sort(var0);
        }

        private static <T> Comparator<Climate.RTree.Node<T>> comparator(int param0, boolean param1) {
            return Comparator.comparingLong(param2 -> {
                Climate.Parameter var0x = param2.parameterSpace[param0];
                long var1x = (var0x.min() + var0x.max()) / 2L;
                return param1 ? Math.abs(var1x) : var1x;
            });
        }

        private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> param0) {
            List<Climate.RTree.SubTree<T>> var0 = Lists.newArrayList();
            List<Climate.RTree.Node<T>> var1 = Lists.newArrayList();
            int var2 = (int)Math.pow(10.0, Math.floor(Math.log((double)param0.size() - 0.01) / Math.log(10.0)));

            for(Climate.RTree.Node<T> var3 : param0) {
                var1.add(var3);
                if (var1.size() >= var2) {
                    var0.add(new Climate.RTree.SubTree<>(var1));
                    var1 = Lists.newArrayList();
                }
            }

            if (!var1.isEmpty()) {
                var0.add(new Climate.RTree.SubTree<>(var1));
            }

            return var0;
        }

        private static long cost(Climate.Parameter[] param0) {
            long var0 = 0L;

            for(Climate.Parameter var1 : param0) {
                var0 += Math.abs(var1.max() - var1.min());
            }

            return var0;
        }

        static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> param0) {
            if (param0.isEmpty()) {
                throw new IllegalArgumentException("SubTree needs at least one child");
            } else {
                int var0 = 7;
                List<Climate.Parameter> var1 = Lists.newArrayList();

                for(int var2 = 0; var2 < 7; ++var2) {
                    var1.add(null);
                }

                for(Climate.RTree.Node<T> var3 : param0) {
                    for(int var4 = 0; var4 < 7; ++var4) {
                        var1.set(var4, var3.parameterSpace[var4].span((Climate.Parameter)var1.get(var4)));
                    }
                }

                return var1;
            }
        }

        public T search(Climate.TargetPoint param0, Climate.DistanceMetric<T> param1) {
            long[] var0 = param0.toParameterArray();
            Climate.RTree.Leaf<T> var1 = this.root.search(var0, this.lastResult.get(), param1);
            this.lastResult.set(var1);
            return var1.value;
        }

        static final class Leaf<T> extends Climate.RTree.Node<T> {
            final T value;

            Leaf(Climate.ParameterPoint param0, T param1) {
                super(param0.parameterSpace());
                this.value = param1;
            }

            @Override
            protected Climate.RTree.Leaf<T> search(long[] param0, @Nullable Climate.RTree.Leaf<T> param1, Climate.DistanceMetric<T> param2) {
                return this;
            }
        }

        abstract static class Node<T> {
            protected final Climate.Parameter[] parameterSpace;

            protected Node(List<Climate.Parameter> param0) {
                this.parameterSpace = param0.toArray(new Climate.Parameter[0]);
            }

            protected abstract Climate.RTree.Leaf<T> search(long[] var1, @Nullable Climate.RTree.Leaf<T> var2, Climate.DistanceMetric<T> var3);

            protected long distance(long[] param0) {
                long var0 = 0L;

                for(int var1 = 0; var1 < 7; ++var1) {
                    var0 += Mth.square(this.parameterSpace[var1].distance(param0[var1]));
                }

                return var0;
            }

            @Override
            public String toString() {
                return Arrays.toString((Object[])this.parameterSpace);
            }
        }

        static final class SubTree<T> extends Climate.RTree.Node<T> {
            final Climate.RTree.Node<T>[] children;

            protected SubTree(List<? extends Climate.RTree.Node<T>> param0) {
                this(Climate.RTree.buildParameterSpace(param0), param0);
            }

            protected SubTree(List<Climate.Parameter> param0, List<? extends Climate.RTree.Node<T>> param1) {
                super(param0);
                this.children = param1.toArray(new Climate.RTree.Node[0]);
            }

            @Override
            protected Climate.RTree.Leaf<T> search(long[] param0, @Nullable Climate.RTree.Leaf<T> param1, Climate.DistanceMetric<T> param2) {
                long var0 = param1 == null ? Long.MAX_VALUE : param2.distance(param1, param0);
                Climate.RTree.Leaf<T> var1 = param1;

                for(Climate.RTree.Node<T> var2 : this.children) {
                    long var3 = param2.distance(var2, param0);
                    if (var0 > var3) {
                        Climate.RTree.Leaf<T> var4 = var2.search(param0, var1, param2);
                        long var5 = var2 == var4 ? var3 : param2.distance(var4, param0);
                        if (var0 > var5) {
                            var0 = var5;
                            var1 = var4;
                        }
                    }
                }

                return var1;
            }
        }
    }

    public static record Sampler(
        DensityFunction temperature,
        DensityFunction humidity,
        DensityFunction continentalness,
        DensityFunction erosion,
        DensityFunction depth,
        DensityFunction weirdness,
        List<Climate.ParameterPoint> spawnTarget
    ) {
        public Climate.TargetPoint sample(int param0, int param1, int param2) {
            int var0 = QuartPos.toBlock(param0);
            int var1 = QuartPos.toBlock(param1);
            int var2 = QuartPos.toBlock(param2);
            DensityFunction.SinglePointContext var3 = new DensityFunction.SinglePointContext(var0, var1, var2);
            return Climate.target(
                (float)this.temperature.compute(var3),
                (float)this.humidity.compute(var3),
                (float)this.continentalness.compute(var3),
                (float)this.erosion.compute(var3),
                (float)this.depth.compute(var3),
                (float)this.weirdness.compute(var3)
            );
        }

        public BlockPos findSpawnPosition() {
            return this.spawnTarget.isEmpty() ? BlockPos.ZERO : Climate.findSpawnPosition(this.spawnTarget, this);
        }
    }

    static class SpawnFinder {
        Climate.SpawnFinder.Result result;

        SpawnFinder(List<Climate.ParameterPoint> param0, Climate.Sampler param1) {
            this.result = getSpawnPositionAndFitness(param0, param1, 0, 0);
            this.radialSearch(param0, param1, 2048.0F, 512.0F);
            this.radialSearch(param0, param1, 512.0F, 32.0F);
        }

        private void radialSearch(List<Climate.ParameterPoint> param0, Climate.Sampler param1, float param2, float param3) {
            float var0 = 0.0F;
            float var1 = param3;
            BlockPos var2 = this.result.location();

            while(var1 <= param2) {
                int var3 = var2.getX() + (int)(Math.sin((double)var0) * (double)var1);
                int var4 = var2.getZ() + (int)(Math.cos((double)var0) * (double)var1);
                Climate.SpawnFinder.Result var5 = getSpawnPositionAndFitness(param0, param1, var3, var4);
                if (var5.fitness() < this.result.fitness()) {
                    this.result = var5;
                }

                var0 += param3 / var1;
                if ((double)var0 > Math.PI * 2) {
                    var0 = 0.0F;
                    var1 += param3;
                }
            }

        }

        private static Climate.SpawnFinder.Result getSpawnPositionAndFitness(
            List<Climate.ParameterPoint> param0, Climate.Sampler param1, int param2, int param3
        ) {
            double var0 = Mth.square(2500.0);
            int var1 = 2;
            long var2 = (long)((double)Mth.square(10000.0F) * Math.pow((double)(Mth.square((long)param2) + Mth.square((long)param3)) / var0, 2.0));
            Climate.TargetPoint var3 = param1.sample(QuartPos.fromBlock(param2), 0, QuartPos.fromBlock(param3));
            Climate.TargetPoint var4 = new Climate.TargetPoint(
                var3.temperature(), var3.humidity(), var3.continentalness(), var3.erosion(), 0L, var3.weirdness()
            );
            long var5 = Long.MAX_VALUE;

            for(Climate.ParameterPoint var6 : param0) {
                var5 = Math.min(var5, var6.fitness(var4));
            }

            return new Climate.SpawnFinder.Result(new BlockPos(param2, 0, param3), var2 + var5);
        }

        static record Result(BlockPos location, long fitness) {
        }
    }

    public static record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {
        @VisibleForTesting
        protected long[] toParameterArray() {
            return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
        }
    }
}
