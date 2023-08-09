package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public record DamageSourcePredicate(List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity) {
    public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(TagPredicate.codec(Registries.DAMAGE_TYPE).listOf(), "tags", List.of())
                        .forGetter(DamageSourcePredicate::tags),
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "direct_entity").forGetter(DamageSourcePredicate::directEntity),
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "source_entity").forGetter(DamageSourcePredicate::sourceEntity)
                )
                .apply(param0, DamageSourcePredicate::new)
    );

    static Optional<DamageSourcePredicate> of(List<TagPredicate<DamageType>> param0, Optional<EntityPredicate> param1, Optional<EntityPredicate> param2) {
        return param0.isEmpty() && param1.isEmpty() && param2.isEmpty() ? Optional.empty() : Optional.of(new DamageSourcePredicate(param0, param1, param2));
    }

    public boolean matches(ServerPlayer param0, DamageSource param1) {
        return this.matches(param0.serverLevel(), param0.position(), param1);
    }

    public boolean matches(ServerLevel param0, Vec3 param1, DamageSource param2) {
        for(TagPredicate<DamageType> var0 : this.tags) {
            if (!var0.matches(param2.typeHolder())) {
                return false;
            }
        }

        if (this.directEntity.isPresent() && !this.directEntity.get().matches(param0, param1, param2.getDirectEntity())) {
            return false;
        } else {
            return !this.sourceEntity.isPresent() || this.sourceEntity.get().matches(param0, param1, param2.getEntity());
        }
    }

    public static Optional<DamageSourcePredicate> fromJson(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull()
            ? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new))
            : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private Optional<EntityPredicate> directEntity = Optional.empty();
        private Optional<EntityPredicate> sourceEntity = Optional.empty();

        public static DamageSourcePredicate.Builder damageType() {
            return new DamageSourcePredicate.Builder();
        }

        public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> param0) {
            this.tags.add(param0);
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate.Builder param0) {
            this.directEntity = param0.build();
            return this;
        }

        public DamageSourcePredicate.Builder source(EntityPredicate.Builder param0) {
            this.sourceEntity = param0.build();
            return this;
        }

        public Optional<DamageSourcePredicate> build() {
            return DamageSourcePredicate.of(this.tags.build(), this.directEntity, this.sourceEntity);
        }
    }
}
