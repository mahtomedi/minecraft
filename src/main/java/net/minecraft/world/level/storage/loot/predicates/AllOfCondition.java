package net.minecraft.world.level.storage.loot.predicates;

public class AllOfCondition extends CompositeLootItemCondition {
    AllOfCondition(LootItemCondition[] param0) {
        super(param0, LootItemConditions.andConditions(param0));
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
        protected LootItemCondition create(LootItemCondition[] param0) {
            return new AllOfCondition(param0);
        }
    }

    public static class Serializer extends CompositeLootItemCondition.Serializer<AllOfCondition> {
        protected AllOfCondition create(LootItemCondition[] param0) {
            return new AllOfCondition(param0);
        }
    }
}
