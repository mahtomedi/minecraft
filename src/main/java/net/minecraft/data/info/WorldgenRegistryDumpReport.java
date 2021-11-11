package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldgenRegistryDumpReport implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public WorldgenRegistryDumpReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) {
        Path var0 = this.generator.getOutputFolder();
        RegistryAccess var1 = RegistryAccess.builtin();
        int var2 = 0;
        MappedRegistry<LevelStem> var3 = DimensionType.defaultDimensions(var1, 0L, false);
        ChunkGenerator var4 = WorldGenSettings.makeDefaultOverworld(var1, 0L, false);
        MappedRegistry<LevelStem> var5 = WorldGenSettings.withOverworld(var1.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), var3, var4);
        DynamicOps<JsonElement> var6 = RegistryWriteOps.create(JsonOps.INSTANCE, var1);
        RegistryAccess.knownRegistries().forEach(param4 -> dumpRegistryCap(param0, var0, var1, var6, param4));
        dumpRegistry(var0, param0, var6, Registry.LEVEL_STEM_REGISTRY, var5, LevelStem.CODEC);
    }

    private static <T> void dumpRegistryCap(
        HashCache param0, Path param1, RegistryAccess param2, DynamicOps<JsonElement> param3, RegistryAccess.RegistryData<T> param4
    ) {
        dumpRegistry(param1, param0, param3, param4.key(), param2.ownedRegistryOrThrow(param4.key()), param4.codec());
    }

    private static <E, T extends Registry<E>> void dumpRegistry(
        Path param0, HashCache param1, DynamicOps<JsonElement> param2, ResourceKey<? extends T> param3, T param4, Encoder<E> param5
    ) {
        for(Entry<ResourceKey<E>, E> var0 : param4.entrySet()) {
            Path var1 = createPath(param0, param3.location(), var0.getKey().location());
            dumpValue(var1, param1, param2, param5, var0.getValue());
        }

    }

    private static <E> void dumpValue(Path param0, HashCache param1, DynamicOps<JsonElement> param2, Encoder<E> param3, E param4) {
        try {
            Optional<JsonElement> var0 = param3.encodeStart(param2, param4).result();
            if (var0.isPresent()) {
                DataProvider.save(GSON, param1, var0.get(), param0);
            } else {
                LOGGER.error("Couldn't serialize element {}", param0);
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
