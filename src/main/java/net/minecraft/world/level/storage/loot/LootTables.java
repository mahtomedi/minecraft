package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTables extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RandomValueBounds.class, new RandomValueBounds.Serializer())
        .registerTypeAdapter(BinomialDistributionGenerator.class, new BinomialDistributionGenerator.Serializer())
        .registerTypeAdapter(ConstantIntValue.class, new ConstantIntValue.Serializer())
        .registerTypeAdapter(IntLimiter.class, new IntLimiter.Serializer())
        .registerTypeAdapter(LootPool.class, new LootPool.Serializer())
        .registerTypeAdapter(LootTable.class, new LootTable.Serializer())
        .registerTypeHierarchyAdapter(LootPoolEntryContainer.class, new LootPoolEntries.Serializer())
        .registerTypeHierarchyAdapter(LootItemFunction.class, new LootItemFunctions.Serializer())
        .registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer())
        .registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
        .create();
    private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();

    public LootTables() {
        super(GSON, "loot_tables");
    }

    public LootTable get(ResourceLocation param0) {
        return this.tables.getOrDefault(param0, LootTable.EMPTY);
    }

    protected void apply(Map<ResourceLocation, JsonObject> param0, ResourceManager param1, ProfilerFiller param2) {
        Builder<ResourceLocation, LootTable> var0 = ImmutableMap.builder();
        JsonObject var1 = param0.remove(BuiltInLootTables.EMPTY);
        if (var1 != null) {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
        }

        param0.forEach((param1x, param2x) -> {
            try {
                LootTable var0x = GSON.fromJson(param2x, LootTable.class);
                var0.put(param1x, var0x);
            } catch (Exception var4x) {
                LOGGER.error("Couldn't parse loot table {}", param1x, var4x);
            }

        });
        var0.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
        ImmutableMap<ResourceLocation, LootTable> var2 = var0.build();
        LootTableProblemCollector var3 = new LootTableProblemCollector();
        var2.forEach((param2x, param3) -> validate(var3, param2x, param3, var2::get));
        var3.getProblems().forEach((param0x, param1x) -> LOGGER.warn("Found validation problem in " + param0x + ": " + param1x));
        this.tables = var2;
    }

    public static void validate(LootTableProblemCollector param0, ResourceLocation param1, LootTable param2, Function<ResourceLocation, LootTable> param3) {
        Set<ResourceLocation> var0 = ImmutableSet.of(param1);
        param2.validate(param0.forChild("{" + param1.toString() + "}"), param3, var0, param2.getParamSet());
    }

    public static JsonElement serialize(LootTable param0) {
        return GSON.toJsonTree(param0);
    }

    public Set<ResourceLocation> getIds() {
        return this.tables.keySet();
    }
}
