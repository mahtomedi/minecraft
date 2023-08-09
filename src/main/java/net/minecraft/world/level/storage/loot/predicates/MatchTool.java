package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record MatchTool(Optional<ItemPredicate> predicate) implements LootItemCondition {
    public static final Codec<MatchTool> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "predicate").forGetter(MatchTool::predicate)).apply(param0, MatchTool::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.MATCH_TOOL;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext param0) {
        ItemStack var0 = param0.getParamOrNull(LootContextParams.TOOL);
        return var0 != null && (this.predicate.isEmpty() || this.predicate.get().matches(var0));
    }

    public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder param0) {
        return () -> new MatchTool(param0.build());
    }
}
