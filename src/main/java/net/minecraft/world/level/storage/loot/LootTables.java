package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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
    private final PredicateManager predicateManager;

    public LootTables(PredicateManager param0) {
        super(GSON, "loot_tables");
        this.predicateManager = param0;
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
        ValidationContext var3 = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, var2::get);
        var2.forEach((param1x, param2x) -> validate(var3, param1x, param2x));
        var3.getProblems().forEach((param0x, param1x) -> LOGGER.warn("Found validation problem in " + param0x + ": " + param1x));
        this.tables = var2;
    }

    public static void validate(ValidationContext param0, ResourceLocation param1, LootTable param2) {
        param2.validate(param0.setParams(param2.getParamSet()).enterTable("{" + param1 + "}", param1));
    }

    public static JsonElement serialize(LootTable param0) {
        return GSON.toJsonTree(param0);
    }

    public Set<ResourceLocation> getIds() {
        return this.tables.keySet();
    }
}
