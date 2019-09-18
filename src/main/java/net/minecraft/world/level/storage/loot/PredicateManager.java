package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PredicateManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RandomValueBounds.class, new RandomValueBounds.Serializer())
        .registerTypeAdapter(BinomialDistributionGenerator.class, new BinomialDistributionGenerator.Serializer())
        .registerTypeAdapter(ConstantIntValue.class, new ConstantIntValue.Serializer())
        .registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer())
        .registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
        .create();
    private Map<ResourceLocation, LootItemCondition> conditions = ImmutableMap.of();

    public PredicateManager() {
        super(GSON, "predicates");
    }

    @Nullable
    public LootItemCondition get(ResourceLocation param0) {
        return this.conditions.get(param0);
    }

    public LootItemCondition get(ResourceLocation param0, LootItemCondition param1) {
        return this.conditions.getOrDefault(param0, param1);
    }

    protected void apply(Map<ResourceLocation, JsonObject> param0, ResourceManager param1, ProfilerFiller param2) {
        Builder<ResourceLocation, LootItemCondition> var0 = ImmutableMap.builder();
        param0.forEach((param1x, param2x) -> {
            try {
                LootItemCondition var0x = GSON.fromJson(param2x, LootItemCondition.class);
                var0.put(param1x, var0x);
            } catch (Exception var4x) {
                LOGGER.error("Couldn't parse loot table {}", param1x, var4x);
            }

        });
        Map<ResourceLocation, LootItemCondition> var1 = var0.build();
        ValidationContext var2 = new ValidationContext(LootContextParamSets.ALL_PARAMS, var1::get, param0x -> null);
        var1.forEach((param1x, param2x) -> param2x.validate(var2.enterCondition("{" + param1x + "}", param1x)));
        var2.getProblems().forEach((param0x, param1x) -> LOGGER.warn("Found validation problem in " + param0x + ": " + param1x));
        this.conditions = var1;
    }

    public Set<ResourceLocation> getKeys() {
        return Collections.unmodifiableSet(this.conditions.keySet());
    }
}
