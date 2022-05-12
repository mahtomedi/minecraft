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
import org.slf4j.Logger;

public class WorldgenRegistryDumpReport implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;

    public WorldgenRegistryDumpReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) {
        RegistryAccess var0 = RegistryAccess.BUILTIN.get();
        DynamicOps<JsonElement> var1 = RegistryOps.create(JsonOps.INSTANCE, var0);
        RegistryAccess.knownRegistries().forEach(param3 -> this.dumpRegistryCap(param0, var0, var1, param3));
    }

    private <T> void dumpRegistryCap(CachedOutput param0, RegistryAccess param1, DynamicOps<JsonElement> param2, RegistryAccess.RegistryData<T> param3) {
        ResourceKey<? extends Registry<T>> var0 = param3.key();
        Registry<T> var1 = param1.ownedRegistryOrThrow(var0);
        DataGenerator.PathProvider var2 = this.generator.createPathProvider(DataGenerator.Target.REPORTS, var0.location().getPath());

        for(Entry<ResourceKey<T>, T> var3 : var1.entrySet()) {
            dumpValue(var2.json(var3.getKey().location()), param0, param2, param3.codec(), var3.getValue());
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
