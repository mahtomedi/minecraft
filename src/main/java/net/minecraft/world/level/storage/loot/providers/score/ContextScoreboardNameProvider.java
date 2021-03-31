package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class ContextScoreboardNameProvider implements ScoreboardNameProvider {
    private final LootContext.EntityTarget target;

    private ContextScoreboardNameProvider(LootContext.EntityTarget param0) {
        this.target = param0;
    }

    public static ScoreboardNameProvider forTarget(LootContext.EntityTarget param0) {
        return new ContextScoreboardNameProvider(param0);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.CONTEXT;
    }

    @Nullable
    @Override
    public String getScoreboardName(LootContext param0) {
        Entity var0 = param0.getParamOrNull(this.target.getParam());
        return var0 != null ? var0.getScoreboardName() : null;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }

    public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextScoreboardNameProvider> {
        public JsonElement serialize(ContextScoreboardNameProvider param0, JsonSerializationContext param1) {
            return param1.serialize(param0.target);
        }

        public ContextScoreboardNameProvider deserialize(JsonElement param0, JsonDeserializationContext param1) {
            LootContext.EntityTarget var0 = param1.deserialize(param0, LootContext.EntityTarget.class);
            return new ContextScoreboardNameProvider(var0);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextScoreboardNameProvider> {
        public void serialize(JsonObject param0, ContextScoreboardNameProvider param1, JsonSerializationContext param2) {
            param0.addProperty("target", param1.target.name());
        }

        public ContextScoreboardNameProvider deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootContext.EntityTarget var0 = GsonHelper.getAsObject(param0, "target", param1, LootContext.EntityTarget.class);
            return new ContextScoreboardNameProvider(var0);
        }
    }
}
