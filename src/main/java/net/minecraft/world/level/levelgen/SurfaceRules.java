package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
    public static final SurfaceRules.ConditionSource ON_FLOOR = stoneDepthCheck(0, false, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource VERY_DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource ON_CEILING = stoneDepthCheck(0, false, CaveSurface.CEILING);
    public static final SurfaceRules.ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, CaveSurface.CEILING);

    public static SurfaceRules.ConditionSource stoneDepthCheck(int param0, boolean param1, CaveSurface param2) {
        return new SurfaceRules.StoneDepthCheck(param0, param1, 0, param2);
    }

    public static SurfaceRules.ConditionSource stoneDepthCheck(int param0, boolean param1, int param2, CaveSurface param3) {
        return new SurfaceRules.StoneDepthCheck(param0, param1, param2, param3);
    }

    public static SurfaceRules.ConditionSource not(SurfaceRules.ConditionSource param0) {
        return new SurfaceRules.NotConditionSource(param0);
    }

    public static SurfaceRules.ConditionSource yBlockCheck(VerticalAnchor param0, int param1) {
        return new SurfaceRules.YConditionSource(param0, param1, false);
    }

    public static SurfaceRules.ConditionSource yStartCheck(VerticalAnchor param0, int param1) {
        return new SurfaceRules.YConditionSource(param0, param1, true);
    }

    public static SurfaceRules.ConditionSource waterBlockCheck(int param0, int param1) {
        return new SurfaceRules.WaterConditionSource(param0, param1, false);
    }

    public static SurfaceRules.ConditionSource waterStartCheck(int param0, int param1) {
        return new SurfaceRules.WaterConditionSource(param0, param1, true);
    }

    @SafeVarargs
    public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome>... param0) {
        return isBiome(List.of(param0));
    }

    private static SurfaceRules.BiomeConditionSource isBiome(List<ResourceKey<Biome>> param0) {
        return new SurfaceRules.BiomeConditionSource(param0);
    }

    public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> param0, double param1) {
        return noiseCondition(param0, param1, Double.MAX_VALUE);
    }

    public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> param0, double param1, double param2) {
        return new SurfaceRules.NoiseThresholdConditionSource(param0, param1, param2);
    }

    public static SurfaceRules.ConditionSource verticalGradient(String param0, VerticalAnchor param1, VerticalAnchor param2) {
        return new SurfaceRules.VerticalGradientConditionSource(new ResourceLocation(param0), param1, param2);
    }

    public static SurfaceRules.ConditionSource steep() {
        return SurfaceRules.Steep.INSTANCE;
    }

    public static SurfaceRules.ConditionSource hole() {
        return SurfaceRules.Hole.INSTANCE;
    }

    public static SurfaceRules.ConditionSource abovePreliminarySurface() {
        return SurfaceRules.AbovePreliminarySurface.INSTANCE;
    }

    public static SurfaceRules.ConditionSource temperature() {
        return SurfaceRules.Temperature.INSTANCE;
    }

    public static SurfaceRules.RuleSource ifTrue(SurfaceRules.ConditionSource param0, SurfaceRules.RuleSource param1) {
        return new SurfaceRules.TestRuleSource(param0, param1);
    }

    public static SurfaceRules.RuleSource sequence(SurfaceRules.RuleSource... param0) {
        if (param0.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        } else {
            return new SurfaceRules.SequenceRuleSource(Arrays.asList(param0));
        }
    }

    public static SurfaceRules.RuleSource state(BlockState param0) {
        return new SurfaceRules.BlockRuleSource(param0);
    }

    public static SurfaceRules.RuleSource bandlands() {
        return SurfaceRules.Bandlands.INSTANCE;
    }

    static <A> Codec<? extends A> register(Registry<Codec<? extends A>> param0, String param1, KeyDispatchDataCodec<? extends A> param2) {
        return Registry.register(param0, param1, param2.codec());
    }

    static enum AbovePreliminarySurface implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.AbovePreliminarySurface> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.abovePreliminarySurface;
        }
    }

    static enum Bandlands implements SurfaceRules.RuleSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Bandlands> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            return param0.system::getBand;
        }
    }

    static final class BiomeConditionSource implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.BiomeConditionSource> CODEC = KeyDispatchDataCodec.of(
            ResourceKey.codec(Registries.BIOME).listOf().fieldOf("biome_is").xmap(SurfaceRules::isBiome, param0 -> param0.biomes)
        );
        private final List<ResourceKey<Biome>> biomes;
        final Predicate<ResourceKey<Biome>> biomeNameTest;

        BiomeConditionSource(List<ResourceKey<Biome>> param0) {
            this.biomes = param0;
            this.biomeNameTest = Set.copyOf(param0)::contains;
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            class BiomeCondition extends SurfaceRules.LazyYCondition {
                BiomeCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    return this.context.biome.get().is(BiomeConditionSource.this.biomeNameTest);
                }
            }

            return new BiomeCondition();
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else {
                return param0 instanceof SurfaceRules.BiomeConditionSource var0 ? this.biomes.equals(var0.biomes) : false;
            }
        }

        @Override
        public int hashCode() {
            return this.biomes.hashCode();
        }

        @Override
        public String toString() {
            return "BiomeConditionSource[biomes=" + this.biomes + "]";
        }
    }

    static record BlockRuleSource(BlockState resultState, SurfaceRules.StateRule rule) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.BlockRuleSource> CODEC = KeyDispatchDataCodec.of(
            BlockState.CODEC.xmap(SurfaceRules.BlockRuleSource::new, SurfaceRules.BlockRuleSource::resultState).fieldOf("result_state")
        );

        BlockRuleSource(BlockState param0) {
            this(param0, new SurfaceRules.StateRule(param0));
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            return this.rule;
        }
    }

    interface Condition {
        boolean test();
    }

    public interface ConditionSource extends Function<SurfaceRules.Context, SurfaceRules.Condition> {
        Codec<SurfaceRules.ConditionSource> CODEC = BuiltInRegistries.MATERIAL_CONDITION
            .byNameCodec()
            .dispatch(param0 -> param0.codec().codec(), Function.identity());

        static Codec<? extends SurfaceRules.ConditionSource> bootstrap(Registry<Codec<? extends SurfaceRules.ConditionSource>> param0) {
            SurfaceRules.register(param0, "biome", SurfaceRules.BiomeConditionSource.CODEC);
            SurfaceRules.register(param0, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
            SurfaceRules.register(param0, "vertical_gradient", SurfaceRules.VerticalGradientConditionSource.CODEC);
            SurfaceRules.register(param0, "y_above", SurfaceRules.YConditionSource.CODEC);
            SurfaceRules.register(param0, "water", SurfaceRules.WaterConditionSource.CODEC);
            SurfaceRules.register(param0, "temperature", SurfaceRules.Temperature.CODEC);
            SurfaceRules.register(param0, "steep", SurfaceRules.Steep.CODEC);
            SurfaceRules.register(param0, "not", SurfaceRules.NotConditionSource.CODEC);
            SurfaceRules.register(param0, "hole", SurfaceRules.Hole.CODEC);
            SurfaceRules.register(param0, "above_preliminary_surface", SurfaceRules.AbovePreliminarySurface.CODEC);
            return SurfaceRules.register(param0, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
        }

        KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec();
    }

    protected static final class Context {
        private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
        private static final int SURFACE_CELL_BITS = 4;
        private static final int SURFACE_CELL_SIZE = 16;
        private static final int SURFACE_CELL_MASK = 15;
        final SurfaceSystem system;
        final SurfaceRules.Condition temperature = new SurfaceRules.Context.TemperatureHelperCondition(this);
        final SurfaceRules.Condition steep = new SurfaceRules.Context.SteepMaterialCondition(this);
        final SurfaceRules.Condition hole = new SurfaceRules.Context.HoleCondition(this);
        final SurfaceRules.Condition abovePreliminarySurface = new SurfaceRules.Context.AbovePreliminarySurfaceCondition();
        final RandomState randomState;
        final ChunkAccess chunk;
        private final NoiseChunk noiseChunk;
        private final Function<BlockPos, Holder<Biome>> biomeGetter;
        final WorldGenerationContext context;
        private long lastPreliminarySurfaceCellOrigin = Long.MAX_VALUE;
        private final int[] preliminarySurfaceCache = new int[4];
        long lastUpdateXZ = -9223372036854775807L;
        int blockX;
        int blockZ;
        int surfaceDepth;
        private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
        private double surfaceSecondary;
        private long lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;
        private int minSurfaceLevel;
        long lastUpdateY = -9223372036854775807L;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Supplier<Holder<Biome>> biome;
        int blockY;
        int waterHeight;
        int stoneDepthBelow;
        int stoneDepthAbove;

        protected Context(
            SurfaceSystem param0,
            RandomState param1,
            ChunkAccess param2,
            NoiseChunk param3,
            Function<BlockPos, Holder<Biome>> param4,
            Registry<Biome> param5,
            WorldGenerationContext param6
        ) {
            this.system = param0;
            this.randomState = param1;
            this.chunk = param2;
            this.noiseChunk = param3;
            this.biomeGetter = param4;
            this.context = param6;
        }

        protected void updateXZ(int param0, int param1) {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = param0;
            this.blockZ = param1;
            this.surfaceDepth = this.system.getSurfaceDepth(param0, param1);
        }

        protected void updateY(int param0, int param1, int param2, int param3, int param4, int param5) {
            ++this.lastUpdateY;
            this.biome = Suppliers.memoize(() -> this.biomeGetter.apply(this.pos.set(param3, param4, param5)));
            this.blockY = param4;
            this.waterHeight = param2;
            this.stoneDepthBelow = param1;
            this.stoneDepthAbove = param0;
        }

        protected double getSurfaceSecondary() {
            if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
                this.lastSurfaceDepth2Update = this.lastUpdateXZ;
                this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
            }

            return this.surfaceSecondary;
        }

        private static int blockCoordToSurfaceCell(int param0) {
            return param0 >> 4;
        }

        private static int surfaceCellToBlockCoord(int param0) {
            return param0 << 4;
        }

        protected int getMinSurfaceLevel() {
            if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
                this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
                int var0 = blockCoordToSurfaceCell(this.blockX);
                int var1 = blockCoordToSurfaceCell(this.blockZ);
                long var2 = ChunkPos.asLong(var0, var1);
                if (this.lastPreliminarySurfaceCellOrigin != var2) {
                    this.lastPreliminarySurfaceCellOrigin = var2;
                    this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(var0), surfaceCellToBlockCoord(var1));
                    this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(var0 + 1), surfaceCellToBlockCoord(var1));
                    this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(var0), surfaceCellToBlockCoord(var1 + 1));
                    this.preliminarySurfaceCache[3] = this.noiseChunk
                        .preliminarySurfaceLevel(surfaceCellToBlockCoord(var0 + 1), surfaceCellToBlockCoord(var1 + 1));
                }

                int var3 = Mth.floor(
                    Mth.lerp2(
                        (double)((float)(this.blockX & 15) / 16.0F),
                        (double)((float)(this.blockZ & 15) / 16.0F),
                        (double)this.preliminarySurfaceCache[0],
                        (double)this.preliminarySurfaceCache[1],
                        (double)this.preliminarySurfaceCache[2],
                        (double)this.preliminarySurfaceCache[3]
                    )
                );
                this.minSurfaceLevel = var3 + this.surfaceDepth - 8;
            }

            return this.minSurfaceLevel;
        }

        final class AbovePreliminarySurfaceCondition implements SurfaceRules.Condition {
            @Override
            public boolean test() {
                return Context.this.blockY >= Context.this.getMinSurfaceLevel();
            }
        }

        static final class HoleCondition extends SurfaceRules.LazyXZCondition {
            HoleCondition(SurfaceRules.Context param0) {
                super(param0);
            }

            @Override
            protected boolean compute() {
                return this.context.surfaceDepth <= 0;
            }
        }

        static class SteepMaterialCondition extends SurfaceRules.LazyXZCondition {
            SteepMaterialCondition(SurfaceRules.Context param0) {
                super(param0);
            }

            @Override
            protected boolean compute() {
                int var0 = this.context.blockX & 15;
                int var1 = this.context.blockZ & 15;
                int var2 = Math.max(var1 - 1, 0);
                int var3 = Math.min(var1 + 1, 15);
                ChunkAccess var4 = this.context.chunk;
                int var5 = var4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var0, var2);
                int var6 = var4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var0, var3);
                if (var6 >= var5 + 4) {
                    return true;
                } else {
                    int var7 = Math.max(var0 - 1, 0);
                    int var8 = Math.min(var0 + 1, 15);
                    int var9 = var4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var7, var1);
                    int var10 = var4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8, var1);
                    return var9 >= var10 + 4;
                }
            }
        }

        static class TemperatureHelperCondition extends SurfaceRules.LazyYCondition {
            TemperatureHelperCondition(SurfaceRules.Context param0) {
                super(param0);
            }

            @Override
            protected boolean compute() {
                return this.context.biome.get().value().coldEnoughToSnow(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
            }
        }
    }

    static enum Hole implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Hole> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.hole;
        }
    }

    abstract static class LazyCondition implements SurfaceRules.Condition {
        protected final SurfaceRules.Context context;
        private long lastUpdate;
        @Nullable
        Boolean result;

        protected LazyCondition(SurfaceRules.Context param0) {
            this.context = param0;
            this.lastUpdate = this.getContextLastUpdate() - 1L;
        }

        @Override
        public boolean test() {
            long var0 = this.getContextLastUpdate();
            if (var0 == this.lastUpdate) {
                if (this.result == null) {
                    throw new IllegalStateException("Update triggered but the result is null");
                } else {
                    return this.result;
                }
            } else {
                this.lastUpdate = var0;
                this.result = this.compute();
                return this.result;
            }
        }

        protected abstract long getContextLastUpdate();

        protected abstract boolean compute();
    }

    abstract static class LazyXZCondition extends SurfaceRules.LazyCondition {
        protected LazyXZCondition(SurfaceRules.Context param0) {
            super(param0);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateXZ;
        }
    }

    abstract static class LazyYCondition extends SurfaceRules.LazyCondition {
        protected LazyYCondition(SurfaceRules.Context param0) {
            super(param0);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateY;
        }
    }

    static record NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.NoiseThresholdConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(SurfaceRules.NoiseThresholdConditionSource::noise),
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::minThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::maxThreshold)
                        )
                        .apply(param0, SurfaceRules.NoiseThresholdConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            final NormalNoise var0 = param0.randomState.getOrCreateNoise(this.noise);

            class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition {
                NoiseThresholdCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    double var0 = var0.getValue((double)this.context.blockX, 0.0, (double)this.context.blockZ);
                    return var0 >= NoiseThresholdConditionSource.this.minThreshold && var0 <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }

            return new NoiseThresholdCondition();
        }
    }

    static record NotCondition(SurfaceRules.Condition target) implements SurfaceRules.Condition {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static record NotConditionSource(SurfaceRules.ConditionSource target) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.NotConditionSource> CODEC = KeyDispatchDataCodec.of(
            SurfaceRules.ConditionSource.CODEC.xmap(SurfaceRules.NotConditionSource::new, SurfaceRules.NotConditionSource::target).fieldOf("invert")
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return new SurfaceRules.NotCondition(this.target.apply(param0));
        }
    }

    public interface RuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule> {
        Codec<SurfaceRules.RuleSource> CODEC = BuiltInRegistries.MATERIAL_RULE.byNameCodec().dispatch(param0 -> param0.codec().codec(), Function.identity());

        static Codec<? extends SurfaceRules.RuleSource> bootstrap(Registry<Codec<? extends SurfaceRules.RuleSource>> param0) {
            SurfaceRules.register(param0, "bandlands", SurfaceRules.Bandlands.CODEC);
            SurfaceRules.register(param0, "block", SurfaceRules.BlockRuleSource.CODEC);
            SurfaceRules.register(param0, "sequence", SurfaceRules.SequenceRuleSource.CODEC);
            return SurfaceRules.register(param0, "condition", SurfaceRules.TestRuleSource.CODEC);
        }

        KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec();
    }

    static record SequenceRule(List<SurfaceRules.SurfaceRule> rules) implements SurfaceRules.SurfaceRule {
        @Nullable
        @Override
        public BlockState tryApply(int param0, int param1, int param2) {
            for(SurfaceRules.SurfaceRule var0 : this.rules) {
                BlockState var1 = var0.tryApply(param0, param1, param2);
                if (var1 != null) {
                    return var1;
                }
            }

            return null;
        }
    }

    static record SequenceRuleSource(List<SurfaceRules.RuleSource> sequence) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.SequenceRuleSource> CODEC = KeyDispatchDataCodec.of(
            SurfaceRules.RuleSource.CODEC.listOf().xmap(SurfaceRules.SequenceRuleSource::new, SurfaceRules.SequenceRuleSource::sequence).fieldOf("sequence")
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            if (this.sequence.size() == 1) {
                return this.sequence.get(0).apply(param0);
            } else {
                Builder<SurfaceRules.SurfaceRule> var0 = ImmutableList.builder();

                for(SurfaceRules.RuleSource var1 : this.sequence) {
                    var0.add(var1.apply(param0));
                }

                return new SurfaceRules.SequenceRule(var0.build());
            }
        }
    }

    static record StateRule(BlockState state) implements SurfaceRules.SurfaceRule {
        @Override
        public BlockState tryApply(int param0, int param1, int param2) {
            return this.state;
        }
    }

    static enum Steep implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Steep> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.steep;
        }
    }

    static record StoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.StoneDepthCheck> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            Codec.INT.fieldOf("offset").forGetter(SurfaceRules.StoneDepthCheck::offset),
                            Codec.BOOL.fieldOf("add_surface_depth").forGetter(SurfaceRules.StoneDepthCheck::addSurfaceDepth),
                            Codec.INT.fieldOf("secondary_depth_range").forGetter(SurfaceRules.StoneDepthCheck::secondaryDepthRange),
                            CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
                        )
                        .apply(param0, SurfaceRules.StoneDepthCheck::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            final boolean var0 = this.surfaceType == CaveSurface.CEILING;

            class StoneDepthCondition extends SurfaceRules.LazyYCondition {
                StoneDepthCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    int var0 = var0 ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
                    int var1 = StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0;
                    int var2 = StoneDepthCheck.this.secondaryDepthRange == 0
                        ? 0
                        : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, (double)StoneDepthCheck.this.secondaryDepthRange);
                    return var0 <= 1 + StoneDepthCheck.this.offset + var1 + var2;
                }
            }

            return new StoneDepthCondition();
        }
    }

    protected interface SurfaceRule {
        @Nullable
        BlockState tryApply(int var1, int var2, int var3);
    }

    static enum Temperature implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Temperature> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.temperature;
        }
    }

    static record TestRule(SurfaceRules.Condition condition, SurfaceRules.SurfaceRule followup) implements SurfaceRules.SurfaceRule {
        @Nullable
        @Override
        public BlockState tryApply(int param0, int param1, int param2) {
            return !this.condition.test() ? null : this.followup.tryApply(param0, param1, param2);
        }
    }

    static record TestRuleSource(SurfaceRules.ConditionSource ifTrue, SurfaceRules.RuleSource thenRun) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.TestRuleSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            SurfaceRules.ConditionSource.CODEC.fieldOf("if_true").forGetter(SurfaceRules.TestRuleSource::ifTrue),
                            SurfaceRules.RuleSource.CODEC.fieldOf("then_run").forGetter(SurfaceRules.TestRuleSource::thenRun)
                        )
                        .apply(param0, SurfaceRules.TestRuleSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            return new SurfaceRules.TestRule(this.ifTrue.apply(param0), this.thenRun.apply(param0));
        }
    }

    static record VerticalGradientConditionSource(ResourceLocation randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.VerticalGradientConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            ResourceLocation.CODEC.fieldOf("random_name").forGetter(SurfaceRules.VerticalGradientConditionSource::randomName),
                            VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(SurfaceRules.VerticalGradientConditionSource::trueAtAndBelow),
                            VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(SurfaceRules.VerticalGradientConditionSource::falseAtAndAbove)
                        )
                        .apply(param0, SurfaceRules.VerticalGradientConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            final int var0 = this.trueAtAndBelow().resolveY(param0.context);
            final int var1 = this.falseAtAndAbove().resolveY(param0.context);
            final PositionalRandomFactory var2 = param0.randomState.getOrCreateRandomFactory(this.randomName());

            class VerticalGradientCondition extends SurfaceRules.LazyYCondition {
                VerticalGradientCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    int var0 = this.context.blockY;
                    if (var0 <= var0) {
                        return true;
                    } else if (var0 >= var1) {
                        return false;
                    } else {
                        double var1 = Mth.map((double)var0, (double)var0, (double)var1, 1.0, 0.0);
                        RandomSource var2 = var2.at(this.context.blockX, var0, this.context.blockZ);
                        return (double)var2.nextFloat() < var1;
                    }
                }
            }

            return new VerticalGradientCondition();
        }
    }

    static record WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.WaterConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
                            Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::surfaceDepthMultiplier),
                            Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
                        )
                        .apply(param0, SurfaceRules.WaterConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            class WaterCondition extends SurfaceRules.LazyYCondition {
                WaterCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    return this.context.waterHeight == Integer.MIN_VALUE
                        || this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                            >= this.context.waterHeight
                                + WaterConditionSource.this.offset
                                + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
                }
            }

            return new WaterCondition();
        }
    }

    static record YConditionSource(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.YConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
                            Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.YConditionSource::surfaceDepthMultiplier),
                            Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
                        )
                        .apply(param0, SurfaceRules.YConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            class YCondition extends SurfaceRules.LazyYCondition {
                YCondition() {
                    super(param0);
                }

                @Override
                protected boolean compute() {
                    return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                        >= YConditionSource.this.anchor.resolveY(this.context.context)
                            + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
                }
            }

            return new YCondition();
        }
    }
}
