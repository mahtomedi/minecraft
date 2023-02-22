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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
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
                        RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET)
                            .optionalFieldOf("structure_overrides")
                            .forGetter(param0x -> param0x.structureOverrides),
                        FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                        Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(param0x -> param0x.addLakes),
                        Codec.BOOL.fieldOf("features").orElse(false).forGetter(param0x -> param0x.decoration),
                        Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(param0x -> Optional.of(param0x.biome)),
                        RegistryOps.retrieveElement(Biomes.PLAINS),
                        RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND),
                        RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_SURFACE)
                    )
                    .apply(param0, FlatLevelGeneratorSettings::new)
        )
        .comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity())
        .stable();
    private final Optional<HolderSet<StructureSet>> structureOverrides;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private final Holder<Biome> biome;
    private final List<BlockState> layers;
    private boolean voidGen;
    private boolean decoration;
    private boolean addLakes;
    private final List<Holder<PlacedFeature>> lakes;

    private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings param0) {
        int var0 = param0.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
        return var0 > DimensionType.Y_SIZE ? DataResult.error(() -> "Sum of layer heights is > " + DimensionType.Y_SIZE, param0) : DataResult.success(param0);
    }

    private FlatLevelGeneratorSettings(
        Optional<HolderSet<StructureSet>> param0,
        List<FlatLayerInfo> param1,
        boolean param2,
        boolean param3,
        Optional<Holder<Biome>> param4,
        Holder.Reference<Biome> param5,
        Holder<PlacedFeature> param6,
        Holder<PlacedFeature> param7
    ) {
        this(param0, getBiome(param4, param5), List.of(param6, param7));
        if (param2) {
            this.setAddLakes();
        }

        if (param3) {
            this.setDecoration();
        }

        this.layersInfo.addAll(param1);
        this.updateLayers();
    }

    private static Holder<Biome> getBiome(Optional<? extends Holder<Biome>> param0, Holder<Biome> param1) {
        if (param0.isEmpty()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            return param1;
        } else {
            return param0.get();
        }
    }

    public FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> param0, Holder<Biome> param1, List<Holder<PlacedFeature>> param2) {
        this.structureOverrides = param0;
        this.biome = param1;
        this.layers = Lists.newArrayList();
        this.lakes = param2;
    }

    public FlatLevelGeneratorSettings withBiomeAndLayers(List<FlatLayerInfo> param0, Optional<HolderSet<StructureSet>> param1, Holder<Biome> param2) {
        FlatLevelGeneratorSettings var0 = new FlatLevelGeneratorSettings(param1, param2, this.lakes);

        for(FlatLayerInfo var1 : param0) {
            var0.layersInfo.add(new FlatLayerInfo(var1.getHeight(), var1.getBlockState().getBlock()));
            var0.updateLayers();
        }

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

    public BiomeGenerationSettings adjustGenerationSettings(Holder<Biome> param0) {
        if (!param0.equals(this.biome)) {
            return param0.value().getGenerationSettings();
        } else {
            BiomeGenerationSettings var0 = this.getBiome().value().getGenerationSettings();
            BiomeGenerationSettings.PlainBuilder var1 = new BiomeGenerationSettings.PlainBuilder();
            if (this.addLakes) {
                for(Holder<PlacedFeature> var2 : this.lakes) {
                    var1.addFeature(GenerationStep.Decoration.LAKES, var2);
                }
            }

            boolean var3 = (!this.voidGen || param0.is(Biomes.THE_VOID)) && this.decoration;
            if (var3) {
                List<HolderSet<PlacedFeature>> var4 = var0.features();

                for(int var5 = 0; var5 < var4.size(); ++var5) {
                    if (var5 != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal()
                        && var5 != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()
                        && (!this.addLakes || var5 != GenerationStep.Decoration.LAKES.ordinal())) {
                        for(Holder<PlacedFeature> var7 : var4.get(var5)) {
                            var1.addFeature(var5, var7);
                        }
                    }
                }
            }

            List<BlockState> var8 = this.getLayers();

            for(int var9 = 0; var9 < var8.size(); ++var9) {
                BlockState var10 = var8.get(var9);
                if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var10)) {
                    var8.set(var9, null);
                    var1.addFeature(
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(var9, var10))
                    );
                }
            }

            return var1.build();
        }
    }

    public Optional<HolderSet<StructureSet>> structureOverrides() {
        return this.structureOverrides;
    }

    public Holder<Biome> getBiome() {
        return this.biome;
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

    public static FlatLevelGeneratorSettings getDefault(HolderGetter<Biome> param0, HolderGetter<StructureSet> param1, HolderGetter<PlacedFeature> param2) {
        HolderSet<StructureSet> var0 = HolderSet.direct(param1.getOrThrow(BuiltinStructureSets.STRONGHOLDS), param1.getOrThrow(BuiltinStructureSets.VILLAGES));
        FlatLevelGeneratorSettings var1 = new FlatLevelGeneratorSettings(Optional.of(var0), getDefaultBiome(param0), createLakesList(param2));
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        var1.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        var1.updateLayers();
        return var1;
    }

    public static Holder<Biome> getDefaultBiome(HolderGetter<Biome> param0) {
        return param0.getOrThrow(Biomes.PLAINS);
    }

    public static List<Holder<PlacedFeature>> createLakesList(HolderGetter<PlacedFeature> param0) {
        return List.of(param0.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND), param0.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_SURFACE));
    }
}
