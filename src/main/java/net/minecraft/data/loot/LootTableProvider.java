package net.minecraft.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTableProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders = ImmutableList.of(
        Pair.of(FishingLoot::new, LootContextParamSets.FISHING),
        Pair.of(ChestLoot::new, LootContextParamSets.CHEST),
        Pair.of(EntityLoot::new, LootContextParamSets.ENTITY),
        Pair.of(BlockLoot::new, LootContextParamSets.BLOCK),
        Pair.of(GiftLoot::new, LootContextParamSets.GIFT)
    );

    public LootTableProvider(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) {
        Path var0 = this.generator.getOutputFolder();
        Map<ResourceLocation, LootTable> var1 = Maps.newHashMap();
        this.subProviders.forEach(param1 -> param1.getFirst().get().accept((param2, param3) -> {
                if (var1.put(param2, param3.setParamSet(param1.getSecond()).build()) != null) {
                    throw new IllegalStateException("Duplicate loot table " + param2);
                }
            }));
        ValidationContext var2 = new ValidationContext(LootContextParamSets.ALL_PARAMS, param0x -> null, var1::get);

        for(ResourceLocation var4 : Sets.difference(BuiltInLootTables.all(), var1.keySet())) {
            var2.reportProblem("Missing built-in table: " + var4);
        }

        var1.forEach((param1, param2) -> LootTables.validate(var2, param1, param2));
        Multimap<String, String> var5 = var2.getProblems();
        if (!var5.isEmpty()) {
            var5.forEach((param0x, param1) -> LOGGER.warn("Found validation problem in " + param0x + ": " + param1));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        } else {
            var1.forEach((param2, param3) -> {
                Path var0x = createPath(var0, param2);

                try {
                    DataProvider.save(GSON, param0, LootTables.serialize(param3), var0x);
                } catch (IOException var6) {
                    LOGGER.error("Couldn't save loot table {}", var0x, var6);
                }

            });
        }
    }

    private static Path createPath(Path param0, ResourceLocation param1) {
        return param0.resolve("data/" + param1.getNamespace() + "/loot_tables/" + param1.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "LootTables";
    }
}
