package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
    private final PackOutput output;

    public RegistryDumpReport(PackOutput param0) {
        this.output = param0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        JsonObject var0 = new JsonObject();
        BuiltInRegistries.REGISTRY.holders().forEach(param1 -> var0.add(param1.key().location().toString(), dumpRegistry(param1.value())));
        Path var1 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
        return DataProvider.saveStable(param0, var0, var1);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> param0) {
        JsonObject var0 = new JsonObject();
        if (param0 instanceof DefaultedRegistry) {
            ResourceLocation var1 = ((DefaultedRegistry)param0).getDefaultKey();
            var0.addProperty("default", var1.toString());
        }

        int var2 = BuiltInRegistries.REGISTRY.getId(param0);
        var0.addProperty("protocol_id", var2);
        JsonObject var3 = new JsonObject();
        param0.holders().forEach(param2 -> {
            T var0x = param2.value();
            int var1x = param0.getId(var0x);
            JsonObject var2x = new JsonObject();
            var2x.addProperty("protocol_id", var1x);
            var3.add(param2.key().location().toString(), var2x);
        });
        var0.add("entries", var3);
        return var0;
    }

    @Override
    public final String getName() {
        return "Registry Dump";
    }
}
