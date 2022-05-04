package net.minecraft.data.info;

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
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class WorldgenRegistryDumpReport implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;

    public WorldgenRegistryDumpReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) {
        Path var0 = this.generator.getOutputFolder();
        RegistryAccess var1 = RegistryAccess.BUILTIN.get();
        DynamicOps<JsonElement> var2 = RegistryOps.create(JsonOps.INSTANCE, var1);
        RegistryAccess.knownRegistries().forEach(param4 -> dumpRegistryCap(param0, var0, var1, var2, param4));
    }

    private static <T> void dumpRegistryCap(
        CachedOutput param0, Path param1, RegistryAccess param2, DynamicOps<JsonElement> param3, RegistryAccess.RegistryData<T> param4
    ) {
        dumpRegistry(param1, param0, param3, param4.key(), param2.ownedRegistryOrThrow(param4.key()), param4.codec());
    }

    private static <E, T extends Registry<E>> void dumpRegistry(
        Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, ResourceKey<? extends T> param3, T param4, Encoder<E> param5
    ) {
        for(Entry<ResourceKey<E>, E> var0 : param4.entrySet()) {
            Path var1 = createPath(param0, param3.location(), var0.getKey().location());
            dumpValue(var1, param1, param2, param5, var0.getValue());
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

    private static Path createPath(Path param0, ResourceLocation param1, ResourceLocation param2) {
        return resolveTopPath(param0).resolve(param2.getNamespace()).resolve(param1.getPath()).resolve(param2.getPath() + ".json");
    }

    private static Path resolveTopPath(Path param0) {
        return param0.resolve("reports").resolve("worldgen");
    }

    @Override
    public String getName() {
        return "Worldgen";
    }
}
