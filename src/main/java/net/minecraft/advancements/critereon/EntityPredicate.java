package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
    public static final EntityPredicate ANY = new EntityPredicate(
        EntityTypePredicate.ANY,
        DistancePredicate.ANY,
        LocationPredicate.ANY,
        LocationPredicate.ANY,
        MobEffectsPredicate.ANY,
        NbtPredicate.ANY,
        EntityFlagsPredicate.ANY,
        EntityEquipmentPredicate.ANY,
        EntitySubPredicate.ANY,
        null
    );
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final LocationPredicate steppingOnLocation;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final EntitySubPredicate subPredicate;
    private final EntityPredicate vehicle;
    private final EntityPredicate passenger;
    private final EntityPredicate targetedEntity;
    @Nullable
    private final String team;

    private EntityPredicate(
        EntityTypePredicate param0,
        DistancePredicate param1,
        LocationPredicate param2,
        LocationPredicate param3,
        MobEffectsPredicate param4,
        NbtPredicate param5,
        EntityFlagsPredicate param6,
        EntityEquipmentPredicate param7,
        EntitySubPredicate param8,
        @Nullable String param9
    ) {
        this.entityType = param0;
        this.distanceToPlayer = param1;
        this.location = param2;
        this.steppingOnLocation = param3;
        this.effects = param4;
        this.nbt = param5;
        this.flags = param6;
        this.equipment = param7;
        this.subPredicate = param8;
        this.passenger = this;
        this.vehicle = this;
        this.targetedEntity = this;
        this.team = param9;
    }

    EntityPredicate(
        EntityTypePredicate param0,
        DistancePredicate param1,
        LocationPredicate param2,
        LocationPredicate param3,
        MobEffectsPredicate param4,
        NbtPredicate param5,
        EntityFlagsPredicate param6,
        EntityEquipmentPredicate param7,
        EntitySubPredicate param8,
        EntityPredicate param9,
        EntityPredicate param10,
        EntityPredicate param11,
        @Nullable String param12
    ) {
        this.entityType = param0;
        this.distanceToPlayer = param1;
        this.location = param2;
        this.steppingOnLocation = param3;
        this.effects = param4;
        this.nbt = param5;
        this.flags = param6;
        this.equipment = param7;
        this.subPredicate = param8;
        this.vehicle = param9;
        this.passenger = param10;
        this.targetedEntity = param11;
        this.team = param12;
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
            } else {
                if (this.steppingOnLocation != LocationPredicate.ANY) {
                    Vec3 var0 = Vec3.atCenterOf(param2.getOnPosLegacy());
                    if (!this.steppingOnLocation.matches(param0, var0.x(), var0.y(), var0.z())) {
                        return false;
                    }
                }

                if (!this.effects.matches(param2)) {
                    return false;
                } else if (!this.nbt.matches(param2)) {
                    return false;
                } else if (!this.flags.matches(param2)) {
                    return false;
                } else if (!this.equipment.matches(param2)) {
                    return false;
                } else if (!this.subPredicate.matches(param2, param0, param1)) {
                    return false;
                } else if (!this.vehicle.matches(param0, param1, param2.getVehicle())) {
                    return false;
                } else if (this.passenger != ANY && param2.getPassengers().stream().noneMatch(param2x -> this.passenger.matches(param0, param1, param2x))) {
                    return false;
                } else if (!this.targetedEntity.matches(param0, param1, param2 instanceof Mob ? ((Mob)param2).getTarget() : null)) {
                    return false;
                } else {
                    if (this.team != null) {
                        Team var1 = param2.getTeam();
                        if (var1 == null || !this.team.equals(var1.getName())) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public static EntityPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "entity");
            EntityTypePredicate var1 = EntityTypePredicate.fromJson(var0.get("type"));
            DistancePredicate var2 = DistancePredicate.fromJson(var0.get("distance"));
            LocationPredicate var3 = LocationPredicate.fromJson(var0.get("location"));
            LocationPredicate var4 = LocationPredicate.fromJson(var0.get("stepping_on"));
            MobEffectsPredicate var5 = MobEffectsPredicate.fromJson(var0.get("effects"));
            NbtPredicate var6 = NbtPredicate.fromJson(var0.get("nbt"));
            EntityFlagsPredicate var7 = EntityFlagsPredicate.fromJson(var0.get("flags"));
            EntityEquipmentPredicate var8 = EntityEquipmentPredicate.fromJson(var0.get("equipment"));
            EntitySubPredicate var9 = EntitySubPredicate.fromJson(var0.get("type_specific"));
            EntityPredicate var10 = fromJson(var0.get("vehicle"));
            EntityPredicate var11 = fromJson(var0.get("passenger"));
            EntityPredicate var12 = fromJson(var0.get("targeted_entity"));
            String var13 = GsonHelper.getAsString(var0, "team", null);
            return new EntityPredicate.Builder()
                .entityType(var1)
                .distance(var2)
                .located(var3)
                .steppingOn(var4)
                .effects(var5)
                .nbt(var6)
                .flags(var7)
                .equipment(var8)
                .subPredicate(var9)
                .team(var13)
                .vehicle(var10)
                .passenger(var11)
                .targetedEntity(var12)
                .build();
        } else {
            return ANY;
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
            var0.add("stepping_on", this.steppingOnLocation.serializeToJson());
            var0.add("effects", this.effects.serializeToJson());
            var0.add("nbt", this.nbt.serializeToJson());
            var0.add("flags", this.flags.serializeToJson());
            var0.add("equipment", this.equipment.serializeToJson());
            var0.add("type_specific", this.subPredicate.serialize());
            var0.add("vehicle", this.vehicle.serializeToJson());
            var0.add("passenger", this.passenger.serializeToJson());
            var0.add("targeted_entity", this.targetedEntity.serializeToJson());
            var0.addProperty("team", this.team);
            return var0;
        }
    }

    public static LootContext createContext(ServerPlayer param0, Entity param1) {
        return new LootContext.Builder(param0.getLevel())
            .withParameter(LootContextParams.THIS_ENTITY, param1)
            .withParameter(LootContextParams.ORIGIN, param0.position())
            .withRandom(param0.getRandom())
            .create(LootContextParamSets.ADVANCEMENT_ENTITY);
    }

    public static class Builder {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private EntitySubPredicate subPredicate = EntitySubPredicate.ANY;
        private EntityPredicate vehicle = EntityPredicate.ANY;
        private EntityPredicate passenger = EntityPredicate.ANY;
        private EntityPredicate targetedEntity = EntityPredicate.ANY;
        @Nullable
        private String team;

        public static EntityPredicate.Builder entity() {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> param0) {
            this.entityType = EntityTypePredicate.of(param0);
            return this;
        }

        public EntityPredicate.Builder of(TagKey<EntityType<?>> param0) {
            this.entityType = EntityTypePredicate.of(param0);
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

        public EntityPredicate.Builder steppingOn(LocationPredicate param0) {
            this.steppingOnLocation = param0;
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

        public EntityPredicate.Builder subPredicate(EntitySubPredicate param0) {
            this.subPredicate = param0;
            return this;
        }

        public EntityPredicate.Builder vehicle(EntityPredicate param0) {
            this.vehicle = param0;
            return this;
        }

        public EntityPredicate.Builder passenger(EntityPredicate param0) {
            this.passenger = param0;
            return this;
        }

        public EntityPredicate.Builder targetedEntity(EntityPredicate param0) {
            this.targetedEntity = param0;
            return this;
        }

        public EntityPredicate.Builder team(@Nullable String param0) {
            this.team = param0;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(
                this.entityType,
                this.distanceToPlayer,
                this.location,
                this.steppingOnLocation,
                this.effects,
                this.nbt,
                this.flags,
                this.equipment,
                this.subPredicate,
                this.vehicle,
                this.passenger,
                this.targetedEntity,
                this.team
            );
        }
    }

    public static class Composite {
        public static final EntityPredicate.Composite ANY = new EntityPredicate.Composite(new LootItemCondition[0]);
        private final LootItemCondition[] conditions;
        private final Predicate<LootContext> compositePredicates;

        private Composite(LootItemCondition[] param0) {
            this.conditions = param0;
            this.compositePredicates = LootItemConditions.andConditions(param0);
        }

        public static EntityPredicate.Composite create(LootItemCondition... param0) {
            return new EntityPredicate.Composite(param0);
        }

        public static EntityPredicate.Composite fromJson(JsonObject param0, String param1, DeserializationContext param2) {
            JsonElement var0 = param0.get(param1);
            return fromElement(param1, param2, var0);
        }

        public static EntityPredicate.Composite[] fromJsonArray(JsonObject param0, String param1, DeserializationContext param2) {
            JsonElement var0 = param0.get(param1);
            if (var0 != null && !var0.isJsonNull()) {
                JsonArray var1 = GsonHelper.convertToJsonArray(var0, param1);
                EntityPredicate.Composite[] var2 = new EntityPredicate.Composite[var1.size()];

                for(int var3 = 0; var3 < var1.size(); ++var3) {
                    var2[var3] = fromElement(param1 + "[" + var3 + "]", param2, var1.get(var3));
                }

                return var2;
            } else {
                return new EntityPredicate.Composite[0];
            }
        }

        private static EntityPredicate.Composite fromElement(String param0, DeserializationContext param1, @Nullable JsonElement param2) {
            if (param2 != null && param2.isJsonArray()) {
                LootItemCondition[] var0 = param1.deserializeConditions(
                    param2.getAsJsonArray(), param1.getAdvancementId() + "/" + param0, LootContextParamSets.ADVANCEMENT_ENTITY
                );
                return new EntityPredicate.Composite(var0);
            } else {
                EntityPredicate var1 = EntityPredicate.fromJson(param2);
                return wrap(var1);
            }
        }

        public static EntityPredicate.Composite wrap(EntityPredicate param0) {
            if (param0 == EntityPredicate.ANY) {
                return ANY;
            } else {
                LootItemCondition var0 = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, param0).build();
                return new EntityPredicate.Composite(new LootItemCondition[]{var0});
            }
        }

        public boolean matches(LootContext param0) {
            return this.compositePredicates.test(param0);
        }

        public JsonElement toJson(SerializationContext param0) {
            return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : param0.serializeConditions(this.conditions));
        }

        public static JsonElement toJson(EntityPredicate.Composite[] param0, SerializationContext param1) {
            if (param0.length == 0) {
                return JsonNull.INSTANCE;
            } else {
                JsonArray var0 = new JsonArray();

                for(EntityPredicate.Composite var1 : param0) {
                    var0.add(var1.toJson(param1));
                }

                return var0;
            }
        }
    }
}
