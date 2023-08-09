package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.List;

public class AnyOfCondition extends CompositeLootItemCondition {
    public static final Codec<AnyOfCondition> CODEC = createCodec(AnyOfCondition::new);

    AnyOfCondition(List<LootItemCondition> param0) {
        super(param0, LootItemConditions.orConditions(param0));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ANY_OF;
    }

    public static AnyOfCondition.Builder anyOf(LootItemCondition.Builder... param0) {
        return new AnyOfCondition.Builder(param0);
    }

    public static class Builder extends CompositeLootItemCondition.Builder {
        public Builder(LootItemCondition.Builder... param0) {
            super(param0);
        }

        @Override
        public AnyOfCondition.Builder or(LootItemCondition.Builder param0) {
            this.addTerm(param0);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> param0) {
            return new AnyOfCondition(param0);
        }
    }
}
