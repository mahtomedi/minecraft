package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BiomeParametersDumpReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) {
        Path var0 = this.generator.getOutputFolder();
        RegistryAccess.Frozen var1 = RegistryAccess.BUILTIN.get();
        DynamicOps<JsonElement> var2 = RegistryOps.create(JsonOps.INSTANCE, var1);
        Registry<Biome> var3 = var1.registryOrThrow(Registry.BIOME_REGISTRY);
        MultiNoiseBiomeSource.Preset.getPresets().forEach(param4 -> {
            MultiNoiseBiomeSource var0x = param4.getSecond().biomeSource(var3, false);
            dumpValue(createPath(var0, param4.getFirst()), param0, var2, MultiNoiseBiomeSource.CODEC, var0x);
        });
    }

    private static <E> void dumpValue(Path param0, CachedOutput param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        try {
            Optional<JsonElement> var0 = param3.encodeStart(param2, param4)
                .resultOrPartial(param1x -> LOGGER.error("Couldn't serialize element {}: {}", param0, param1x));
            if (var0.isPresent()) {
                DataProvider.save(GSON, param1, var0.get(), param0);
            }
        } catch (IOException var6) {
            LOGGER.error("Couldn't save element {}", param0, var6);
        }

    }

    private static Path createPath(Path param0, ResourceLocation param1) {
        return resolveTopPath(param0).resolve(param1.getNamespace()).resolve(param1.getPath() + ".json");
    }

    private static Path resolveTopPath(Path param0) {
        return param0.resolve("reports").resolve("biome_parameters");
    }

    @Override
    public String getName() {
        return "Biome Parameters";
    }
}
