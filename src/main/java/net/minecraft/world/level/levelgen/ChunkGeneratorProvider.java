package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DynamicLike;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class ChunkGeneratorProvider {
    private final LevelType type;
    private final Dynamic<?> settings;
    private final Function<LevelAccessor, ChunkGenerator<?>> supplier;

    public ChunkGeneratorProvider(LevelType param0, Dynamic<?> param1, Function<LevelAccessor, ChunkGenerator<?>> param2) {
        this.type = param0;
        this.settings = param1;
        this.supplier = param2;
    }

    public LevelType getType() {
        return this.type;
    }

    public Dynamic<?> getSettings() {
        return this.settings;
    }

    public ChunkGenerator<?> create(LevelAccessor param0) {
        return this.supplier.apply(param0);
    }

    public static ChunkGeneratorProvider createNormal(LevelType param0, Dynamic<?> param1) {
        OverworldGeneratorSettings var0 = ChunkGeneratorType.SURFACE.createSettings();
        return new ChunkGeneratorProvider(
            param0,
            param1,
            param2 -> {
                OverworldBiomeSourceSettings var0x = BiomeSourceType.VANILLA_LAYERED
                    .createSettings(param2.getSeed())
                    .setLevelType(param0)
                    .setGeneratorSettings(var0);
                return ChunkGeneratorType.SURFACE.create(param2, BiomeSourceType.VANILLA_LAYERED.create(var0x), var0);
            }
        );
    }

    public static ChunkGeneratorProvider createFlat(LevelType param0, Dynamic<?> param1) {
        FlatLevelGeneratorSettings var0 = FlatLevelGeneratorSettings.fromObject(param1);
        return new ChunkGeneratorProvider(param0, param1, param1x -> {
            FixedBiomeSourceSettings var0x = BiomeSourceType.FIXED.createSettings(param1x.getSeed()).setBiome(var0.getBiome());
            return ChunkGeneratorType.FLAT.create(param1x, BiomeSourceType.FIXED.create(var0x), var0);
        });
    }

    private static <T> T getRegistryValue(DynamicLike<?> param0, Registry<T> param1, T param2) {
        return param0.asString().map(ResourceLocation::new).flatMap(param1::getOptional).orElse(param2);
    }

    private static LongFunction<BiomeSource> createBuffetBiomeSource(DynamicLike<?> param0) {
        BiomeSourceType<?, ?> var0 = getRegistryValue(param0.get("type"), Registry.BIOME_SOURCE_TYPE, BiomeSourceType.FIXED);
        DynamicLike<?> var1 = param0.get("options");
        Stream<Biome> var2 = var1.get("biomes")
            .asStreamOpt()
            .map(param0x -> param0x.map(param0xx -> getRegistryValue(param0xx, Registry.BIOME, Biomes.OCEAN)))
            .orElseGet(Stream::empty);
        if (BiomeSourceType.CHECKERBOARD == var0) {
            int var3 = var1.get("size").asInt(2);
            Biome[] var4 = var2.toArray(param0x -> new Biome[param0x]);
            Biome[] var5 = var4.length > 0 ? var4 : new Biome[]{Biomes.OCEAN};
            return param2 -> {
                CheckerboardBiomeSourceSettings var0x = BiomeSourceType.CHECKERBOARD.createSettings(param2).setAllowedBiomes(var5).setSize(var3);
                return BiomeSourceType.CHECKERBOARD.create(var0x);
            };
        } else if (BiomeSourceType.VANILLA_LAYERED == var0) {
            return param0x -> {
                OverworldBiomeSourceSettings var0x = BiomeSourceType.VANILLA_LAYERED.createSettings(param0x);
                return BiomeSourceType.VANILLA_LAYERED.create(var0x);
            };
        } else {
            Biome var6 = var2.findFirst().orElse(Biomes.OCEAN);
            return param1 -> {
                FixedBiomeSourceSettings var0x = BiomeSourceType.FIXED.createSettings(param1).setBiome(var6);
                return BiomeSourceType.FIXED.create(var0x);
            };
        }
    }

    private static void decorateCommonGeneratorSettings(ChunkGeneratorSettings param0, DynamicLike<?> param1) {
        BlockState var0 = getRegistryValue(param1.get("default_block"), Registry.BLOCK, Blocks.STONE).defaultBlockState();
        param0.setDefaultBlock(var0);
        BlockState var1 = getRegistryValue(param1.get("default_fluid"), Registry.BLOCK, Blocks.WATER).defaultBlockState();
        param0.setDefaultFluid(var1);
    }

    private static Function<LevelAccessor, ChunkGenerator<?>> createBuffetGenerator(DynamicLike<?> param0, LongFunction<BiomeSource> param1) {
        ChunkGeneratorType<?, ?> var0 = getRegistryValue(param0.get("type"), Registry.CHUNK_GENERATOR_TYPE, ChunkGeneratorType.SURFACE);
        return createBuffetGeneratorCap(param0, var0, param1);
    }

    private static <C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> Function<LevelAccessor, ChunkGenerator<?>> createBuffetGeneratorCap(
        DynamicLike<?> param0, ChunkGeneratorType<C, T> param1, LongFunction<BiomeSource> param2
    ) {
        C var0 = param1.createSettings();
        if (param1 == ChunkGeneratorType.FLOATING_ISLANDS) {
            TheEndGeneratorSettings var1 = (TheEndGeneratorSettings)var0;
            var1.setSpawnPosition(new BlockPos(0, 64, 0));
        }

        decorateCommonGeneratorSettings(var0, param0.get("options"));
        return param3 -> param1.create(param3, param2.apply(param3.getSeed()), var0);
    }

    public static ChunkGeneratorProvider createBuffet(LevelType param0, Dynamic<?> param1) {
        LongFunction<BiomeSource> var0 = createBuffetBiomeSource(param1.get("biome_source"));
        Function<LevelAccessor, ChunkGenerator<?>> var1 = createBuffetGenerator(param1.get("chunk_generator"), var0);
        return new ChunkGeneratorProvider(param0, param1, var1);
    }

    public static ChunkGeneratorProvider createDebug(LevelType param0, Dynamic<?> param1) {
        return new ChunkGeneratorProvider(param0, param1, param0x -> {
            FixedBiomeSourceSettings var0x = BiomeSourceType.FIXED.createSettings(param0x.getSeed()).setBiome(Biomes.PLAINS);
            return ChunkGeneratorType.DEBUG.create(param0x, BiomeSourceType.FIXED.create(var0x), ChunkGeneratorType.DEBUG.createSettings());
        });
    }
}
