package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemKilledByPlayerCondition implements LootItemCondition {
    private static final LootItemKilledByPlayerCondition INSTANCE = new LootItemKilledByPlayerCondition();

    private LootItemKilledByPlayerCondition() {
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public boolean test(LootContext param0) {
        return param0.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder killedByPlayer() {
        return () -> INSTANCE;
    }

    public static class Serializer extends LootItemCondition.Serializer<LootItemKilledByPlayerCondition> {
        protected Serializer() {
            super(new ResourceLocation("killed_by_player"), LootItemKilledByPlayerCondition.class);
        }

        public void serialize(JsonObject param0, LootItemKilledByPlayerCondition param1, JsonSerializationContext param2) {
        }

        public LootItemKilledByPlayerCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return LootItemKilledByPlayerCondition.INSTANCE;
        }
    }
}
