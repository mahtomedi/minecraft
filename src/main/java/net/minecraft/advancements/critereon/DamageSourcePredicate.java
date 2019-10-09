package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate {
    public static final DamageSourcePredicate ANY = DamageSourcePredicate.Builder.damageType().build();
    private final Boolean isProjectile;
    private final Boolean isExplosion;
    private final Boolean bypassesArmor;
    private final Boolean bypassesInvulnerability;
    private final Boolean bypassesMagic;
    private final Boolean isFire;
    private final Boolean isMagic;
    private final Boolean isLightning;
    private final EntityPredicate directEntity;
    private final EntityPredicate sourceEntity;

    public DamageSourcePredicate(
        @Nullable Boolean param0,
        @Nullable Boolean param1,
        @Nullable Boolean param2,
        @Nullable Boolean param3,
        @Nullable Boolean param4,
        @Nullable Boolean param5,
        @Nullable Boolean param6,
        @Nullable Boolean param7,
        EntityPredicate param8,
        EntityPredicate param9
    ) {
        this.isProjectile = param0;
        this.isExplosion = param1;
        this.bypassesArmor = param2;
        this.bypassesInvulnerability = param3;
        this.bypassesMagic = param4;
        this.isFire = param5;
        this.isMagic = param6;
        this.isLightning = param7;
        this.directEntity = param8;
        this.sourceEntity = param9;
    }

    public boolean matches(ServerPlayer param0, DamageSource param1) {
        return this.matches(param0.getLevel(), param0.position(), param1);
    }

    public boolean matches(ServerLevel param0, Vec3 param1, DamageSource param2) {
        if (this == ANY) {
            return true;
        } else if (this.isProjectile != null && this.isProjectile != param2.isProjectile()) {
            return false;
        } else if (this.isExplosion != null && this.isExplosion != param2.isExplosion()) {
            return false;
        } else if (this.bypassesArmor != null && this.bypassesArmor != param2.isBypassArmor()) {
            return false;
        } else if (this.bypassesInvulnerability != null && this.bypassesInvulnerability != param2.isBypassInvul()) {
            return false;
        } else if (this.bypassesMagic != null && this.bypassesMagic != param2.isBypassMagic()) {
            return false;
        } else if (this.isFire != null && this.isFire != param2.isFire()) {
            return false;
        } else if (this.isMagic != null && this.isMagic != param2.isMagic()) {
            return false;
        } else if (this.isLightning != null && this.isLightning != (param2 == DamageSource.LIGHTNING_BOLT)) {
            return false;
        } else if (!this.directEntity.matches(param0, param1, param2.getDirectEntity())) {
            return false;
        } else {
            return this.sourceEntity.matches(param0, param1, param2.getEntity());
        }
    }

    public static DamageSourcePredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "damage type");
            Boolean var1 = getOptionalBoolean(var0, "is_projectile");
            Boolean var2 = getOptionalBoolean(var0, "is_explosion");
            Boolean var3 = getOptionalBoolean(var0, "bypasses_armor");
            Boolean var4 = getOptionalBoolean(var0, "bypasses_invulnerability");
            Boolean var5 = getOptionalBoolean(var0, "bypasses_magic");
            Boolean var6 = getOptionalBoolean(var0, "is_fire");
            Boolean var7 = getOptionalBoolean(var0, "is_magic");
            Boolean var8 = getOptionalBoolean(var0, "is_lightning");
            EntityPredicate var9 = EntityPredicate.fromJson(var0.get("direct_entity"));
            EntityPredicate var10 = EntityPredicate.fromJson(var0.get("source_entity"));
            return new DamageSourcePredicate(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
        } else {
            return ANY;
        }
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject param0, String param1) {
        return param0.has(param1) ? GsonHelper.getAsBoolean(param0, param1) : null;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            this.addOptionally(var0, "is_projectile", this.isProjectile);
            this.addOptionally(var0, "is_explosion", this.isExplosion);
            this.addOptionally(var0, "bypasses_armor", this.bypassesArmor);
            this.addOptionally(var0, "bypasses_invulnerability", this.bypassesInvulnerability);
            this.addOptionally(var0, "bypasses_magic", this.bypassesMagic);
            this.addOptionally(var0, "is_fire", this.isFire);
            this.addOptionally(var0, "is_magic", this.isMagic);
            this.addOptionally(var0, "is_lightning", this.isLightning);
            var0.add("direct_entity", this.directEntity.serializeToJson());
            var0.add("source_entity", this.sourceEntity.serializeToJson());
            return var0;
        }
    }

    private void addOptionally(JsonObject param0, String param1, @Nullable Boolean param2) {
        if (param2 != null) {
            param0.addProperty(param1, param2);
        }

    }

    public static class Builder {
        private Boolean isProjectile;
        private Boolean isExplosion;
        private Boolean bypassesArmor;
        private Boolean bypassesInvulnerability;
        private Boolean bypassesMagic;
        private Boolean isFire;
        private Boolean isMagic;
        private Boolean isLightning;
        private EntityPredicate directEntity = EntityPredicate.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;

        public static DamageSourcePredicate.Builder damageType() {
            return new DamageSourcePredicate.Builder();
        }

        public DamageSourcePredicate.Builder isProjectile(Boolean param0) {
            this.isProjectile = param0;
            return this;
        }

        public DamageSourcePredicate.Builder isLightning(Boolean param0) {
            this.isLightning = param0;
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate.Builder param0) {
            this.directEntity = param0.build();
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate(
                this.isProjectile,
                this.isExplosion,
                this.bypassesArmor,
                this.bypassesInvulnerability,
                this.bypassesMagic,
                this.isFire,
                this.isMagic,
                this.isLightning,
                this.directEntity,
                this.sourceEntity
            );
        }
    }
}
