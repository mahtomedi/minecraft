package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path topPath;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private static final MapCodec<ResourceKey<Biome>> ENTRY_CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("biome");
    private static final Codec<Climate.ParameterList<ResourceKey<Biome>>> CODEC = Climate.ParameterList.<ResourceKey<Biome>>codec(ENTRY_CODEC)
        .fieldOf("biomes")
        .codec();

    public BiomeParametersDumpReport(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        this.topPath = param0.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
        this.registries = param1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        return this.registries
            .thenCompose(
                param1 -> {
                    DynamicOps<JsonElement> var0 = RegistryOps.create(JsonOps.INSTANCE, param1);
                    List<CompletableFuture<?>> var1x = new ArrayList();
                    MultiNoiseBiomeSourceParameterList.knownPresets()
                        .forEach((param3, param4) -> var1x.add(dumpValue(this.createPath(param3.id()), param0, var0, CODEC, param4)));
                    return CompletableFuture.allOf((CompletableFuture<?>[])var1x.toArray(param0x -> new CompletableFuture[param0x]));
                }
            );
    }

    private static <E> CompletableFuture<?> dumpValue(Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        Optional<JsonElement> var0 = param3.encodeStart(param2, param4)
            .resultOrPartial(param1x -> LOGGER.error("Couldn't serialize element {}: {}", param0, param1x));
        return var0.isPresent() ? DataProvider.saveStable(param1, var0.get(), param0) : CompletableFuture.completedFuture(null);
    }

    private Path createPath(ResourceLocation param0) {
        return this.topPath.resolve(param0.getNamespace()).resolve(param0.getPath() + ".json");
    }

    @Override
    public final String getName() {
        return "Biome Parameters";
    }
}
