package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuiltinRegistries {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(
        ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental()
    );
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
    public static final Registry<ConfiguredSurfaceBuilder<?>> CONFIGURED_SURFACE_BUILDER = registerSimple(
        Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, () -> SurfaceBuilders.NOPE
    );
    public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> Carvers.CAVE);
    public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, () -> Features.NOPE);
    public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(
        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, () -> StructureFeatures.MINESHAFT
    );
    public static final Registry<ImmutableList<StructureProcessor>> PROCESSOR_LIST = registerSimple(
        Registry.PROCESSOR_LIST_REGISTRY, () -> ProcessorLists.ZOMBIE_PLAINS
    );
    public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, () -> Pools.EMPTY);
    public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, () -> Biomes.DEFAULT);

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> param0, Supplier<T> param1) {
        return registerSimple(param0, Lifecycle.experimental(), param1);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Supplier<T> param2) {
        return internalRegister(param0, new MappedRegistry<>(param0, param1), param2);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> param0, R param1, Supplier<T> param2) {
        ResourceLocation var0 = param0.location();
        LOADERS.put(var0, param2);
        WritableRegistry<R> var1 = WRITABLE_REGISTRY;
        return var1.register(param0, param1);
    }

    public static <T> T register(Registry<? super T> param0, String param1, T param2) {
        return register(param0, new ResourceLocation(param1), param2);
    }

    public static <V, T extends V> T register(Registry<V> param0, ResourceLocation param1, T param2) {
        return ((WritableRegistry)param0).register(ResourceKey.create(param0.key(), param1), param2);
    }

    public static <V, T extends V> T registerMapping(Registry<V> param0, int param1, String param2, T param3) {
        return ((WritableRegistry)param0).registerMapping(param1, ResourceKey.create(param0.key(), new ResourceLocation(param2)), param3);
    }

    public static void bootstrap() {
    }

    static {
        LOADERS.forEach((param0, param1) -> {
            if (param1.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", param0);
            }

        });
        Registry.checkRegistry(WRITABLE_REGISTRY);
    }
}
