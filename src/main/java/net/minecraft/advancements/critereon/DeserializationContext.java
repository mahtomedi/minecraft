package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeserializationContext {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation id;
    private final PredicateManager predicateManager;
    private final Gson predicateGson = Deserializers.createConditionSerializer().create();

    public DeserializationContext(ResourceLocation param0, PredicateManager param1) {
        this.id = param0;
        this.predicateManager = param1;
    }

    public final LootItemCondition[] deserializeConditions(JsonArray param0, String param1, LootContextParamSet param2) {
        LootItemCondition[] var0 = this.predicateGson.fromJson(param0, LootItemCondition[].class);
        ValidationContext var1 = new ValidationContext(param2, this.predicateManager::get, param0x -> null);

        for(LootItemCondition var2 : var0) {
            var2.validate(var1);
            var1.getProblems()
                .forEach((param1x, param2x) -> LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", param1, param1x, param2x));
        }

        return var0;
    }

    public ResourceLocation getAdvancementId() {
        return this.id;
    }
}
