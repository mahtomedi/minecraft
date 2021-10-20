package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
    public static final SurfaceRules.ConditionSource ON_FLOOR = new SurfaceRules.StoneDepthCheck(false, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource UNDER_FLOOR = new SurfaceRules.StoneDepthCheck(true, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource UNDER_CEILING = new SurfaceRules.StoneDepthCheck(true, CaveSurface.CEILING);

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
        return noiseCondition(param0, param1, Double.POSITIVE_INFINITY);
    }

    public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> param0, double param1, double param2) {
        return new SurfaceRules.NoiseThresholdConditionSource(param0, param1, param2);
    }

    public static SurfaceRules.ConditionSource steep() {
        return SurfaceRules.Steep.INSTANCE;
    }

    public static SurfaceRules.ConditionSource hole() {
        return SurfaceRules.Hole.INSTANCE;
    }

    public static SurfaceRules.ConditionSource temperature() {
        return SurfaceRules.Temperature.INSTANCE;
    }

    public static SurfaceRules.RuleSource ifTrue(SurfaceRules.ConditionSource param0, SurfaceRules.RuleSource param1) {
        return new SurfaceRules.TestRuleSource(param0, param1);
    }

    public static SurfaceRules.RuleSource sequence(SurfaceRules.RuleSource param0, SurfaceRules.RuleSource... param1) {
        return new SurfaceRules.SequenceRuleSource(Stream.concat(Stream.of(param0), Arrays.stream(param1)).toList());
    }

    public static SurfaceRules.RuleSource state(BlockState param0) {
        return new SurfaceRules.BlockRuleSource(param0);
    }

    public static SurfaceRules.RuleSource bandlands() {
        return SurfaceRules.Bandlands.INSTANCE;
    }

    static enum Bandlands implements SurfaceRules.RuleSource {
        INSTANCE;

        static final Codec<SurfaceRules.Bandlands> CODEC = Codec.unit(INSTANCE);

        @Override
        public Codec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            return param0.system::getBand;
        }
    }

    static record BiomeConditionSource(List<ResourceKey<Biome>> biomes) implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.BiomeConditionSource> CODEC = ResourceKey.codec(Registry.BIOME_REGISTRY)
            .listOf()
            .fieldOf("biome_is")
            .xmap(SurfaceRules::isBiome, SurfaceRules.BiomeConditionSource::biomes)
            .codec();

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            final Set<ResourceKey<Biome>> var0 = Set.copyOf(this.biomes);

            class BiomeCondition extends SurfaceRules.EagerCondition<ResourceKey<Biome>> {
                protected boolean compute(ResourceKey<Biome> param0) {
                    return var0.contains(param0);
                }
            }

            BiomeCondition var1 = new BiomeCondition();
            param0.biomeConditions.add(var1);
            return var1;
        }
    }

    static record BlockRuleSource(BlockState resultState, SurfaceRules.StateRule rule) implements SurfaceRules.RuleSource {
        static final Codec<SurfaceRules.BlockRuleSource> CODEC = BlockState.CODEC
            .xmap(SurfaceRules.BlockRuleSource::new, SurfaceRules.BlockRuleSource::resultState)
            .fieldOf("result_state")
            .codec();

        BlockRuleSource(BlockState param0) {
            this(param0, new SurfaceRules.StateRule(param0));
        }

        @Override
        public Codec<? extends SurfaceRules.RuleSource> codec() {
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
        Codec<SurfaceRules.ConditionSource> CODEC = Registry.CONDITION.dispatch(SurfaceRules.ConditionSource::codec, Function.identity());

        static Codec<? extends SurfaceRules.ConditionSource> bootstrap() {
            Registry.register(Registry.CONDITION, "biome", SurfaceRules.BiomeConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "y_above", SurfaceRules.YConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "water", SurfaceRules.WaterConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "temperature", SurfaceRules.Temperature.CODEC);
            Registry.register(Registry.CONDITION, "steep", SurfaceRules.Steep.CODEC);
            Registry.register(Registry.CONDITION, "not", SurfaceRules.NotConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "hole", SurfaceRules.Hole.CODEC);
            Registry.register(Registry.CONDITION, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
            return Registry.CONDITION.iterator().next();
        }

        Codec<? extends SurfaceRules.ConditionSource> codec();
    }

    protected static final class Context {
        final SurfaceSystem system;
        final SurfaceRules.UpdatableCondition<SurfaceRules.Context.TemperatureHelperCondition.State> temperature = new SurfaceRules.Context.TemperatureHelperCondition(
            
        );
        final SurfaceRules.UpdatableCondition<SurfaceRules.Context.SteepMaterialCondition.State> steep = new SurfaceRules.Context.SteepMaterialCondition();
        final SurfaceRules.UpdatableCondition<Integer> hole = new SurfaceRules.Context.HoleCondition();
        final List<SurfaceRules.UpdatableCondition<ResourceKey<Biome>>> biomeConditions = new ObjectArrayList<>();
        final List<SurfaceRules.UpdatableCondition<SurfaceRules.NoiseThresholdConditionState>> noiseThresholdConditions = new ObjectArrayList<>();
        final List<SurfaceRules.UpdatableCondition<SurfaceRules.YConditionState>> yConditions = new ObjectArrayList<>();
        boolean hasCeilingRules;
        final WorldGenerationContext context;

        protected Context(SurfaceSystem param0, WorldGenerationContext param1) {
            this.system = param0;
            this.context = param1;
        }

        protected void updateXZ(ChunkAccess param0, int param1, int param2, int param3) {
            SurfaceRules.NoiseThresholdConditionState var0 = new SurfaceRules.NoiseThresholdConditionState(param1, param2);

            for(SurfaceRules.UpdatableCondition<SurfaceRules.NoiseThresholdConditionState> var1 : this.noiseThresholdConditions) {
                var1.update(var0);
            }

            this.steep.update(new SurfaceRules.Context.SteepMaterialCondition.State(param0, param1, param2));
            this.hole.update(param3);
        }

        protected void updateY(ResourceKey<Biome> param0, Biome param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
            for(SurfaceRules.UpdatableCondition<ResourceKey<Biome>> var0 : this.biomeConditions) {
                var0.update(param0);
            }

            SurfaceRules.YConditionState var1 = new SurfaceRules.YConditionState(param7, param3, param4, param2, param5);

            for(SurfaceRules.UpdatableCondition<SurfaceRules.YConditionState> var2 : this.yConditions) {
                var2.update(var1);
            }

            this.temperature.update(new SurfaceRules.Context.TemperatureHelperCondition.State(param1, param6, param7, param8));
        }

        protected boolean hasCeilingRules() {
            return this.hasCeilingRules;
        }

        static final class HoleCondition extends SurfaceRules.EagerCondition<Integer> {
            protected boolean compute(Integer param0) {
                return param0 <= 0;
            }
        }

        static class SteepMaterialCondition extends SurfaceRules.LazyCondition<SurfaceRules.Context.SteepMaterialCondition.State> {
            protected boolean compute(SurfaceRules.Context.SteepMaterialCondition.State param0) {
                int var0 = param0.blockX & 15;
                int var1 = param0.blockZ & 15;
                int var2 = Math.max(var1 - 1, 0);
                int var3 = Math.min(var1 + 1, 15);
                int var4 = param0.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var0, var2);
                int var5 = param0.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var0, var3);
                if (var5 >= var4 + 4) {
                    return true;
                } else {
                    int var6 = Math.max(var0 - 1, 0);
                    int var7 = Math.min(var0 + 1, 15);
                    int var8 = param0.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var6, var1);
                    int var9 = param0.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var7, var1);
                    return var8 >= var9 + 4;
                }
            }

            static record State(ChunkAccess chunk, int blockX, int blockZ) {
            }
        }

        static class TemperatureHelperCondition extends SurfaceRules.LazyCondition<SurfaceRules.Context.TemperatureHelperCondition.State> {
            protected boolean compute(SurfaceRules.Context.TemperatureHelperCondition.State param0) {
                return param0.biome.getTemperature(new BlockPos(param0.blockX, param0.blockY, param0.blockZ)) < 0.15F;
            }

            static record State(Biome biome, int blockX, int blockY, int blockZ) {
            }
        }
    }

    abstract static class EagerCondition<S> implements SurfaceRules.UpdatableCondition<S> {
        boolean state = false;

        @Override
        public void update(S param0) {
            this.state = this.compute(param0);
        }

        @Override
        public boolean test() {
            return this.state;
        }

        protected abstract boolean compute(S var1);
    }

    static enum Hole implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final Codec<SurfaceRules.Hole> CODEC = Codec.unit(INSTANCE);

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.hole;
        }
    }

    abstract static class LazyCondition<S> implements SurfaceRules.UpdatableCondition<S> {
        @Nullable
        private S state;
        @Nullable
        Boolean result;

        @Override
        public void update(S param0) {
            this.state = param0;
            this.result = null;
        }

        @Override
        public boolean test() {
            if (this.result == null) {
                if (this.state == null) {
                    throw new IllegalStateException("Calling test without update");
                }

                this.result = this.compute(this.state);
            }

            return this.result;
        }

        protected abstract boolean compute(S var1);
    }

    static record NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold)
        implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.NoiseThresholdConditionSource> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ResourceKey.codec(Registry.NOISE_REGISTRY).fieldOf("noise").forGetter(SurfaceRules.NoiseThresholdConditionSource::noise),
                        Codec.DOUBLE.fieldOf("min_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::minThreshold),
                        Codec.DOUBLE.fieldOf("max_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::maxThreshold)
                    )
                    .apply(param0, SurfaceRules.NoiseThresholdConditionSource::new)
        );

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            final NormalNoise var0 = param0.system.getOrCreateNoise(this.noise);

            class NoiseThresholdCondition extends SurfaceRules.LazyCondition<SurfaceRules.NoiseThresholdConditionState> {
                protected boolean compute(SurfaceRules.NoiseThresholdConditionState param0) {
                    double var0 = var0.getValue((double)param0.blockX, 0.0, (double)param0.blockZ);
                    return var0 >= NoiseThresholdConditionSource.this.minThreshold && var0 <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }

            NoiseThresholdCondition var1 = new NoiseThresholdCondition();
            param0.noiseThresholdConditions.add(var1);
            return var1;
        }
    }

    static record NoiseThresholdConditionState(int blockX, int blockZ) {
    }

    static record NotCondition(SurfaceRules.Condition target) implements SurfaceRules.Condition {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static record NotConditionSource(SurfaceRules.ConditionSource target) implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.NotConditionSource> CODEC = SurfaceRules.ConditionSource.CODEC
            .xmap(SurfaceRules.NotConditionSource::new, SurfaceRules.NotConditionSource::target)
            .fieldOf("invert")
            .codec();

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return new SurfaceRules.NotCondition(this.target.apply(param0));
        }
    }

    public interface RuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule> {
        Codec<SurfaceRules.RuleSource> CODEC = Registry.RULE.dispatch(SurfaceRules.RuleSource::codec, Function.identity());

        static Codec<? extends SurfaceRules.RuleSource> bootstrap() {
            Registry.register(Registry.RULE, "bandlands", SurfaceRules.Bandlands.CODEC);
            Registry.register(Registry.RULE, "block", SurfaceRules.BlockRuleSource.CODEC);
            Registry.register(Registry.RULE, "sequence", SurfaceRules.SequenceRuleSource.CODEC);
            Registry.register(Registry.RULE, "condition", SurfaceRules.TestRuleSource.CODEC);
            return Registry.RULE.iterator().next();
        }

        Codec<? extends SurfaceRules.RuleSource> codec();
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
        static final Codec<SurfaceRules.SequenceRuleSource> CODEC = SurfaceRules.RuleSource.CODEC
            .listOf()
            .xmap(SurfaceRules.SequenceRuleSource::new, SurfaceRules.SequenceRuleSource::sequence)
            .fieldOf("sequence")
            .codec();

        @Override
        public Codec<? extends SurfaceRules.RuleSource> codec() {
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

        static final Codec<SurfaceRules.Steep> CODEC = Codec.unit(INSTANCE);

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            return param0.steep;
        }
    }

    static record StoneDepthCheck(boolean addRunDepth, CaveSurface surfaceType) implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.StoneDepthCheck> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.BOOL.fieldOf("add_run_depth").forGetter(SurfaceRules.StoneDepthCheck::addRunDepth),
                        CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
                    )
                    .apply(param0, SurfaceRules.StoneDepthCheck::new)
        );

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            final boolean var0 = this.surfaceType == CaveSurface.CEILING;

            class StoneDepthCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
                protected boolean compute(SurfaceRules.YConditionState param0) {
                    return (var0 ? param0.stoneDepthBelow : param0.stoneDepthAbove) <= 1 + (StoneDepthCheck.this.addRunDepth ? param0.runDepth : 0);
                }
            }

            StoneDepthCondition var1 = new StoneDepthCondition();
            param0.yConditions.add(var1);
            if (var0) {
                param0.hasCeilingRules = true;
            }

            return var1;
        }
    }

    protected interface SurfaceRule {
        @Nullable
        BlockState tryApply(int var1, int var2, int var3);
    }

    static enum Temperature implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final Codec<SurfaceRules.Temperature> CODEC = Codec.unit(INSTANCE);

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
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
        static final Codec<SurfaceRules.TestRuleSource> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        SurfaceRules.ConditionSource.CODEC.fieldOf("if_true").forGetter(SurfaceRules.TestRuleSource::ifTrue),
                        SurfaceRules.RuleSource.CODEC.fieldOf("then_run").forGetter(SurfaceRules.TestRuleSource::thenRun)
                    )
                    .apply(param0, SurfaceRules.TestRuleSource::new)
        );

        @Override
        public Codec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context param0) {
            return new SurfaceRules.TestRule(this.ifTrue.apply(param0), this.thenRun.apply(param0));
        }
    }

    interface UpdatableCondition<S> extends SurfaceRules.Condition {
        void update(S var1);
    }

    static record WaterConditionSource(int offset, int runDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.WaterConditionSource> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
                        Codec.intRange(-20, 20).fieldOf("run_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::runDepthMultiplier),
                        Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
                    )
                    .apply(param0, SurfaceRules.WaterConditionSource::new)
        );

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context param0) {
            class WaterCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
                protected boolean compute(SurfaceRules.YConditionState param0) {
                    return param0.waterHeight == Integer.MIN_VALUE
                        || param0.blockY + (WaterConditionSource.this.addStoneDepth ? param0.stoneDepthAbove : 0)
                            >= param0.waterHeight + WaterConditionSource.this.offset + param0.runDepth * WaterConditionSource.this.runDepthMultiplier;
                }
            }

            WaterCondition var0 = new WaterCondition();
            param0.yConditions.add(var0);
            return var0;
        }
    }

    static record YConditionSource(VerticalAnchor anchor, int runDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final Codec<SurfaceRules.YConditionSource> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
                        Codec.intRange(-20, 20).fieldOf("run_depth_multiplier").forGetter(SurfaceRules.YConditionSource::runDepthMultiplier),
                        Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
                    )
                    .apply(param0, SurfaceRules.YConditionSource::new)
        );

        @Override
        public Codec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context param0) {
            class YCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
                protected boolean compute(SurfaceRules.YConditionState param0x) {
                    return param0.blockY + (YConditionSource.this.addStoneDepth ? param0.stoneDepthAbove : 0)
                        >= YConditionSource.this.anchor.resolveY(param0.context) + param0.runDepth * YConditionSource.this.runDepthMultiplier;
                }
            }

            YCondition var0 = new YCondition();
            param0.yConditions.add(var0);
            return var0;
        }
    }

    static record YConditionState(int blockY, int stoneDepthAbove, int stoneDepthBelow, int runDepth, int waterHeight) {
    }
}
