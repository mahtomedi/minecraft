package net.minecraft.data.worldgen;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class BuiltinRegistriesDatapackGenerator implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;

    public BuiltinRegistriesDatapackGenerator(PackOutput param0) {
        this.output = param0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        RegistryAccess var0 = BuiltinRegistries.createAccess();
        DynamicOps<JsonElement> var1 = RegistryOps.create(JsonOps.INSTANCE, var0);
        return CompletableFuture.allOf(
            RegistryDataLoader.WORLDGEN_REGISTRIES
                .stream()
                .map(param3 -> this.dumpRegistryCap(param0, var0, var1, param3))
                .toArray(param0x -> new CompletableFuture[param0x])
        );
    }

    private <T> CompletableFuture<?> dumpRegistryCap(
        CachedOutput param0, RegistryAccess param1, DynamicOps<JsonElement> param2, RegistryDataLoader.RegistryData<T> param3
    ) {
        ResourceKey<? extends Registry<T>> var0 = param3.key();
        Registry<T> var1 = param1.registryOrThrow(var0);
        PackOutput.PathProvider var2 = this.output.createPathProvider(PackOutput.Target.DATA_PACK, var0.location().getPath());
        return CompletableFuture.allOf(
            var1.entrySet()
                .stream()
                .map(param4 -> dumpValue(var2.json(param4.getKey().location()), param0, param2, param3.elementCodec(), param4.getValue()))
                .toArray(param0x -> new CompletableFuture[param0x])
        );
    }

    private static <E> CompletableFuture<?> dumpValue(Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        Optional<JsonElement> var0 = param3.encodeStart(param2, param4)
            .resultOrPartial(param1x -> LOGGER.error("Couldn't serialize element {}: {}", param0, param1x));
        return var0.isPresent() ? DataProvider.saveStable(param1, var0.get(), param0) : CompletableFuture.completedFuture(null);
    }

    @Override
    public final String getName() {
        return "Worldgen";
    }
}
