package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class BuiltinRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Supplier<? extends Holder<?>>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(
        ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental(), null
    );
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
    public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> Carvers.CAVE);
    public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
    public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(
        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeatures::bootstrap
    );
    public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
    public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () -> ProcessorLists.ZOMBIE_PLAINS);
    public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
    public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, Biomes::bootstrap);
    public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap
    );
    public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);
    public static final RegistryAccess ACCESS = RegistryAccess.fromRegistryOfRegistries(REGISTRY);

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> param0, Supplier<? extends Holder<? extends T>> param1) {
        return registerSimple(param0, Lifecycle.stable(), param1);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Supplier<? extends Holder<? extends T>> param2) {
        return internalRegister(param0, new MappedRegistry<>(param0, param1, null), param2, param1);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(
        ResourceKey<? extends Registry<T>> param0, R param1, Supplier<? extends Holder<? extends T>> param2, Lifecycle param3
    ) {
        ResourceLocation var0 = param0.location();
        LOADERS.put(var0, param2);
        WRITABLE_REGISTRY.register(param0, param1, param3);
        return param1;
    }

    public static <V extends T, T> Holder<V> registerExact(Registry<T> param0, String param1, V param2) {
        return register(param0, new ResourceLocation(param1), (T)param2);
    }

    public static <T> Holder<T> register(Registry<T> param0, String param1, T param2) {
        return register(param0, new ResourceLocation(param1), param2);
    }

    public static <T> Holder<T> register(Registry<T> param0, ResourceLocation param1, T param2) {
        return register(param0, ResourceKey.create(param0.key(), param1), param2);
    }

    public static <T> Holder<T> register(Registry<T> param0, ResourceKey<T> param1, T param2) {
        return ((WritableRegistry)param0).register(param1, param2, Lifecycle.stable());
    }

    public static void bootstrap() {
    }

    static {
        LOADERS.forEach((param0, param1) -> {
            if (!param1.get().isBound()) {
                LOGGER.error("Unable to bootstrap registry '{}'", param0);
            }

        });
        Registry.checkRegistry(WRITABLE_REGISTRY);
    }
}
