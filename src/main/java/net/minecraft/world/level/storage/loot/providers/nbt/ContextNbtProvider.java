package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ContextNbtProvider implements NbtProvider {
    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
        @Override
        public Tag get(LootContext param0) {
            BlockEntity var0 = param0.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            return var0 != null ? var0.save(new CompoundTag()) : null;
        }

        @Override
        public String getId() {
            return "block_entity";
        }

        @Override
        public Set<LootContextParam<?>> getReferencedContextParams() {
            return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
        }
    };
    public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
    private final ContextNbtProvider.Getter getter;

    private static ContextNbtProvider.Getter forEntity(final LootContext.EntityTarget param0) {
        return new ContextNbtProvider.Getter() {
            @Nullable
            @Override
            public Tag get(LootContext param0x) {
                Entity var0 = param0.getParamOrNull(param0.getParam());
                return var0 != null ? NbtPredicate.getEntityTagToCompare(var0) : null;
            }

            @Override
            public String getId() {
                return param0.name();
            }

            @Override
            public Set<LootContextParam<?>> getReferencedContextParams() {
                return ImmutableSet.of(param0.getParam());
            }
        };
    }

    private ContextNbtProvider(ContextNbtProvider.Getter param0) {
        this.getter = param0;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Nullable
    @Override
    public Tag get(LootContext param0) {
        return this.getter.get(param0);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.getter.getReferencedContextParams();
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget param0) {
        return new ContextNbtProvider(forEntity(param0));
    }

    private static ContextNbtProvider createFromContext(String param0) {
        if (param0.equals("block_entity")) {
            return new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
        } else {
            LootContext.EntityTarget var0 = LootContext.EntityTarget.getByName(param0);
            return new ContextNbtProvider(forEntity(var0));
        }
    }

    interface Getter {
        @Nullable
        Tag get(LootContext var1);

        String getId();

        Set<LootContextParam<?>> getReferencedContextParams();
    }

    public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextNbtProvider> {
        public JsonElement serialize(ContextNbtProvider param0, JsonSerializationContext param1) {
            return new JsonPrimitive(param0.getter.getId());
        }

        public ContextNbtProvider deserialize(JsonElement param0, JsonDeserializationContext param1) {
            String var0 = param0.getAsString();
            return ContextNbtProvider.createFromContext(var0);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextNbtProvider> {
        public void serialize(JsonObject param0, ContextNbtProvider param1, JsonSerializationContext param2) {
            param0.addProperty("target", param1.getter.getId());
        }

        public ContextNbtProvider deserialize(JsonObject param0, JsonDeserializationContext param1) {
            String var0 = GsonHelper.getAsString(param0, "target");
            return ContextNbtProvider.createFromContext(var0);
        }
    }
}
