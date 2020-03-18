package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
    public static final EntityPredicate ANY = new EntityPredicate(
        EntityTypePredicate.ANY,
        DistancePredicate.ANY,
        LocationPredicate.ANY,
        MobEffectsPredicate.ANY,
        NbtPredicate.ANY,
        EntityFlagsPredicate.ANY,
        EntityEquipmentPredicate.ANY,
        PlayerPredicate.ANY,
        FishingHookPredicate.ANY,
        null,
        null
    );
    public static final EntityPredicate[] ANY_ARRAY = new EntityPredicate[0];
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
    @Nullable
    private final String team;
    @Nullable
    private final ResourceLocation catType;

    private EntityPredicate(
        EntityTypePredicate param0,
        DistancePredicate param1,
        LocationPredicate param2,
        MobEffectsPredicate param3,
        NbtPredicate param4,
        EntityFlagsPredicate param5,
        EntityEquipmentPredicate param6,
        PlayerPredicate param7,
        FishingHookPredicate param8,
        @Nullable String param9,
        @Nullable ResourceLocation param10
    ) {
        this.entityType = param0;
        this.distanceToPlayer = param1;
        this.location = param2;
        this.effects = param3;
        this.nbt = param4;
        this.flags = param5;
        this.equipment = param6;
        this.player = param7;
        this.fishingHook = param8;
        this.team = param9;
        this.catType = param10;
    }

    public boolean matches(ServerPlayer param0, @Nullable Entity param1) {
        return this.matches(param0.getLevel(), param0.position(), param1);
    }

    public boolean matches(ServerLevel param0, @Nullable Vec3 param1, @Nullable Entity param2) {
        if (this == ANY) {
            return true;
        } else if (param2 == null) {
            return false;
        } else if (!this.entityType.matches(param2.getType())) {
            return false;
        } else {
            if (param1 == null) {
                if (this.distanceToPlayer != DistancePredicate.ANY) {
                    return false;
                }
            } else if (!this.distanceToPlayer.matches(param1.x, param1.y, param1.z, param2.getX(), param2.getY(), param2.getZ())) {
                return false;
            }

            if (!this.location.matches(param0, param2.getX(), param2.getY(), param2.getZ())) {
                return false;
            } else if (!this.effects.matches(param2)) {
                return false;
            } else if (!this.nbt.matches(param2)) {
                return false;
            } else if (!this.flags.matches(param2)) {
                return false;
            } else if (!this.equipment.matches(param2)) {
                return false;
            } else if (!this.player.matches(param2)) {
                return false;
            } else if (!this.fishingHook.matches(param2)) {
                return false;
            } else {
                if (this.team != null) {
                    Team var0 = param2.getTeam();
                    if (var0 == null || !this.team.equals(var0.getName())) {
                        return false;
                    }
                }

                return this.catType == null || param2 instanceof Cat && ((Cat)param2).getResourceLocation().equals(this.catType);
            }
        }
    }

    public static EntityPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "entity");
            EntityTypePredicate var1 = EntityTypePredicate.fromJson(var0.get("type"));
            DistancePredicate var2 = DistancePredicate.fromJson(var0.get("distance"));
            LocationPredicate var3 = LocationPredicate.fromJson(var0.get("location"));
            MobEffectsPredicate var4 = MobEffectsPredicate.fromJson(var0.get("effects"));
            NbtPredicate var5 = NbtPredicate.fromJson(var0.get("nbt"));
            EntityFlagsPredicate var6 = EntityFlagsPredicate.fromJson(var0.get("flags"));
            EntityEquipmentPredicate var7 = EntityEquipmentPredicate.fromJson(var0.get("equipment"));
            PlayerPredicate var8 = PlayerPredicate.fromJson(var0.get("player"));
            FishingHookPredicate var9 = FishingHookPredicate.fromJson(var0.get("fishing_hook"));
            String var10 = GsonHelper.getAsString(var0, "team", null);
            ResourceLocation var11 = var0.has("catType") ? new ResourceLocation(GsonHelper.getAsString(var0, "catType")) : null;
            return new EntityPredicate.Builder()
                .entityType(var1)
                .distance(var2)
                .located(var3)
                .effects(var4)
                .nbt(var5)
                .flags(var6)
                .equipment(var7)
                .player(var8)
                .fishingHook(var9)
                .team(var10)
                .catType(var11)
                .build();
        } else {
            return ANY;
        }
    }

    public static EntityPredicate[] fromJsonArray(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonArray var0 = GsonHelper.convertToJsonArray(param0, "entities");
            EntityPredicate[] var1 = new EntityPredicate[var0.size()];

            for(int var2 = 0; var2 < var0.size(); ++var2) {
                var1[var2] = fromJson(var0.get(var2));
            }

            return var1;
        } else {
            return ANY_ARRAY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("type", this.entityType.serializeToJson());
            var0.add("distance", this.distanceToPlayer.serializeToJson());
            var0.add("location", this.location.serializeToJson());
            var0.add("effects", this.effects.serializeToJson());
            var0.add("nbt", this.nbt.serializeToJson());
            var0.add("flags", this.flags.serializeToJson());
            var0.add("equipment", this.equipment.serializeToJson());
            var0.add("player", this.player.serializeToJson());
            var0.add("fishing_hook", this.fishingHook.serializeToJson());
            var0.addProperty("team", this.team);
            if (this.catType != null) {
                var0.addProperty("catType", this.catType.toString());
            }

            return var0;
        }
    }

    public static JsonElement serializeArrayToJson(EntityPredicate[] param0) {
        if (param0 == ANY_ARRAY) {
            return JsonNull.INSTANCE;
        } else {
            JsonArray var0 = new JsonArray();

            for(EntityPredicate var1 : param0) {
                JsonElement var2 = var1.serializeToJson();
                if (!var2.isJsonNull()) {
                    var0.add(var2);
                }
            }

            return var0;
        }
    }

    public static class Builder {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private PlayerPredicate player = PlayerPredicate.ANY;
        private FishingHookPredicate fishingHook = FishingHookPredicate.ANY;
        private String team;
        private ResourceLocation catType;

        public static EntityPredicate.Builder entity() {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> param0) {
            this.entityType = EntityTypePredicate.of(param0);
            return this;
        }

        public EntityPredicate.Builder of(Tag<EntityType<?>> param0) {
            this.entityType = EntityTypePredicate.of(param0);
            return this;
        }

        public EntityPredicate.Builder of(ResourceLocation param0) {
            this.catType = param0;
            return this;
        }

        public EntityPredicate.Builder entityType(EntityTypePredicate param0) {
            this.entityType = param0;
            return this;
        }

        public EntityPredicate.Builder distance(DistancePredicate param0) {
            this.distanceToPlayer = param0;
            return this;
        }

        public EntityPredicate.Builder located(LocationPredicate param0) {
            this.location = param0;
            return this;
        }

        public EntityPredicate.Builder effects(MobEffectsPredicate param0) {
            this.effects = param0;
            return this;
        }

        public EntityPredicate.Builder nbt(NbtPredicate param0) {
            this.nbt = param0;
            return this;
        }

        public EntityPredicate.Builder flags(EntityFlagsPredicate param0) {
            this.flags = param0;
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate param0) {
            this.equipment = param0;
            return this;
        }

        public EntityPredicate.Builder player(PlayerPredicate param0) {
            this.player = param0;
            return this;
        }

        public EntityPredicate.Builder fishingHook(FishingHookPredicate param0) {
            this.fishingHook = param0;
            return this;
        }

        public EntityPredicate.Builder team(@Nullable String param0) {
            this.team = param0;
            return this;
        }

        public EntityPredicate.Builder catType(@Nullable ResourceLocation param0) {
            this.catType = param0;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(
                this.entityType,
                this.distanceToPlayer,
                this.location,
                this.effects,
                this.nbt,
                this.flags,
                this.equipment,
                this.player,
                this.fishingHook,
                this.team,
                this.catType
            );
        }
    }
}
