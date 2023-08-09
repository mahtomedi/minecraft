package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ContextNbtProvider implements NbtProvider {
    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
        @Override
        public Tag get(LootContext param0) {
            BlockEntity var0 = param0.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            return var0 != null ? var0.saveWithFullMetadata() : null;
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
    private static final Codec<ContextNbtProvider.Getter> GETTER_CODEC = Codec.STRING.xmap(param0 -> {
        if (param0.equals("block_entity")) {
            return BLOCK_ENTITY_PROVIDER;
        } else {
            LootContext.EntityTarget var0 = LootContext.EntityTarget.getByName(param0);
            return forEntity(var0);
        }
    }, ContextNbtProvider.Getter::getId);
    public static final Codec<ContextNbtProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(GETTER_CODEC.fieldOf("target").forGetter(param0x -> param0x.getter)).apply(param0, ContextNbtProvider::new)
    );
    public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, param0 -> param0.getter);
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

    interface Getter {
        @Nullable
        Tag get(LootContext var1);

        String getId();

        Set<LootContextParam<?>> getReferencedContextParams();
    }
}
