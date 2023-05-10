package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput.PathProvider pathProvider;
    private final Set<ResourceLocation> requiredTables;
    private final List<LootTableProvider.SubProviderEntry> subProviders;

    public LootTableProvider(PackOutput param0, Set<ResourceLocation> param1, List<LootTableProvider.SubProviderEntry> param2) {
        this.pathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
        this.subProviders = param2;
        this.requiredTables = param1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        final Map<ResourceLocation, LootTable> var0 = Maps.newHashMap();
        this.subProviders.forEach(param1 -> param1.provider().get().generate((param2, param3) -> {
                param3.setRandomSequence(param2);
                if (var0.put(param2, param3.setParamSet(param1.paramSet).build()) != null) {
                    throw new IllegalStateException("Duplicate loot table " + param2);
                }
            }));
        ValidationContext var1 = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
            @Nullable
            @Override
            public <T> T getElement(LootDataId<T> param0) {
                return (T)(param0.type() == LootDataType.TABLE ? var0.get(param0.location()) : null);
            }
        });

        for(ResourceLocation var3 : Sets.difference(this.requiredTables, var0.keySet())) {
            var1.reportProblem("Missing built-in table: " + var3);
        }

        var0.forEach(
            (param1, param2) -> param2.validate(
                    var1.setParams(param2.getParamSet()).enterElement("{" + param1 + "}", new LootDataId<>(LootDataType.TABLE, param1))
                )
        );
        Multimap<String, String> var4 = var1.getProblems();
        if (!var4.isEmpty()) {
            var4.forEach((param0x, param1) -> LOGGER.warn("Found validation problem in {}: {}", param0x, param1));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        } else {
            return CompletableFuture.allOf(var0.entrySet().stream().map(param1 -> {
                ResourceLocation var0x = param1.getKey();
                LootTable var1x = param1.getValue();
                Path var2x = this.pathProvider.json(var0x);
                return DataProvider.saveStable(param0, LootDataType.TABLE.parser().toJsonTree(var1x), var2x);
            }).toArray(param0x -> new CompletableFuture[param0x]));
        }
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    public static record SubProviderEntry(Supplier<LootTableSubProvider> provider, LootContextParamSet paramSet) {
    }
}
