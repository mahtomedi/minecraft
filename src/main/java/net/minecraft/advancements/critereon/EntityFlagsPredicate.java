package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record EntityFlagsPredicate(
    Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isBaby
) {
    public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "is_on_fire").forGetter(EntityFlagsPredicate::isOnFire),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "is_sneaking").forGetter(EntityFlagsPredicate::isCrouching),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "is_sprinting").forGetter(EntityFlagsPredicate::isSprinting),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "is_swimming").forGetter(EntityFlagsPredicate::isSwimming),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "is_baby").forGetter(EntityFlagsPredicate::isBaby)
                )
                .apply(param0, EntityFlagsPredicate::new)
    );

    public boolean matches(Entity param0) {
        if (this.isOnFire.isPresent() && param0.isOnFire() != this.isOnFire.get()) {
            return false;
        } else if (this.isCrouching.isPresent() && param0.isCrouching() != this.isCrouching.get()) {
            return false;
        } else if (this.isSprinting.isPresent() && param0.isSprinting() != this.isSprinting.get()) {
            return false;
        } else if (this.isSwimming.isPresent() && param0.isSwimming() != this.isSwimming.get()) {
            return false;
        } else {
            if (this.isBaby.isPresent() && param0 instanceof LivingEntity var0 && var0.isBaby() != this.isBaby.get()) {
                return false;
            }

            return true;
        }
    }

    public static class Builder {
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();

        public static EntityFlagsPredicate.Builder flags() {
            return new EntityFlagsPredicate.Builder();
        }

        public EntityFlagsPredicate.Builder setOnFire(Boolean param0) {
            this.isOnFire = Optional.of(param0);
            return this;
        }

        public EntityFlagsPredicate.Builder setCrouching(Boolean param0) {
            this.isCrouching = Optional.of(param0);
            return this;
        }

        public EntityFlagsPredicate.Builder setSprinting(Boolean param0) {
            this.isSprinting = Optional.of(param0);
            return this;
        }

        public EntityFlagsPredicate.Builder setSwimming(Boolean param0) {
            this.isSwimming = Optional.of(param0);
            return this;
        }

        public EntityFlagsPredicate.Builder setIsBaby(Boolean param0) {
            this.isBaby = Optional.of(param0);
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
        }
    }
}
