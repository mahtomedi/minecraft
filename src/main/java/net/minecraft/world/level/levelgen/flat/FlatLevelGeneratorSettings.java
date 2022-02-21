package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class FlatLevelGeneratorSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
            param0 -> param0.group(
                        RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(param0x -> param0x.biomes),
                        RegistryCodecs.homogeneousList(Registry.STRUCTURE_SET_REGISTRY)
                            .optionalFieldOf("structure_overrides")
                            .forGetter(param0x -> param0x.structureOverrides),
                        FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                        Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(param0x -> param0x.addLakes),
                        Codec.BOOL.fieldOf("features").orElse(false).forGetter(param0x -> param0x.decoration),
                        Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(param0x -> Optional.of(param0x.biome))
                    )
                    .apply(param0, FlatLevelGeneratorSettings::new)
        )
        .comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity())
        .stable();
    private final Registry<Biome> biomes;
    private final Optional<HolderSet<StructureSet>> structureOverrides;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Holder<Biome> biome;
    private final List<BlockState> layers;
    private boolean voidGen;
    private boolean decoration;
    private boolean addLakes;

    private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings param0) {
        int var0 = param0.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
        return var0 > DimensionType.Y_SIZE ? DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE, param0) : DataResult.success(param0);
    }

    private FlatLevelGeneratorSettings(
        Registry<Biome> param0,
        Optional<HolderSet<StructureSet>> param1,
        List<FlatLayerInfo> param2,
        boolean param3,
        boolean param4,
        Optional<Holder<Biome>> param5
    ) {
        this(param1, param0);
        if (param3) {
            this.setAddLakes();
        }

        if (param4) {
            this.setDecoration();
        }

        this.layersInfo.addAll(param2);
        this.updateLayers();
        if (param5.isEmpty()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            this.biome = param0.getOrCreateHolder(Biomes.PLAINS);
        } else {
            this.biome = param5.get();
        }

    }

    public FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> param0, Registry<Biome> param1) {
        this.biomes = param1;
        this.structureOverrides = param0;
        this.biome = param1.getOrCreateHolder(Biomes.PLAINS);
        this.layers = Lists.newArrayList();
    }

    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> param0, Optional<HolderSet<StructureSet>> param1) {
        FlatLevelGeneratorSettings var0 = new FlatLevelGeneratorSettings(param1, this.biomes);

        for(FlatLayerInfo var1 : param0) {
            var0.layersInfo.add(new FlatLayerInfo(var1.getHeight(), var1.getBlockState().getBlock()));
            var0.updateLayers();
        }

        var0.setBiome(this.biome);
        if (this.decoration) {
            var0.setDecoration();
        }

        if (this.addLakes) {
            var0.setAddLakes();
        }

        return var0;
    }

    public void setDecoration() {
        this.decoration = true;
    }

    public void setAddLakes() {
        this.addLakes = true;
    }

    public Holder<Biome> getBiomeFromSettings() {
        Biome var0 = this.getBiome().value();
        BiomeGenerationSettings var1 = var0.getGenerationSettings();
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder();
        if (this.addLakes) {
            var2.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
            var2.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
        }

        boolean var3 = (!this.voidGen || this.biome.is(Biomes.THE_VOID)) && this.decoration;
        if (var3) {
            List<HolderSet<PlacedFeature>> var4 = var1.features();

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                if (var5 != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && var5 != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
                    for(Holder<PlacedFeature> var7 : var4.get(var5)) {
                        var2.addFeature(var5, var7);
                    }
                }
            }
        }

        List<BlockState> var8 = this.getLayers();

        for(int var9 = 0; var9 < var8.size(); ++var9) {
            BlockState var10 = var8.get(var9);
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var10)) {
                var8.set(var9, null);
                var2.addFeature(
                    GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(var9, var10))
                );
            }
        }

        return Holder.direct(Biome.BiomeBuilder.from(var0).generationSettings(var2.build()).build());
    }

    public Optional<HolderSet<StructureSet>> structureOverrides() {
        return this.structureOverrides;
    }

    public Holder<Biome> getBiome() {
        return this.biome;
    }

    public void setBiome(Holder<Biome> param0) {
        this.biome = param0;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public List<BlockState> getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        this.layers.clear();

        for(FlatLayerInfo var0 : this.layersInfo) {
            for(int var1 = 0; var1 < var0.getHeight(); ++var1) {
                this.layers.add(var0.getBlockState());
            }
        }

        this.voidGen = this.layers.stream().allMatch(param0 -> param0.is(Blocks.AIR));
    }

    public static FlatLevelGeneratorSettings getDefault(Registry<Biome> param0, Registry<StructureSet> param1) {
        HolderSet<StructureSet> var0 = HolderSet.direct(
            param1.getHolderOrThrow(BuiltinStructureSets.STRONGHOLDS), param1.getHolderOrThrow(BuiltinStructureSets.VILLAGES)
        );
        FlatLevelGeneratorSettings var1 = new FlatLevelGeneratorSettings(Optional.of(var0), param0);
        var1.biome = param0.getOrCreateHolder(Biomes.PLAINS);
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        var1.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        var1.updateLayers();
        return var1;
    }
}
