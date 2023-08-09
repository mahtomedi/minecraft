package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.List;

public class AllOfCondition extends CompositeLootItemCondition {
    public static final Codec<AllOfCondition> CODEC = createCodec(AllOfCondition::new);
    public static final Codec<AllOfCondition> INLINE_CODEC = createInlineCodec(AllOfCondition::new);

    AllOfCondition(List<LootItemCondition> param0) {
        super(param0, LootItemConditions.andConditions(param0));
    }

    public static AllOfCondition allOf(List<LootItemCondition> param0) {
        return new AllOfCondition(List.copyOf(param0));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ALL_OF;
    }

    public static AllOfCondition.Builder allOf(LootItemCondition.Builder... param0) {
        return new AllOfCondition.Builder(param0);
    }

    public static class Builder extends CompositeLootItemCondition.Builder {
        public Builder(LootItemCondition.Builder... param0) {
            super(param0);
        }

        @Override
        public AllOfCondition.Builder and(LootItemCondition.Builder param0) {
            this.addTerm(param0);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> param0) {
            return new AllOfCondition(param0);
        }
    }
}
