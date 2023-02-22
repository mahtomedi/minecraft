package net.minecraft.world.level.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MultiNoiseBiomeSourceParameterLists {
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER = register("nether");
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = register("overworld");

    public static void bootstrap(BootstapContext<MultiNoiseBiomeSourceParameterList> param0) {
        HolderGetter<Biome> var0 = param0.lookup(Registries.BIOME);
        param0.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, var0));
        param0.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, var0));
    }

    public static void nextUpdate(BootstapContext<MultiNoiseBiomeSourceParameterList> param0) {
        HolderGetter<Biome> var0 = param0.lookup(Registries.BIOME);
        param0.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD_UPDATE_1_20, var0));
    }

    private static ResourceKey<MultiNoiseBiomeSourceParameterList> register(String param0) {
        return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, new ResourceLocation(param0));
    }
}
