package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
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
        MobEffectsPredicate.ANY,
        NbtPredicate.ANY,
        EntityFlagsPredicate.ANY,
        EntityEquipmentPredicate.ANY,
        PlayerPredicate.ANY,
        FishingHookPredicate.ANY,
        EntityPredicate.ANY,
        EntityPredicate.ANY,
        null,
        null
    );
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
    private final EntityPredicate vehicle;
    private final EntityPredicate targetedEntity;
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
        EntityPredicate param9,
        EntityPredicate param10,
        @Nullable String param11,
        @Nullable ResourceLocation param12
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
        this.vehicle = param9;
        this.targetedEntity = param10;
        this.team = param11;
        this.catType = param12;
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
            } else if (!this.vehicle.matches(param0, param1, param2.getVehicle())) {
                return false;
            } else if (!this.targetedEntity.matches(param0, param1, param2 instanceof Mob ? ((Mob)param2).getTarget() : null)) {
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
            EntityPredicate var10 = fromJson(var0.get("vehicle"));
            EntityPredicate var11 = fromJson(var0.get("targeted_entity"));
            String var12 = GsonHelper.getAsString(var0, "team", null);
            ResourceLocation var13 = var0.has("catType") ? new ResourceLocation(GsonHelper.getAsString(var0, "catType")) : null;
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
                .team(var12)
                .vehicle(var10)
                .targetedEntity(var11)
                .catType(var13)
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
            var0.add("effects", this.effects.serializeToJson());
            var0.add("nbt", this.nbt.serializeToJson());
            var0.add("flags", this.flags.serializeToJson());
            var0.add("equipment", this.equipment.serializeToJson());
            var0.add("player", this.player.serializeToJson());
            var0.add("fishing_hook", this.fishingHook.serializeToJson());
            var0.add("vehicle", this.vehicle.serializeToJson());
            var0.add("targeted_entity", this.targetedEntity.serializeToJson());
            var0.addProperty("team", this.team);
            if (this.catType != null) {
                var0.addProperty("catType", this.catType.toString());
            }

            return var0;
        }
    }

    public static LootContext createContext(ServerPlayer param0, Entity param1) {
        return new LootContext.Builder(param0.getLevel())
            .withParameter(LootContextParams.THIS_ENTITY, param1)
            .withParameter(LootContextParams.BLOCK_POS, param1.blockPosition())
            .withParameter(LootContextParams.ORIGIN, param0.position())
            .withRandom(param0.getRandom())
            .create(LootContextParamSets.ADVANCEMENT_ENTITY);
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
        private EntityPredicate vehicle = EntityPredicate.ANY;
        private EntityPredicate targetedEntity = EntityPredicate.ANY;
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

        public EntityPredicate.Builder vehicle(EntityPredicate param0) {
            this.vehicle = param0;
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
                this.vehicle,
                this.targetedEntity,
                this.team,
                this.catType
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
                    param2.getAsJsonArray(), param1.getAdvancementId().toString() + "/" + param0, LootContextParamSets.ADVANCEMENT_ENTITY
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
