package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public class DamagePredicate {
    public static final DamagePredicate ANY = DamagePredicate.Builder.damageInstance().build();
    private final MinMaxBounds.Floats dealtDamage;
    private final MinMaxBounds.Floats takenDamage;
    private final EntityPredicate sourceEntity;
    private final Boolean blocked;
    private final DamageSourcePredicate type;

    public DamagePredicate() {
        this.dealtDamage = MinMaxBounds.Floats.ANY;
        this.takenDamage = MinMaxBounds.Floats.ANY;
        this.sourceEntity = EntityPredicate.ANY;
        this.blocked = null;
        this.type = DamageSourcePredicate.ANY;
    }

    public DamagePredicate(
        MinMaxBounds.Floats param0, MinMaxBounds.Floats param1, EntityPredicate param2, @Nullable Boolean param3, DamageSourcePredicate param4
    ) {
        this.dealtDamage = param0;
        this.takenDamage = param1;
        this.sourceEntity = param2;
        this.blocked = param3;
        this.type = param4;
    }

    public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        if (this == ANY) {
            return true;
        } else if (!this.dealtDamage.matches(param2)) {
            return false;
        } else if (!this.takenDamage.matches(param3)) {
            return false;
        } else if (!this.sourceEntity.matches(param0, param1.getEntity())) {
            return false;
        } else if (this.blocked != null && this.blocked != param4) {
            return false;
        } else {
            return this.type.matches(param0, param1);
        }
    }

    public static DamagePredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "damage");
            MinMaxBounds.Floats var1 = MinMaxBounds.Floats.fromJson(var0.get("dealt"));
            MinMaxBounds.Floats var2 = MinMaxBounds.Floats.fromJson(var0.get("taken"));
            Boolean var3 = var0.has("blocked") ? GsonHelper.getAsBoolean(var0, "blocked") : null;
            EntityPredicate var4 = EntityPredicate.fromJson(var0.get("source_entity"));
            DamageSourcePredicate var5 = DamageSourcePredicate.fromJson(var0.get("type"));
            return new DamagePredicate(var1, var2, var4, var3, var5);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("dealt", this.dealtDamage.serializeToJson());
            var0.add("taken", this.takenDamage.serializeToJson());
            var0.add("source_entity", this.sourceEntity.serializeToJson());
            var0.add("type", this.type.serializeToJson());
            if (this.blocked != null) {
                var0.addProperty("blocked", this.blocked);
            }

            return var0;
        }
    }

    public static class Builder {
        private MinMaxBounds.Floats dealtDamage = MinMaxBounds.Floats.ANY;
        private MinMaxBounds.Floats takenDamage = MinMaxBounds.Floats.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;
        private Boolean blocked;
        private DamageSourcePredicate type = DamageSourcePredicate.ANY;

        public static DamagePredicate.Builder damageInstance() {
            return new DamagePredicate.Builder();
        }

        public DamagePredicate.Builder dealtDamage(MinMaxBounds.Floats param0) {
            this.dealtDamage = param0;
            return this;
        }

        public DamagePredicate.Builder takenDamage(MinMaxBounds.Floats param0) {
            this.takenDamage = param0;
            return this;
        }

        public DamagePredicate.Builder sourceEntity(EntityPredicate param0) {
            this.sourceEntity = param0;
            return this;
        }

        public DamagePredicate.Builder blocked(Boolean param0) {
            this.blocked = param0;
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate param0) {
            this.type = param0;
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate.Builder param0) {
            this.type = param0.build();
            return this;
        }

        public DamagePredicate build() {
            return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
        }
    }
}
