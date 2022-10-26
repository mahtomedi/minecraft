package net.minecraft.data.info;

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
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path topPath;

    public BiomeParametersDumpReport(PackOutput param0) {
        this.topPath = param0.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        RegistryAccess var0 = BuiltinRegistries.createAccess();
        DynamicOps<JsonElement> var1 = RegistryOps.create(JsonOps.INSTANCE, var0);
        Registry<Biome> var2 = var0.registryOrThrow(Registry.BIOME_REGISTRY);
        return CompletableFuture.allOf(MultiNoiseBiomeSource.Preset.getPresets().map(param3 -> {
            MultiNoiseBiomeSource var0x = param3.getSecond().biomeSource(var2, false);
            return dumpValue(this.createPath(param3.getFirst()), param0, var1, MultiNoiseBiomeSource.CODEC, var0x);
        }).toArray(param0x -> new CompletableFuture[param0x]));
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
