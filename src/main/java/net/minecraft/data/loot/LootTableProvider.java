package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.RandomSupport;
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
        Map<RandomSupport.Seed128bit, ResourceLocation> var1 = new Object2ObjectOpenHashMap<>();
        this.subProviders.forEach(param2 -> param2.provider().get().generate((param3, param4) -> {
                ResourceLocation var0x = var1.put(RandomSequence.seedForKey(param3), param3);
                if (var0x != null) {
                    Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + var0x + " and " + param3);
                }

                param4.setRandomSequence(param3);
                if (var0.put(param3, param4.setParamSet(param2.paramSet).build()) != null) {
                    throw new IllegalStateException("Duplicate loot table " + param3);
                }
            }));
        ValidationContext var2 = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
            @Nullable
            @Override
            public <T> T getElement(LootDataId<T> param0) {
                return (T)(param0.type() == LootDataType.TABLE ? var0.get(param0.location()) : null);
            }
        });

        for(ResourceLocation var4 : Sets.difference(this.requiredTables, var0.keySet())) {
            var2.reportProblem("Missing built-in table: " + var4);
        }

        var0.forEach(
            (param1, param2) -> param2.validate(
                    var2.setParams(param2.getParamSet()).enterElement("{" + param1 + "}", new LootDataId<>(LootDataType.TABLE, param1))
                )
        );
        Multimap<String, String> var5 = var2.getProblems();
        if (!var5.isEmpty()) {
            var5.forEach((param0x, param1) -> LOGGER.warn("Found validation problem in {}: {}", param0x, param1));
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
