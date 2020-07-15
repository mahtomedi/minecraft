package net.minecraft.data.worldgen.biome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeReport implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BiomeReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) {
        Path var0 = this.generator.getOutputFolder();

        for(Entry<ResourceKey<Biome>, Biome> var1 : BuiltinRegistries.BIOME.entrySet()) {
            Path var2 = createPath(var0, var1.getKey().location());
            Biome var3 = var1.getValue();
            Function<Supplier<Biome>, DataResult<JsonElement>> var4 = JsonOps.INSTANCE.withEncoder(Biome.CODEC);

            try {
                Optional<JsonElement> var5 = var4.apply(() -> var3).result();
                if (var5.isPresent()) {
                    DataProvider.save(GSON, param0, var5.get(), var2);
                } else {
                    LOGGER.error("Couldn't serialize biome {}", var2);
                }
            } catch (IOException var9) {
                LOGGER.error("Couldn't save biome {}", var2, var9);
            }
        }

    }

    private static Path createPath(Path param0, ResourceLocation param1) {
        return param0.resolve("reports/biomes/" + param1.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Biomes";
    }
}
