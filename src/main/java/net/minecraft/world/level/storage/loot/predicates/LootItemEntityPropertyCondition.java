package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootItemEntityPropertyCondition implements LootItemCondition {
    private final EntityPredicate predicate;
    private final LootContext.EntityTarget entityTarget;

    private LootItemEntityPropertyCondition(EntityPredicate param0, LootContext.EntityTarget param1) {
        this.predicate = param0;
        this.entityTarget = param1;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_POS, this.entityTarget.getParam());
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(this.entityTarget.getParam());
        BlockPos var1 = param0.getParamOrNull(LootContextParams.BLOCK_POS);
        return var1 != null && this.predicate.matches(param0.getLevel(), new Vec3(var1), var0);
    }

    public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget param0) {
        return hasProperties(param0, EntityPredicate.Builder.entity());
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget param0, EntityPredicate.Builder param1) {
        return () -> new LootItemEntityPropertyCondition(param1.build(), param0);
    }

    public static class Serializer extends LootItemCondition.Serializer<LootItemEntityPropertyCondition> {
        protected Serializer() {
            super(new ResourceLocation("entity_properties"), LootItemEntityPropertyCondition.class);
        }

        public void serialize(JsonObject param0, LootItemEntityPropertyCondition param1, JsonSerializationContext param2) {
            param0.add("predicate", param1.predicate.serializeToJson());
            param0.add("entity", param2.serialize(param1.entityTarget));
        }

        public LootItemEntityPropertyCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            EntityPredicate var0 = EntityPredicate.fromJson(param0.get("predicate"));
            return new LootItemEntityPropertyCondition(var0, GsonHelper.getAsObject(param0, "entity", param1, LootContext.EntityTarget.class));
        }
    }
}
