package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class DeserializationContext {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation id;
    private final LootDataManager lootData;

    public DeserializationContext(ResourceLocation param0, LootDataManager param1) {
        this.id = param0;
        this.lootData = param1;
    }

    public final List<LootItemCondition> deserializeConditions(JsonArray param0, String param1, LootContextParamSet param2) {
        List<LootItemCondition> var0 = Util.getOrThrow(LootItemConditions.CODEC.listOf().parse(JsonOps.INSTANCE, param0), JsonParseException::new);
        ValidationContext var1 = new ValidationContext(param2, this.lootData);

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
