package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class RegistriesDatapackGenerator implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public RegistriesDatapackGenerator(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        this.registries = param1;
        this.output = param0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        return this.registries
            .thenCompose(
                param1 -> {
                    DynamicOps<JsonElement> var0 = RegistryOps.create(JsonOps.INSTANCE, param1);
                    return CompletableFuture.allOf(
                        RegistryDataLoader.WORLDGEN_REGISTRIES
                            .stream()
                            .flatMap(param3 -> this.dumpRegistryCap(param0, param1, var0, param3).stream())
                            .toArray(param0x -> new CompletableFuture[param0x])
                    );
                }
            );
    }

    private <T> Optional<CompletableFuture<?>> dumpRegistryCap(
        CachedOutput param0, HolderLookup.Provider param1, DynamicOps<JsonElement> param2, RegistryDataLoader.RegistryData<T> param3
    ) {
        ResourceKey<? extends Registry<T>> var0 = param3.key();
        return param1.lookup(var0)
            .map(
                param4 -> {
                    PackOutput.PathProvider var0x = this.output.createPathProvider(PackOutput.Target.DATA_PACK, var0.location().getPath());
                    return CompletableFuture.allOf(
                        param4.listElements()
                            .map(param4x -> dumpValue(var0x.json(param4x.key().location()), param0, param2, param3.elementCodec(), (T)param4x.value()))
                            .toArray(param0x -> new CompletableFuture[param0x])
                    );
                }
            );
    }

    private static <E> CompletableFuture<?> dumpValue(Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        Optional<JsonElement> var0 = param3.encodeStart(param2, param4)
            .resultOrPartial(param1x -> LOGGER.error("Couldn't serialize element {}: {}", param0, param1x));
        return var0.isPresent() ? DataProvider.saveStable(param1, var0.get(), param0) : CompletableFuture.completedFuture(null);
    }

    @Override
    public final String getName() {
        return "Registries";
    }
}
