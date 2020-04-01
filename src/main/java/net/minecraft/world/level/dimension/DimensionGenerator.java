package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceSettings;
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.special.G01;
import net.minecraft.world.level.dimension.special.G02;
import net.minecraft.world.level.dimension.special.G03;
import net.minecraft.world.level.dimension.special.G04;
import net.minecraft.world.level.dimension.special.G05;
import net.minecraft.world.level.dimension.special.G06;
import net.minecraft.world.level.dimension.special.G07;
import net.minecraft.world.level.dimension.special.G08;
import net.minecraft.world.level.dimension.special.G09;
import net.minecraft.world.level.dimension.special.G10;
import net.minecraft.world.level.dimension.special.G11;
import net.minecraft.world.level.dimension.special.G12;
import net.minecraft.world.level.dimension.special.G13;
import net.minecraft.world.level.dimension.special.G14;
import net.minecraft.world.level.dimension.special.G15;
import net.minecraft.world.level.dimension.special.G16;
import net.minecraft.world.level.dimension.special.G17;
import net.minecraft.world.level.dimension.special.G18;
import net.minecraft.world.level.dimension.special.G19;
import net.minecraft.world.level.dimension.special.G20;
import net.minecraft.world.level.dimension.special.G21;
import net.minecraft.world.level.dimension.special.G22;
import net.minecraft.world.level.dimension.special.G23;
import net.minecraft.world.level.dimension.special.G24;
import net.minecraft.world.level.dimension.special.G25;
import net.minecraft.world.level.dimension.special.G26;
import net.minecraft.world.level.dimension.special.G27;
import net.minecraft.world.level.dimension.special.G28;
import net.minecraft.world.level.dimension.special.G29;
import net.minecraft.world.level.dimension.special.G30;
import net.minecraft.world.level.dimension.special.G31;
import net.minecraft.world.level.dimension.special.G32;
import net.minecraft.world.level.dimension.special.G33;
import net.minecraft.world.level.dimension.special.G34;
import net.minecraft.world.level.dimension.special.G35;
import net.minecraft.world.level.dimension.special.G36;
import net.minecraft.world.level.dimension.special.G37;
import net.minecraft.world.level.dimension.special.G38;
import net.minecraft.world.level.dimension.special.G39;
import net.minecraft.world.level.dimension.special.G40;
import net.minecraft.world.level.dimension.special.LastPage;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DimensionGenerator {
    private static final BiomeZoomer[] ZOOMERS = new BiomeZoomer[]{
        FuzzyOffsetBiomeZoomer.INSTANCE, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, NearestNeighborBiomeZoomer.INSTANCE
    };
    private static final Int2ObjectMap<IntFunction<DimensionType>> CUSTOMS = new Int2ObjectOpenHashMap<>();

    private static IntFunction<DimensionType> createStandardGenerator(
        BiFunction<Level, DimensionType, ? extends Dimension> param0, boolean param1, BiomeZoomer param2
    ) {
        return param3 -> new DimensionType(param3, "_" + param3, "DIM" + param3, param0, param1, param2);
    }

    private static IntFunction<DimensionType> createNonStandardGenerator(
        BiFunction<Level, DimensionType, ? extends Dimension> param0, boolean param1, BiomeZoomer param2
    ) {
        return param3 -> new DimensionType(param3, "_" + param3, "DIM" + param3, param0, param1, param2) {
                @Override
                public boolean requirePortalGen() {
                    return true;
                }
            };
    }

    public static DimensionType perform(int param0) {
        IntFunction<DimensionType> var0 = CUSTOMS.get(param0);
        if (var0 != null) {
            return var0.apply(param0);
        } else {
            WorldgenRandom var1 = new WorldgenRandom((long)param0);
            BiomeZoomer var2 = ZOOMERS[var1.nextInt(ZOOMERS.length)];
            boolean var3 = var1.nextBoolean();
            int var4 = var1.nextInt();
            return new DimensionType(
                param0, "_" + param0, "DIM" + param0, (param1, param2) -> DimensionGenerator.GeneratedDimension.create(param1, param2, var4), var3, var2
            );
        }
    }

    static {
        CUSTOMS.put(741472677, createStandardGenerator(G01::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(236157810, createStandardGenerator(G02::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1896587401, createStandardGenerator(G03::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(726931095, createStandardGenerator(G04::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(233542201, createStandardGenerator(G05::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(669175628, createStandardGenerator(G06::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1929426645, createStandardGenerator(G07::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(378547252, createStandardGenerator(G08::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(94341406, createStandardGenerator(G09::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1174283440, createStandardGenerator(G10::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1210674279, createStandardGenerator(G11::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(344885676, createStandardGenerator(G12::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(31674686, createNonStandardGenerator(G13::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(2114493792, createStandardGenerator(G14.create(new Vector3f(1.0F, 0.0F, 0.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1143264807, createStandardGenerator(G14.create(new Vector3f(0.0F, 1.0F, 0.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1049823113, createStandardGenerator(G14.create(new Vector3f(0.0F, 0.0F, 1.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1011847535, createStandardGenerator(G15::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1902968744, createStandardGenerator(G16::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(264458659, createStandardGenerator(G17::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1201319931, createStandardGenerator(G18::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1113696725, createStandardGenerator(G19::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1384344230, createStandardGenerator(G20::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(214387762, createStandardGenerator(G21::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1098962767, createStandardGenerator(G22::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(927632079, createStandardGenerator(G23::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(307219718, createStandardGenerator(G24::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(545072168, createStandardGenerator(G25::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1834117187, createStandardGenerator(G26::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(661885389, createStandardGenerator(G27::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1036032341, createStandardGenerator(G28::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(484336196, createStandardGenerator(G29::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1059552697, createStandardGenerator(G30::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(907661935, createStandardGenerator(G31::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1141490659, createStandardGenerator(G32::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(1028465021, createStandardGenerator(G33::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(2003598857, createStandardGenerator(G34::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(985130845, createStandardGenerator(G35::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(107712651, createStandardGenerator(G36::new, true, NearestNeighborBiomeZoomer.INSTANCE));
        CUSTOMS.put(251137100, createStandardGenerator(G37::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1537997313, createStandardGenerator(G38::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1916276638, createStandardGenerator(G39::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(894945615, createStandardGenerator(G40::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
        CUSTOMS.put(1791460938, createStandardGenerator(LastPage::new, true, NearestNeighborBiomeZoomer.INSTANCE));
    }

    static class GeneratedDimension extends Dimension {
        private final boolean fixedTime;
        private final float fixedTimeValue;
        private final double ticksPerDay;
        private final boolean naturalDimension;
        private final boolean endSky;
        private final float sunSize;
        private final Vector3f sunTint;
        private final float moonSize;
        private final Vector3f moonTint;
        private final Vec3 fogA;
        private final Vec3 fogB;
        private final boolean foggy;
        private final Supplier<ChunkGenerator> generatorProvider;
        @Nullable
        private final Object2FloatMap<Direction> customShade;
        private final int cloudHeight;
        @Nullable
        private final Vector3f[] lightmapNoise;
        @Nullable
        private final Vector3f[] tintVariants;

        public static Dimension create(Level param0, DimensionType param1, int param2) {
            return new DimensionGenerator.GeneratedDimension(param0, param1, new WorldgenRandom((long)param2));
        }

        private GeneratedDimension(Level param0, DimensionType param1, WorldgenRandom param2) {
            super(param0, param1, param2.nextFloat());
            this.ultraWarm = param2.nextInt(5) == 0;
            this.hasCeiling = param2.nextBoolean();
            this.fixedTime = param2.nextBoolean();
            this.fixedTimeValue = param2.nextFloat();
            this.naturalDimension = param2.nextBoolean();
            this.endSky = param2.nextInt(8) == 0;
            this.ticksPerDay = Math.max(100.0, param2.nextGaussian() * 3.0 * 24000.0);
            this.fogA = new Vec3(param2.nextDouble(), param2.nextDouble(), param2.nextDouble());
            this.fogB = new Vec3(param2.nextDouble(), param2.nextDouble(), param2.nextDouble());
            this.foggy = param2.nextBoolean();
            this.sunSize = (float)Math.max(5.0, 30.0 * (1.0 + 4.0 * param2.nextGaussian()));
            this.moonSize = (float)Math.max(5.0, 20.0 * (1.0 + 4.0 * param2.nextGaussian()));
            this.sunTint = this.generateTint(param2);
            this.moonTint = this.generateTint(param2);
            this.cloudHeight = param2.nextInt(255);
            MultiNoiseBiomeSourceSettings var0 = new MultiNoiseBiomeSourceSettings((long)param2.nextInt());
            Map<Biome, List<Biome.ClimateParameters>> var1 = IntStream.range(2, param2.nextInt(15))
                .mapToObj(param1x -> Math.abs(param2.nextInt()))
                .collect(Collectors.toMap(Registry.BIOME::byId, param1x -> Biome.ClimateParameters.randomList(param2)));

            while(param2.nextBoolean()) {
                var1.put(Biomes.getRandomVanillaBiome(param2), Biome.ClimateParameters.randomList(param2));
            }

            var0.setBiomes(var1);
            BiomeSource var2 = BiomeSourceType.MULTI_NOISE.create(var0);
            if (param2.nextInt(7) == 0) {
                this.customShade = new Object2FloatOpenHashMap<>();

                for(Direction var3 : Direction.values()) {
                    this.customShade.put(var3, (float)Mth.clamp((double)super.getBlockShade(var3, true) + param2.nextGaussian(), 0.0, 1.0));
                }
            } else {
                this.customShade = null;
            }

            if (param2.nextInt(4) == 0) {
                this.lightmapNoise = this.generateLightmapNoise(param2);
            } else {
                this.lightmapNoise = null;
            }

            if (param2.nextInt(3) == 0) {
                this.tintVariants = this.generateTintVariants(param2);
            } else {
                this.tintVariants = null;
            }

            this.generatorProvider = createGeneratorProvider(param0, param2, var2);
        }

        private Vector3f generateTint(WorldgenRandom param0) {
            return param0.nextBoolean() ? new Vector3f(param0.nextFloat(), param0.nextFloat(), param0.nextFloat()) : new Vector3f(1.0F, 1.0F, 1.0F);
        }

        private Vector3f[] generateTintVariants(Random param0) {
            int var0 = param0.nextInt(6) + 2;
            Vector3f[] var1 = new Vector3f[var0];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = new Vector3f(param0.nextFloat(), param0.nextFloat(), param0.nextFloat());
            }

            return var1;
        }

        private static float getNoise(PerlinNoise param0, int param1, int param2) {
            return (float)param0.getValue((double)param1, (double)param2, 0.0);
        }

        private Vector3f[] generateLightmapNoise(WorldgenRandom param0) {
            Vector3f[] var0 = new Vector3f[256];
            PerlinNoise var1 = new PerlinNoise(param0, IntStream.rangeClosed(-3, 0));
            PerlinNoise var2 = new PerlinNoise(param0, IntStream.rangeClosed(-2, 4));
            PerlinNoise var3 = new PerlinNoise(param0, IntStream.rangeClosed(-5, 0));

            for(int var4 = 0; var4 < 16; ++var4) {
                for(int var5 = 0; var5 < 16; ++var5) {
                    Vector3f var6 = new Vector3f(getNoise(var1, var4, var5), getNoise(var2, var4, var5), getNoise(var3, var4, var5));
                    var0[var4 * 16 + var5] = var6;
                }
            }

            return var0;
        }

        private static Supplier<ChunkGenerator> createGeneratorProvider(Level param0, Random param1, BiomeSource param2) {
            int var0 = param1.nextInt();
            switch(param1.nextInt(3)) {
                case 0:
                    return () -> ChunkGeneratorType.SURFACE.create(param0, param2, new OverworldGeneratorSettings(new WorldgenRandom((long)var0)));
                case 1:
                    return () -> ChunkGeneratorType.CAVES.create(param0, param2, new NetherGeneratorSettings(new WorldgenRandom((long)var0)));
                default:
                    return () -> ChunkGeneratorType.FLOATING_ISLANDS.create(param0, param2, new TheEndGeneratorSettings(new WorldgenRandom((long)var0)));
            }
        }

        @Override
        public ChunkGenerator<?> createRandomLevelGenerator() {
            return this.generatorProvider.get();
        }

        @Nullable
        @Override
        public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
            return NormalDimension.getSpawnPosInChunkI(this.level, param0, param1);
        }

        @Nullable
        @Override
        public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
            return NormalDimension.getValidSpawnPositionI(this.level, param0, param1, param2);
        }

        @Override
        public float getTimeOfDay(long param0, float param1) {
            return this.fixedTime ? this.fixedTimeValue : NormalDimension.getTimeOfDayI(param0, this.ticksPerDay);
        }

        @Override
        public boolean isNaturalDimension() {
            return this.naturalDimension;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
            return param0.multiply(
                (double)param1 * this.fogA.x + this.fogB.x, (double)param1 * this.fogA.y + this.fogB.y, (double)param1 * this.fogA.z + this.fogB.z
            );
        }

        @Override
        public boolean mayRespawn() {
            return false;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean isFoggyAt(int param0, int param1) {
            return this.foggy;
        }

        @Override
        public float getBlockShade(Direction param0, boolean param1) {
            return this.customShade != null && param1 ? this.customShade.getFloat(param0) : super.getBlockShade(param0, param1);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void modifyLightmapColor(int param0, int param1, Vector3f param2) {
            if (this.lightmapNoise != null) {
                param2.add(this.lightmapNoise[param1 * 16 + param0]);
                param2.clamp(0.0F, 1.0F);
            }

        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
            if (this.tintVariants == null) {
                return super.getExtraTint(param0, param1);
            } else {
                int var0 = Block.BLOCK_STATE_REGISTRY.getId(param0);
                return this.tintVariants[var0 % this.tintVariants.length];
            }
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
            if (this.tintVariants == null) {
                return super.getEntityExtraTint(param0);
            } else {
                int var0 = Registry.ENTITY_TYPE.getId(param0.getType());
                return this.tintVariants[var0 % this.tintVariants.length];
            }
        }

        @Override
        public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
            T var0 = param0.createMap(
                Stream.of(Direction.values())
                    .collect(
                        ImmutableMap.toImmutableMap(
                            param1 -> param0.createString(param1.getName()), param1 -> param0.createDouble((double)this.getBlockShade(param1, true))
                        )
                    )
            );
            return super.serialize(param0)
                .merge(
                    new Dynamic<>(
                        param0,
                        param0.createMap(
                            ImmutableMap.<T, T>builder()
                                .put(param0.createString("foggy"), param0.createBoolean(this.foggy))
                                .put(param0.createString("fogA"), param0.createList(Stream.of(this.fogA.x, this.fogA.y, this.fogA.z).map(param0::createDouble)))
                                .put(param0.createString("fogB"), param0.createList(Stream.of(this.fogB.x, this.fogB.y, this.fogB.z).map(param0::createDouble)))
                                .put(param0.createString("tickPerDay"), param0.createDouble(this.ticksPerDay))
                                .put(param0.createString("shade"), var0)
                                .build()
                        )
                    )
                );
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean isEndSky() {
            return this.endSky;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public float getSunSize() {
            return this.sunSize;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Vector3f getSunTint() {
            return this.sunTint;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public float getMoonSize() {
            return this.moonSize;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Vector3f getMoonTint() {
            return this.moonTint;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public float getCloudHeight() {
            return (float)this.cloudHeight;
        }
    }
}
