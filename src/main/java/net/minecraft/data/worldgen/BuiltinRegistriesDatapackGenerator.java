package net.minecraft.data.worldgen;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
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
    public void run(CachedOutput param0) {
        RegistryAccess var0 = BuiltinRegistries.createAccess();
        DynamicOps<JsonElement> var1 = RegistryOps.create(JsonOps.INSTANCE, var0);
        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(param3 -> this.dumpRegistryCap(param0, var0, var1, param3));
    }

    private <T> void dumpRegistryCap(CachedOutput param0, RegistryAccess param1, DynamicOps<JsonElement> param2, RegistryDataLoader.RegistryData<T> param3) {
        ResourceKey<? extends Registry<T>> var0 = param3.key();
        Registry<T> var1 = param1.registryOrThrow(var0);
        PackOutput.PathProvider var2 = this.output.createPathProvider(PackOutput.Target.DATA_PACK, var0.location().getPath());

        for(Entry<ResourceKey<T>, T> var3 : var1.entrySet()) {
            dumpValue(var2.json(var3.getKey().location()), param0, param2, param3.elementCodec(), var3.getValue());
        }

    }

    private static <E> void dumpValue(Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        try {
            Optional<JsonElement> var0 = param3.encodeStart(param2, param4)
                .resultOrPartial(param1x -> LOGGER.error("Couldn't serialize element {}: {}", param0, param1x));
            if (var0.isPresent()) {
                DataProvider.saveStable(param1, var0.get(), param0);
            }
        } catch (IOException var6) {
            LOGGER.error("Couldn't save element {}", param0, var6);
        }

    }

    @Override
    public String getName() {
        return "Worldgen";
    }
}
