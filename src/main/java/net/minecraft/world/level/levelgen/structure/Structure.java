package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
    public static final Codec<Structure> DIRECT_CODEC = Registry.STRUCTURE_TYPES.byNameCodec().dispatch(Structure::type, StructureType::codec);
    public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_REGISTRY, DIRECT_CODEC);
    protected final Structure.StructureSettings settings;

    public static <S extends Structure> RecordCodecBuilder<S, Structure.StructureSettings> settingsCodec(Instance<S> param0) {
        return Structure.StructureSettings.CODEC.forGetter(param0x -> param0x.settings);
    }

    public static <S extends Structure> Codec<S> simpleCodec(Function<Structure.StructureSettings, S> param0) {
        return RecordCodecBuilder.create(param1 -> param1.group(settingsCodec(param1)).apply(param1, param0));
    }

    protected Structure(Structure.StructureSettings param0) {
        this.settings = param0;
    }

    public HolderSet<Biome> biomes() {
        return this.settings.biomes;
    }

    public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
        return this.settings.spawnOverrides;
    }

    public GenerationStep.Decoration step() {
        return this.settings.step;
    }

    public TerrainAdjustment terrainAdaptation() {
        return this.settings.terrainAdaptation;
    }

    public BoundingBox adjustBoundingBox(BoundingBox param0) {
        return this.terrainAdaptation() != TerrainAdjustment.NONE ? param0.inflatedBy(12) : param0;
    }

    public StructureStart generate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        RandomState param3,
        StructureTemplateManager param4,
        long param5,
        ChunkPos param6,
        int param7,
        LevelHeightAccessor param8,
        Predicate<Holder<Biome>> param9
    ) {
        Optional<Structure.GenerationStub> var0 = this.findGenerationPoint(
            new Structure.GenerationContext(param0, param1, param2, param3, param4, param5, param6, param8, param9)
        );
        if (var0.isPresent() && isValidBiome(var0.get(), param1, param3, param9)) {
            StructurePiecesBuilder var1 = var0.get().getPiecesBuilder();
            StructureStart var2 = new StructureStart(this, param6, param7, var1.build());
            if (var2.isValid()) {
                return var2;
            }
        }

        return StructureStart.INVALID_START;
    }

    protected static Optional<Structure.GenerationStub> onTopOfChunkCenter(
        Structure.GenerationContext param0, Heightmap.Types param1, Consumer<StructurePiecesBuilder> param2
    ) {
        ChunkPos var0 = param0.chunkPos();
        int var1 = var0.getMiddleBlockX();
        int var2 = var0.getMiddleBlockZ();
        int var3 = param0.chunkGenerator().getFirstOccupiedHeight(var1, var2, param1, param0.heightAccessor(), param0.randomState());
        return Optional.of(new Structure.GenerationStub(new BlockPos(var1, var3, var2), param2));
    }

    private static boolean isValidBiome(Structure.GenerationStub param0, ChunkGenerator param1, RandomState param2, Predicate<Holder<Biome>> param3) {
        BlockPos var0 = param0.position();
        return param3.test(
            param1.getBiomeSource()
                .getNoiseBiome(QuartPos.fromBlock(var0.getX()), QuartPos.fromBlock(var0.getY()), QuartPos.fromBlock(var0.getZ()), param2.sampler())
        );
    }

    public void afterPlace(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, PiecesContainer param6
    ) {
    }

    private static int[] getCornerHeights(Structure.GenerationContext param0, int param1, int param2, int param3, int param4) {
        ChunkGenerator var0 = param0.chunkGenerator();
        LevelHeightAccessor var1 = param0.heightAccessor();
        RandomState var2 = param0.randomState();
        return new int[]{
            var0.getFirstOccupiedHeight(param1, param3, Heightmap.Types.WORLD_SURFACE_WG, var1, var2),
            var0.getFirstOccupiedHeight(param1, param3 + param4, Heightmap.Types.WORLD_SURFACE_WG, var1, var2),
            var0.getFirstOccupiedHeight(param1 + param2, param3, Heightmap.Types.WORLD_SURFACE_WG, var1, var2),
            var0.getFirstOccupiedHeight(param1 + param2, param3 + param4, Heightmap.Types.WORLD_SURFACE_WG, var1, var2)
        };
    }

    protected static int getLowestY(Structure.GenerationContext param0, int param1, int param2) {
        ChunkPos var0 = param0.chunkPos();
        int var1 = var0.getMinBlockX();
        int var2 = var0.getMinBlockZ();
        return getLowestY(param0, var1, var2, param1, param2);
    }

    protected static int getLowestY(Structure.GenerationContext param0, int param1, int param2, int param3, int param4) {
        int[] var0 = getCornerHeights(param0, param1, param3, param2, param4);
        return Math.min(Math.min(var0[0], var0[1]), Math.min(var0[2], var0[3]));
    }

    @Deprecated
    protected BlockPos getLowestYIn5by5BoxOffset7Blocks(Structure.GenerationContext param0, Rotation param1) {
        int var0 = 5;
        int var1 = 5;
        if (param1 == Rotation.CLOCKWISE_90) {
            var0 = -5;
        } else if (param1 == Rotation.CLOCKWISE_180) {
            var0 = -5;
            var1 = -5;
        } else if (param1 == Rotation.COUNTERCLOCKWISE_90) {
            var1 = -5;
        }

        ChunkPos var2 = param0.chunkPos();
        int var3 = var2.getBlockX(7);
        int var4 = var2.getBlockZ(7);
        return new BlockPos(var3, getLowestY(param0, var3, var4, var0, var1), var4);
    }

    public abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext var1);

    public abstract StructureType<?> type();

    public static record GenerationContext(
        RegistryAccess registryAccess,
        ChunkGenerator chunkGenerator,
        BiomeSource biomeSource,
        RandomState randomState,
        StructureTemplateManager structureTemplateManager,
        WorldgenRandom random,
        long seed,
        ChunkPos chunkPos,
        LevelHeightAccessor heightAccessor,
        Predicate<Holder<Biome>> validBiome
    ) {
        public GenerationContext(
            RegistryAccess param0,
            ChunkGenerator param1,
            BiomeSource param2,
            RandomState param3,
            StructureTemplateManager param4,
            long param5,
            ChunkPos param6,
            LevelHeightAccessor param7,
            Predicate<Holder<Biome>> param8
        ) {
            this(param0, param1, param2, param3, param4, makeRandom(param5, param6), param5, param6, param7, param8);
        }

        private static WorldgenRandom makeRandom(long param0, ChunkPos param1) {
            WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
            var0.setLargeFeatureSeed(param0, param1.x, param1.z);
            return var0;
        }
    }

    public static record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
        public GenerationStub(BlockPos param0, Consumer<StructurePiecesBuilder> param1) {
            this(param0, Either.left(param1));
        }

        public StructurePiecesBuilder getPiecesBuilder() {
            return this.generator.map(param0 -> {
                StructurePiecesBuilder var0 = new StructurePiecesBuilder();
                param0.accept(var0);
                return var0;
            }, param0 -> param0);
        }
    }

    public static record StructureSettings(
        HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation
    ) {
        public static final MapCodec<Structure.StructureSettings> CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(Structure.StructureSettings::biomes),
                        Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values()))
                            .fieldOf("spawn_overrides")
                            .forGetter(Structure.StructureSettings::spawnOverrides),
                        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure.StructureSettings::step),
                        TerrainAdjustment.CODEC
                            .optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE)
                            .forGetter(Structure.StructureSettings::terrainAdaptation)
                    )
                    .apply(param0, Structure.StructureSettings::new)
        );
    }
}
