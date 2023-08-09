package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record ContextScoreboardNameProvider(LootContext.EntityTarget target) implements ScoreboardNameProvider {
    public static final Codec<ContextScoreboardNameProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(LootContext.EntityTarget.CODEC.fieldOf("target").forGetter(ContextScoreboardNameProvider::target))
                .apply(param0, ContextScoreboardNameProvider::new)
    );
    public static final Codec<ContextScoreboardNameProvider> INLINE_CODEC = LootContext.EntityTarget.CODEC
        .xmap(ContextScoreboardNameProvider::new, ContextScoreboardNameProvider::target);

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
}
