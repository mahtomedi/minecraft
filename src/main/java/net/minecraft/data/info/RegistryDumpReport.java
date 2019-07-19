package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public RegistryDumpReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) throws IOException {
        JsonObject var0 = new JsonObject();
        Registry.REGISTRY.keySet().forEach(param1 -> var0.add(param1.toString(), dumpRegistry(Registry.REGISTRY.get(param1))));
        Path var1 = this.generator.getOutputFolder().resolve("reports/registries.json");
        DataProvider.save(GSON, param0, var0, var1);
    }

    private static <T> JsonElement dumpRegistry(WritableRegistry<T> param0) {
        JsonObject var0 = new JsonObject();
        if (param0 instanceof DefaultedRegistry) {
            ResourceLocation var1 = ((DefaultedRegistry)param0).getDefaultKey();
            var0.addProperty("default", var1.toString());
        }

        int var2 = Registry.REGISTRY.getId(param0);
        var0.addProperty("protocol_id", var2);
        JsonObject var3 = new JsonObject();

        for(ResourceLocation var4 : param0.keySet()) {
            T var5 = param0.get(var4);
            int var6 = param0.getId(var5);
            JsonObject var7 = new JsonObject();
            var7.addProperty("protocol_id", var6);
            var3.add(var4.toString(), var7);
        }

        var0.add("entries", var3);
        return var0;
    }

    @Override
    public String getName() {
        return "Registry Dump";
    }
}
