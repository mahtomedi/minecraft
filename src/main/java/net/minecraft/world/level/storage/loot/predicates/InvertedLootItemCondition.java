package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record InvertedLootItemCondition(LootItemCondition term) implements LootItemCondition {
    public static final Codec<InvertedLootItemCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(LootItemConditions.CODEC.fieldOf("term").forGetter(InvertedLootItemCondition::term))
                .apply(param0, InvertedLootItemCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    public boolean test(LootContext param0) {
        return !this.term.test(param0);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(ValidationContext param0) {
        LootItemCondition.super.validate(param0);
        this.term.validate(param0);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder param0) {
        InvertedLootItemCondition var0 = new InvertedLootItemCondition(param0.build());
        return () -> var0;
    }
}
