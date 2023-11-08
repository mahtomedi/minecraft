package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(
    MinMaxBounds.Doubles dealtDamage,
    MinMaxBounds.Doubles takenDamage,
    Optional<EntityPredicate> sourceEntity,
    Optional<Boolean> blocked,
    Optional<DamageSourcePredicate> type
) {
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "dealt", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::dealtDamage),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "taken", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::takenDamage),
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "source_entity").forGetter(DamagePredicate::sourceEntity),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "blocked").forGetter(DamagePredicate::blocked),
                    ExtraCodecs.strictOptionalField(DamageSourcePredicate.CODEC, "type").forGetter(DamagePredicate::type)
                )
                .apply(param0, DamagePredicate::new)
    );

    public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        if (!this.dealtDamage.matches((double)param2)) {
            return false;
        } else if (!this.takenDamage.matches((double)param3)) {
            return false;
        } else if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches(param0, param1.getEntity())) {
            return false;
        } else if (this.blocked.isPresent() && this.blocked.get() != param4) {
            return false;
        } else {
            return !this.type.isPresent() || this.type.get().matches(param0, param1);
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> blocked = Optional.empty();
        private Optional<DamageSourcePredicate> type = Optional.empty();

        public static DamagePredicate.Builder damageInstance() {
            return new DamagePredicate.Builder();
        }

        public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles param0) {
            this.dealtDamage = param0;
            return this;
        }

        public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles param0) {
            this.takenDamage = param0;
            return this;
        }

        public DamagePredicate.Builder sourceEntity(EntityPredicate param0) {
            this.sourceEntity = Optional.of(param0);
            return this;
        }

        public DamagePredicate.Builder blocked(Boolean param0) {
            this.blocked = Optional.of(param0);
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate param0) {
            this.type = Optional.of(param0);
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate.Builder param0) {
            this.type = Optional.of(param0.build());
            return this;
        }

        public DamagePredicate build() {
            return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
        }
    }
}
