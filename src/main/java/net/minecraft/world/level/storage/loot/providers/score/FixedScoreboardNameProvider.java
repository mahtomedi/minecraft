package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record FixedScoreboardNameProvider(String name) implements ScoreboardNameProvider {
    public static final Codec<FixedScoreboardNameProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.STRING.fieldOf("name").forGetter(FixedScoreboardNameProvider::name)).apply(param0, FixedScoreboardNameProvider::new)
    );

    public static ScoreboardNameProvider forName(String param0) {
        return new FixedScoreboardNameProvider(param0);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.FIXED;
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
}
