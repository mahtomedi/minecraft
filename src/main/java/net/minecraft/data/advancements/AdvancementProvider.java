package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class AdvancementProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders;

    public AdvancementProvider(PackOutput param0, List<AdvancementSubProvider> param1) {
        this.pathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
        this.subProviders = param1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        Set<ResourceLocation> var0 = new HashSet<>();
        List<CompletableFuture<?>> var1 = new ArrayList<>();
        Consumer<Advancement> var2 = param3 -> {
            if (!var0.add(param3.getId())) {
                throw new IllegalStateException("Duplicate advancement " + param3.getId());
            } else {
                Path var0x = this.pathProvider.json(param3.getId());
                var1.add(DataProvider.saveStable(param0, param3.deconstruct().serializeToJson(), var0x));
            }
        };

        for(AdvancementSubProvider var3 : this.subProviders) {
            var3.generate(var2);
        }

        return CompletableFuture.allOf(var1.toArray(param0x -> new CompletableFuture[param0x]));
    }

    @Override
    public final String getName() {
        return "Advancements";
    }
}
