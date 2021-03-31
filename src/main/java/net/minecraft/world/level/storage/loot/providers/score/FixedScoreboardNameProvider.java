package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class FixedScoreboardNameProvider implements ScoreboardNameProvider {
    private final String name;

    private FixedScoreboardNameProvider(String param0) {
        this.name = param0;
    }

    public static ScoreboardNameProvider forName(String param0) {
        return new FixedScoreboardNameProvider(param0);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.FIXED;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public String getScoreboardName(LootContext param0) {
        return this.name;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FixedScoreboardNameProvider> {
        public void serialize(JsonObject param0, FixedScoreboardNameProvider param1, JsonSerializationContext param2) {
            param0.addProperty("name", param1.name);
        }

        public FixedScoreboardNameProvider deserialize(JsonObject param0, JsonDeserializationContext param1) {
            String var0 = GsonHelper.getAsString(param0, "name");
            return new FixedScoreboardNameProvider(var0);
        }
    }
}
