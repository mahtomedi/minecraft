package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class AdvancementProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AdvancementProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1, List<AdvancementSubProvider> param2) {
        this.pathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
        this.subProviders = param2;
        this.registries = param1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        return this.registries.thenCompose(param1 -> {
            Set<ResourceLocation> var0 = new HashSet<>();
            List<CompletableFuture<?>> var1x = new ArrayList();
            Consumer<AdvancementHolder> var2 = param3 -> {
                if (!var0.add(param3.id())) {
                    throw new IllegalStateException("Duplicate advancement " + param3.id());
                } else {
                    Path var0x = this.pathProvider.json(param3.id());
                    var1x.add(DataProvider.saveStable(param0, Advancement.CODEC, param3.value(), var0x));
                }
            };

            for(AdvancementSubProvider var3 : this.subProviders) {
                var3.generate(param1, var2);
            }

            return CompletableFuture.allOf(var1x.toArray(param0x -> new CompletableFuture[param0x]));
        });
    }

    @Override
    public final String getName() {
        return "Advancements";
    }
}
