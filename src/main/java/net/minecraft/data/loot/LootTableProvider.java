package net.minecraft.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator.PathProvider pathProvider;
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders = ImmutableList.of(
        Pair.of(FishingLoot::new, LootContextParamSets.FISHING),
        Pair.of(ChestLoot::new, LootContextParamSets.CHEST),
        Pair.of(EntityLoot::new, LootContextParamSets.ENTITY),
        Pair.of(BlockLoot::new, LootContextParamSets.BLOCK),
        Pair.of(PiglinBarterLoot::new, LootContextParamSets.PIGLIN_BARTER),
        Pair.of(GiftLoot::new, LootContextParamSets.GIFT)
    );

    public LootTableProvider(DataGenerator param0) {
        this.pathProvider = param0.createPathProvider(DataGenerator.Target.DATA_PACK, "loot_tables");
    }

    @Override
    public void run(CachedOutput param0) {
        Map<ResourceLocation, LootTable> var0 = Maps.newHashMap();
        this.subProviders.forEach(param1 -> param1.getFirst().get().accept((param2, param3) -> {
                if (var0.put(param2, param3.setParamSet(param1.getSecond()).build()) != null) {
                    throw new IllegalStateException("Duplicate loot table " + param2);
                }
            }));
        ValidationContext var1 = new ValidationContext(LootContextParamSets.ALL_PARAMS, param0x -> null, var0::get);

        for(ResourceLocation var3 : Sets.difference(BuiltInLootTables.all(), var0.keySet())) {
            var1.reportProblem("Missing built-in table: " + var3);
        }

        var0.forEach((param1, param2) -> LootTables.validate(var1, param1, param2));
        Multimap<String, String> var4 = var1.getProblems();
        if (!var4.isEmpty()) {
            var4.forEach((param0x, param1) -> LOGGER.warn("Found validation problem in {}: {}", param0x, param1));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        } else {
            var0.forEach((param1, param2) -> {
                Path var0x = this.pathProvider.json(param1);

                try {
                    DataProvider.saveStable(param0, LootTables.serialize(param2), var0x);
                } catch (IOException var6x) {
                    LOGGER.error("Couldn't save loot table {}", var0x, var6x);
                }

            });
        }
    }

    @Override
    public String getName() {
        return "LootTables";
    }
}
