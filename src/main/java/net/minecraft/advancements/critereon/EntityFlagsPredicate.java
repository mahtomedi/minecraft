package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityFlagsPredicate {
    public static final EntityFlagsPredicate ANY = new EntityFlagsPredicate.Builder().build();
    @Nullable
    private final Boolean isOnFire;
    @Nullable
    private final Boolean isSneaking;
    @Nullable
    private final Boolean isSprinting;
    @Nullable
    private final Boolean isSwimming;
    @Nullable
    private final Boolean isBaby;

    public EntityFlagsPredicate(
        @Nullable Boolean param0, @Nullable Boolean param1, @Nullable Boolean param2, @Nullable Boolean param3, @Nullable Boolean param4
    ) {
        this.isOnFire = param0;
        this.isSneaking = param1;
        this.isSprinting = param2;
        this.isSwimming = param3;
        this.isBaby = param4;
    }

    public boolean matches(Entity param0) {
        if (this.isOnFire != null && param0.isOnFire() != this.isOnFire) {
            return false;
        } else if (this.isSneaking != null && param0.isSneaking() != this.isSneaking) {
            return false;
        } else if (this.isSprinting != null && param0.isSprinting() != this.isSprinting) {
            return false;
        } else if (this.isSwimming != null && param0.isSwimming() != this.isSwimming) {
            return false;
        } else {
            return this.isBaby == null || !(param0 instanceof LivingEntity) || ((LivingEntity)param0).isBaby() == this.isBaby;
        }
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject param0, String param1) {
        return param0.has(param1) ? GsonHelper.getAsBoolean(param0, param1) : null;
    }

    public static EntityFlagsPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "entity flags");
            Boolean var1 = getOptionalBoolean(var0, "is_on_fire");
            Boolean var2 = getOptionalBoolean(var0, "is_sneaking");
            Boolean var3 = getOptionalBoolean(var0, "is_sprinting");
            Boolean var4 = getOptionalBoolean(var0, "is_swimming");
            Boolean var5 = getOptionalBoolean(var0, "is_baby");
            return new EntityFlagsPredicate(var1, var2, var3, var4, var5);
        } else {
            return ANY;
        }
    }

    private void addOptionalBoolean(JsonObject param0, String param1, @Nullable Boolean param2) {
        if (param2 != null) {
            param0.addProperty(param1, param2);
        }

    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            this.addOptionalBoolean(var0, "is_on_fire", this.isOnFire);
            this.addOptionalBoolean(var0, "is_sneaking", this.isSneaking);
            this.addOptionalBoolean(var0, "is_sprinting", this.isSprinting);
            this.addOptionalBoolean(var0, "is_swimming", this.isSwimming);
            this.addOptionalBoolean(var0, "is_baby", this.isBaby);
            return var0;
        }
    }

    public static class Builder {
        @Nullable
        private Boolean isOnFire;
        @Nullable
        private Boolean isSneaking;
        @Nullable
        private Boolean isSprinting;
        @Nullable
        private Boolean isSwimming;
        @Nullable
        private Boolean isBaby;

        public static EntityFlagsPredicate.Builder flags() {
            return new EntityFlagsPredicate.Builder();
        }

        public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean param0) {
            this.isOnFire = param0;
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnFire, this.isSneaking, this.isSprinting, this.isSwimming, this.isBaby);
        }
    }
}
