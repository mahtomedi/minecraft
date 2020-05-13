package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DynamicLike;
import com.mojang.datafixers.types.JsonOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
    private static final Dynamic<?> EMPTY_SETTINGS = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
    private static final ChunkGenerator FLAT = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
    private static final int DEMO_SEED = "North Carolina".hashCode();
    public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(
        (long)DEMO_SEED,
        true,
        true,
        WorldGenSettings.LevelType.NORMAL,
        EMPTY_SETTINGS,
        new OverworldLevelSource(new OverworldBiomeSource((long)DEMO_SEED, false, 4), (long)DEMO_SEED, new OverworldGeneratorSettings())
    );
    public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(0L, false, false, WorldGenSettings.LevelType.FLAT, EMPTY_SETTINGS, FLAT);
    private static final Logger LOGGER = LogManager.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final WorldGenSettings.LevelType type;
    private final Dynamic<?> settings;
    private final ChunkGenerator generator;
    @Nullable
    private final String legacyCustomOptions;
    private final boolean isOldCustomizedWorld;
    private static final Map<WorldGenSettings.LevelType, WorldGenSettings.Preset> PRESETS = Maps.newHashMap();

    public WorldGenSettings(long param0, boolean param1, boolean param2, WorldGenSettings.LevelType param3, Dynamic<?> param4, ChunkGenerator param5) {
        this(param0, param1, param2, param3, param4, param5, null, false);
    }

    private WorldGenSettings(
        long param0,
        boolean param1,
        boolean param2,
        WorldGenSettings.LevelType param3,
        Dynamic<?> param4,
        ChunkGenerator param5,
        @Nullable String param6,
        boolean param7
    ) {
        this.seed = param0;
        this.generateFeatures = param1;
        this.generateBonusChest = param2;
        this.legacyCustomOptions = param6;
        this.isOldCustomizedWorld = param7;
        this.type = param3;
        this.settings = param4;
        this.generator = param5;
    }

    public static WorldGenSettings readWorldGenSettings(CompoundTag param0, DataFixer param1, int param2) {
        long var0 = param0.getLong("RandomSeed");
        String var1 = null;
        WorldGenSettings.LevelType var3;
        Dynamic<?> var9;
        ChunkGenerator var10;
        if (param0.contains("generatorName", 8)) {
            String var2 = param0.getString("generatorName");
            var3 = WorldGenSettings.LevelType.byName(var2);
            if (var3 == null) {
                var3 = WorldGenSettings.LevelType.NORMAL;
            } else if (var3 == WorldGenSettings.LevelType.CUSTOMIZED) {
                var1 = param0.getString("generatorOptions");
            } else if (var3 == WorldGenSettings.LevelType.NORMAL) {
                int var4 = 0;
                if (param0.contains("generatorVersion", 99)) {
                    var4 = param0.getInt("generatorVersion");
                }

                if (var4 == 0) {
                    var3 = WorldGenSettings.LevelType.NORMAL_1_1;
                }
            }

            CompoundTag var5 = param0.getCompound("generatorOptions");
            Dynamic<?> var6 = new Dynamic<>(NbtOps.INSTANCE, var5);
            int var7 = Math.max(param2, 2501);
            Dynamic<?> var8 = var6.merge(var6.createString("levelType"), var6.createString(var3.name));
            var9 = param1.update(References.CHUNK_GENERATOR_SETTINGS, var8, var7, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
            var10 = make(var3, var9, var0);
        } else {
            var9 = EMPTY_SETTINGS;
            var10 = new OverworldLevelSource(new OverworldBiomeSource(var0, false, 4), var0, new OverworldGeneratorSettings());
            var3 = WorldGenSettings.LevelType.NORMAL;
        }

        if (param0.contains("legacy_custom_options", 8)) {
            var1 = param0.getString("legacy_custom_options");
        }

        boolean var14;
        if (param0.contains("MapFeatures", 99)) {
            var14 = param0.getBoolean("MapFeatures");
        } else {
            var14 = true;
        }

        boolean var16 = param0.getBoolean("BonusChest");
        boolean var17 = var3 == WorldGenSettings.LevelType.CUSTOMIZED && param2 < 1466;
        return new WorldGenSettings(var0, var14, var16, var3, var9, var10, var1, var17);
    }

    private static ChunkGenerator defaultEndGenerator(long param0) {
        TheEndBiomeSource var0 = new TheEndBiomeSource(param0);
        NoiseGeneratorSettings var1 = new NoiseGeneratorSettings(new ChunkGeneratorSettings());
        var1.setDefaultBlock(Blocks.END_STONE.defaultBlockState());
        var1.setDefaultFluid(Blocks.AIR.defaultBlockState());
        return new TheEndLevelSource(var0, param0, var1);
    }

    private static ChunkGenerator defaultNetherGenerator(long param0) {
        ImmutableList<Biome> var0 = ImmutableList.of(
            Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS
        );
        MultiNoiseBiomeSource var1 = MultiNoiseBiomeSource.of(param0, var0);
        NetherGeneratorSettings var2 = new NetherGeneratorSettings(new ChunkGeneratorSettings());
        var2.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
        var2.setDefaultFluid(Blocks.LAVA.defaultBlockState());
        return new NetherLevelSource(var1, param0, var2);
    }

    @OnlyIn(Dist.CLIENT)
    public static WorldGenSettings makeDefault() {
        long var0 = new Random().nextLong();
        return new WorldGenSettings(
            var0,
            true,
            false,
            WorldGenSettings.LevelType.NORMAL,
            EMPTY_SETTINGS,
            new OverworldLevelSource(new OverworldBiomeSource(var0, false, 4), var0, new OverworldGeneratorSettings())
        );
    }

    public CompoundTag serialize() {
        CompoundTag var0 = new CompoundTag();
        var0.putLong("RandomSeed", this.seed());
        WorldGenSettings.LevelType var1 = this.type == WorldGenSettings.LevelType.CUSTOMIZED ? WorldGenSettings.LevelType.NORMAL : this.type;
        var0.putString("generatorName", var1.name);
        var0.putInt("generatorVersion", this.type == WorldGenSettings.LevelType.NORMAL ? 1 : 0);
        CompoundTag var2 = (CompoundTag)this.settings.convert(NbtOps.INSTANCE).getValue();
        if (!var2.isEmpty()) {
            var0.put("generatorOptions", var2);
        }

        if (this.legacyCustomOptions != null) {
            var0.putString("legacy_custom_options", this.legacyCustomOptions);
        }

        var0.putBoolean("MapFeatures", this.generateFeatures());
        var0.putBoolean("BonusChest", this.generateBonusChest());
        return var0;
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateFeatures() {
        return this.generateFeatures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public Map<DimensionType, ChunkGenerator> generators() {
        return ImmutableMap.of(
            DimensionType.OVERWORLD,
            this.generator,
            DimensionType.NETHER,
            defaultNetherGenerator(this.seed),
            DimensionType.THE_END,
            defaultEndGenerator(this.seed)
        );
    }

    public ChunkGenerator overworld() {
        return this.generator;
    }

    public boolean isDebug() {
        return this.type == WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES;
    }

    public boolean isFlatWorld() {
        return this.type == WorldGenSettings.LevelType.FLAT;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isOldCustomizedWorld() {
        return this.isOldCustomizedWorld;
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(
            this.seed, this.generateFeatures, true, this.type, this.settings, this.generator, this.legacyCustomOptions, this.isOldCustomizedWorld
        );
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.type, this.settings, this.generator);
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.type, this.settings, this.generator);
    }

    public static WorldGenSettings read(Properties param0) {
        String var0 = MoreObjects.firstNonNull((String)param0.get("generator-settings"), "");
        param0.put("generator-settings", var0);
        String var1 = MoreObjects.firstNonNull((String)param0.get("level-seed"), "");
        param0.put("level-seed", var1);
        String var2 = (String)param0.get("generate-structures");
        boolean var3 = var2 == null || Boolean.parseBoolean(var2);
        param0.put("generate-structures", Objects.toString(var3));
        String var4 = (String)param0.get("level-type");
        WorldGenSettings.LevelType var5;
        if (var4 != null) {
            var5 = MoreObjects.firstNonNull(WorldGenSettings.LevelType.byName(var4), WorldGenSettings.LevelType.NORMAL);
        } else {
            var5 = WorldGenSettings.LevelType.NORMAL;
        }

        param0.put("level-type", var5.name);
        JsonObject var7 = !var0.isEmpty() ? GsonHelper.parse(var0) : new JsonObject();
        long var8 = new Random().nextLong();
        if (!var1.isEmpty()) {
            try {
                long var9 = Long.parseLong(var1);
                if (var9 != 0L) {
                    var8 = var9;
                }
            } catch (NumberFormatException var12) {
                var8 = (long)var1.hashCode();
            }
        }

        Dynamic<?> var11 = new Dynamic<>(JsonOps.INSTANCE, var7);
        return new WorldGenSettings(var8, var3, false, var5, var11, make(var5, var11, var8));
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withPreset(WorldGenSettings.Preset param0) {
        return this.withProvider(param0.type, EMPTY_SETTINGS, make(param0.type, EMPTY_SETTINGS, this.seed));
    }

    @OnlyIn(Dist.CLIENT)
    private WorldGenSettings withProvider(WorldGenSettings.LevelType param0, Dynamic<?> param1, ChunkGenerator param2) {
        return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings fromFlatSettings(FlatLevelGeneratorSettings param0) {
        return this.withProvider(WorldGenSettings.LevelType.FLAT, param0.toObject(NbtOps.INSTANCE), new FlatLevelSource(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings fromBuffetSettings(WorldGenSettings.BuffetGeneratorType param0, Set<Biome> param1) {
        Dynamic<?> var0 = createBuffetSettings(param0, param1);
        return this.withProvider(WorldGenSettings.LevelType.BUFFET, var0, make(WorldGenSettings.LevelType.BUFFET, var0, this.seed));
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings.Preset preset() {
        return this.type == WorldGenSettings.LevelType.CUSTOMIZED
            ? WorldGenSettings.Preset.NORMAL
            : PRESETS.getOrDefault(this.type, WorldGenSettings.Preset.NORMAL);
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withSeed(boolean param0, OptionalLong param1) {
        long var0 = param1.orElse(this.seed);
        ChunkGenerator var1 = param1.isPresent() ? this.generator.withSeed(param1.getAsLong()) : this.generator;
        WorldGenSettings var2;
        if (this.isDebug()) {
            var2 = new WorldGenSettings(var0, false, false, this.type, this.settings, var1);
        } else {
            var2 = new WorldGenSettings(var0, this.generateFeatures(), this.generateBonusChest() && !param0, this.type, this.settings, var1);
        }

        return var2;
    }

    private static ChunkGenerator make(WorldGenSettings.LevelType param0, Dynamic<?> param1, long param2) {
        if (param0 == WorldGenSettings.LevelType.BUFFET) {
            BiomeSource var0 = createBuffetBiomeSource(param1.get("biome_source"), param2);
            DynamicLike<?> var1 = param1.get("chunk_generator");
            WorldGenSettings.BuffetGeneratorType var2 = DataFixUtils.orElse(
                var1.get("type").asString().flatMap(param0x -> Optional.ofNullable(WorldGenSettings.BuffetGeneratorType.byName(param0x))),
                WorldGenSettings.BuffetGeneratorType.SURFACE
            );
            DynamicLike<?> var3 = var1.get("options");
            BlockState var4 = getRegistryValue(var3.get("default_block"), Registry.BLOCK, Blocks.STONE).defaultBlockState();
            BlockState var5 = getRegistryValue(var3.get("default_fluid"), Registry.BLOCK, Blocks.WATER).defaultBlockState();
            switch(var2) {
                case CAVES:
                    NetherGeneratorSettings var6 = new NetherGeneratorSettings(new ChunkGeneratorSettings());
                    var6.setDefaultBlock(var4);
                    var6.setDefaultFluid(var5);
                    return new NetherLevelSource(var0, param2, var6);
                case FLOATING_ISLANDS:
                    NoiseGeneratorSettings var7 = new NoiseGeneratorSettings(new ChunkGeneratorSettings());
                    var7.setDefaultBlock(var4);
                    var7.setDefaultFluid(var5);
                    return new TheEndLevelSource(var0, param2, var7);
                case SURFACE:
                default:
                    OverworldGeneratorSettings var8 = new OverworldGeneratorSettings();
                    var8.setDefaultBlock(var4);
                    var8.setDefaultFluid(var5);
                    return new OverworldLevelSource(var0, param2, var8);
            }
        } else if (param0 == WorldGenSettings.LevelType.FLAT) {
            FlatLevelGeneratorSettings var9 = FlatLevelGeneratorSettings.fromObject(param1);
            return new FlatLevelSource(var9);
        } else if (param0 == WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES) {
            return DebugLevelSource.INSTANCE;
        } else {
            boolean var10 = param0 == WorldGenSettings.LevelType.NORMAL_1_1;
            int var11 = param0 == WorldGenSettings.LevelType.LARGE_BIOMES ? 6 : 4;
            boolean var12 = param0 == WorldGenSettings.LevelType.AMPLIFIED;
            OverworldGeneratorSettings var13 = new OverworldGeneratorSettings(new ChunkGeneratorSettings(), var12);
            return new OverworldLevelSource(new OverworldBiomeSource(param2, var10, var11), param2, var13);
        }
    }

    private static <T> T getRegistryValue(DynamicLike<?> param0, Registry<T> param1, T param2) {
        return param0.asString().map(ResourceLocation::new).flatMap(param1::getOptional).orElse(param2);
    }

    private static BiomeSource createBuffetBiomeSource(DynamicLike<?> param0, long param1) {
        BiomeSourceType var0 = getRegistryValue(param0.get("type"), Registry.BIOME_SOURCE_TYPE, BiomeSourceType.FIXED);
        DynamicLike<?> var1 = param0.get("options");
        Stream<Biome> var2 = var1.get("biomes")
            .asStreamOpt()
            .map(param0x -> param0x.map(param0xx -> getRegistryValue(param0xx, Registry.BIOME, Biomes.OCEAN)))
            .orElseGet(Stream::empty);
        if (BiomeSourceType.CHECKERBOARD == var0) {
            int var3 = var1.get("size").asInt(2);
            Biome[] var4 = var2.toArray(param0x -> new Biome[param0x]);
            Biome[] var5 = var4.length > 0 ? var4 : new Biome[]{Biomes.OCEAN};
            return new CheckerboardColumnBiomeSource(var5, var3);
        } else if (BiomeSourceType.VANILLA_LAYERED == var0) {
            return new OverworldBiomeSource(param1, false, 4);
        } else {
            Biome var6 = var2.findFirst().orElse(Biomes.OCEAN);
            return new FixedBiomeSource(var6);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static Dynamic<?> createBuffetSettings(WorldGenSettings.BuffetGeneratorType param0, Set<Biome> param1) {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();
        var1.putString("type", Registry.BIOME_SOURCE_TYPE.getKey(BiomeSourceType.FIXED).toString());
        CompoundTag var2 = new CompoundTag();
        ListTag var3 = new ListTag();

        for(Biome var4 : param1) {
            var3.add(StringTag.valueOf(Registry.BIOME.getKey(var4).toString()));
        }

        var2.put("biomes", var3);
        var1.put("options", var2);
        CompoundTag var5 = new CompoundTag();
        CompoundTag var6 = new CompoundTag();
        var5.putString("type", param0.getName());
        var6.putString("default_block", "minecraft:stone");
        var6.putString("default_fluid", "minecraft:water");
        var5.put("options", var6);
        var0.put("biome_source", var1);
        var0.put("chunk_generator", var5);
        return new Dynamic<>(NbtOps.INSTANCE, var0);
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings parseFlatSettings() {
        return this.type == WorldGenSettings.LevelType.FLAT ? FlatLevelGeneratorSettings.fromObject(this.settings) : FlatLevelGeneratorSettings.getDefault();
    }

    @OnlyIn(Dist.CLIENT)
    public Pair<WorldGenSettings.BuffetGeneratorType, Set<Biome>> parseBuffetSettings() {
        if (this.type != WorldGenSettings.LevelType.BUFFET) {
            return Pair.of(WorldGenSettings.BuffetGeneratorType.SURFACE, ImmutableSet.of());
        } else {
            WorldGenSettings.BuffetGeneratorType var0 = WorldGenSettings.BuffetGeneratorType.SURFACE;
            Set<Biome> var1 = Sets.newHashSet();
            CompoundTag var2 = (CompoundTag)this.settings.convert(NbtOps.INSTANCE).getValue();
            if (var2.contains("chunk_generator", 10) && var2.getCompound("chunk_generator").contains("type", 8)) {
                String var3 = var2.getCompound("chunk_generator").getString("type");
                var0 = WorldGenSettings.BuffetGeneratorType.byName(var3);
            }

            if (var2.contains("biome_source", 10) && var2.getCompound("biome_source").contains("biomes", 9)) {
                ListTag var4 = var2.getCompound("biome_source").getList("biomes", 8);

                for(int var5 = 0; var5 < var4.size(); ++var5) {
                    ResourceLocation var6 = new ResourceLocation(var4.getString(var5));
                    Biome var7 = Registry.BIOME.get(var6);
                    var1.add(var7);
                }
            }

            return Pair.of(var0, var1);
        }
    }

    public static enum BuffetGeneratorType {
        SURFACE("minecraft:surface"),
        CAVES("minecraft:caves"),
        FLOATING_ISLANDS("minecraft:floating_islands");

        private static final Map<String, WorldGenSettings.BuffetGeneratorType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(WorldGenSettings.BuffetGeneratorType::getName, Function.identity()));
        private final String name;

        private BuffetGeneratorType(String param0) {
            this.name = param0;
        }

        @OnlyIn(Dist.CLIENT)
        public Component createGeneratorString() {
            return new TranslatableComponent("createWorld.customize.buffet.generatortype")
                .append(" ")
                .append(new TranslatableComponent(Util.makeDescriptionId("generator", new ResourceLocation(this.name))));
        }

        private String getName() {
            return this.name;
        }

        @Nullable
        public static WorldGenSettings.BuffetGeneratorType byName(String param0) {
            return BY_NAME.get(param0);
        }
    }

    static class LevelType {
        private static final Set<WorldGenSettings.LevelType> TYPES = Sets.newHashSet();
        public static final WorldGenSettings.LevelType NORMAL = new WorldGenSettings.LevelType("default");
        public static final WorldGenSettings.LevelType FLAT = new WorldGenSettings.LevelType("flat");
        public static final WorldGenSettings.LevelType LARGE_BIOMES = new WorldGenSettings.LevelType("largeBiomes");
        public static final WorldGenSettings.LevelType AMPLIFIED = new WorldGenSettings.LevelType("amplified");
        public static final WorldGenSettings.LevelType BUFFET = new WorldGenSettings.LevelType("buffet");
        public static final WorldGenSettings.LevelType DEBUG_ALL_BLOCK_STATES = new WorldGenSettings.LevelType("debug_all_block_states");
        public static final WorldGenSettings.LevelType CUSTOMIZED = new WorldGenSettings.LevelType("customized");
        public static final WorldGenSettings.LevelType NORMAL_1_1 = new WorldGenSettings.LevelType("default_1_1");
        private final String name;

        private LevelType(String param0) {
            this.name = param0;
            TYPES.add(this);
        }

        @Nullable
        public static WorldGenSettings.LevelType byName(String param0) {
            for(WorldGenSettings.LevelType var0 : TYPES) {
                if (var0.name.equalsIgnoreCase(param0)) {
                    return var0;
                }
            }

            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Preset {
        public static final WorldGenSettings.Preset NORMAL = new WorldGenSettings.Preset(WorldGenSettings.LevelType.NORMAL);
        public static final WorldGenSettings.Preset FLAT = new WorldGenSettings.Preset(WorldGenSettings.LevelType.FLAT);
        public static final WorldGenSettings.Preset AMPLIFIED = new WorldGenSettings.Preset(WorldGenSettings.LevelType.AMPLIFIED);
        public static final WorldGenSettings.Preset BUFFET = new WorldGenSettings.Preset(WorldGenSettings.LevelType.BUFFET);
        public static final List<WorldGenSettings.Preset> PRESETS = Lists.newArrayList(
            NORMAL,
            FLAT,
            new WorldGenSettings.Preset(WorldGenSettings.LevelType.LARGE_BIOMES),
            AMPLIFIED,
            BUFFET,
            new WorldGenSettings.Preset(WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES)
        );
        private final WorldGenSettings.LevelType type;
        private final Component description;

        private Preset(WorldGenSettings.LevelType param0) {
            this.type = param0;
            WorldGenSettings.PRESETS.put(param0, this);
            this.description = new TranslatableComponent("generator." + param0.name);
        }

        public Component description() {
            return this.description;
        }
    }
}
