package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(
    MinMaxBounds.Doubles dealtDamage,
    MinMaxBounds.Doubles takenDamage,
    Optional<EntityPredicate> sourceEntity,
    Optional<Boolean> blocked,
    Optional<DamageSourcePredicate> type
) {
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

    public static Optional<DamagePredicate> fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "damage");
            MinMaxBounds.Doubles var1 = MinMaxBounds.Doubles.fromJson(var0.get("dealt"));
            MinMaxBounds.Doubles var2 = MinMaxBounds.Doubles.fromJson(var0.get("taken"));
            Optional<Boolean> var3 = var0.has("blocked") ? Optional.of(GsonHelper.getAsBoolean(var0, "blocked")) : Optional.empty();
            Optional<EntityPredicate> var4 = EntityPredicate.fromJson(var0.get("source_entity"));
            Optional<DamageSourcePredicate> var5 = DamageSourcePredicate.fromJson(var0.get("type"));
            return var1.isAny() && var2.isAny() && var4.isEmpty() && var3.isEmpty() && var5.isEmpty()
                ? Optional.empty()
                : Optional.of(new DamagePredicate(var1, var2, var4, var3, var5));
        } else {
            return Optional.empty();
        }
    }

    public JsonElement serializeToJson() {
        JsonObject var0 = new JsonObject();
        var0.add("dealt", this.dealtDamage.serializeToJson());
        var0.add("taken", this.takenDamage.serializeToJson());
        this.sourceEntity.ifPresent(param1 -> var0.add("source_entity", param1.serializeToJson()));
        this.type.ifPresent(param1 -> var0.add("type", param1.serializeToJson()));
        this.blocked.ifPresent(param1 -> var0.addProperty("blocked", param1));
        return var0;
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
