package net.minecraft.world.level.storage.loot.predicates;

public class AnyOfCondition extends CompositeLootItemCondition {
    AnyOfCondition(LootItemCondition[] param0) {
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
        protected LootItemCondition create(LootItemCondition[] param0) {
            return new AnyOfCondition(param0);
        }
    }

    public static class Serializer extends CompositeLootItemCondition.Serializer<AnyOfCondition> {
        protected AnyOfCondition create(LootItemCondition[] param0) {
            return new AnyOfCondition(param0);
        }
    }
}
