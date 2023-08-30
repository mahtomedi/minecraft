package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public record EntityPredicate(
    Optional<EntityTypePredicate> entityType,
    Optional<DistancePredicate> distanceToPlayer,
    Optional<LocationPredicate> location,
    Optional<LocationPredicate> steppingOnLocation,
    Optional<MobEffectsPredicate> effects,
    Optional<NbtPredicate> nbt,
    Optional<EntityFlagsPredicate> flags,
    Optional<EntityEquipmentPredicate> equipment,
    Optional<EntitySubPredicate> subPredicate,
    Optional<EntityPredicate> vehicle,
    Optional<EntityPredicate> passenger,
    Optional<EntityPredicate> targetedEntity,
    Optional<String> team
) {
    public static final Codec<EntityPredicate> CODEC = ExtraCodecs.recursive(
        param0 -> RecordCodecBuilder.create(
                param1 -> param1.group(
                            ExtraCodecs.strictOptionalField(EntityTypePredicate.CODEC, "type").forGetter(EntityPredicate::entityType),
                            ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(EntityPredicate::distanceToPlayer),
                            ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(EntityPredicate::location),
                            ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "stepping_on").forGetter(EntityPredicate::steppingOnLocation),
                            ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "effects").forGetter(EntityPredicate::effects),
                            ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(EntityPredicate::nbt),
                            ExtraCodecs.strictOptionalField(EntityFlagsPredicate.CODEC, "flags").forGetter(EntityPredicate::flags),
                            ExtraCodecs.strictOptionalField(EntityEquipmentPredicate.CODEC, "equipment").forGetter(EntityPredicate::equipment),
                            ExtraCodecs.strictOptionalField(EntitySubPredicate.CODEC, "type_specific").forGetter(EntityPredicate::subPredicate),
                            ExtraCodecs.strictOptionalField(param0, "vehicle").forGetter(EntityPredicate::vehicle),
                            ExtraCodecs.strictOptionalField(param0, "passenger").forGetter(EntityPredicate::passenger),
                            ExtraCodecs.strictOptionalField(param0, "targeted_entity").forGetter(EntityPredicate::targetedEntity),
                            ExtraCodecs.strictOptionalField(Codec.STRING, "team").forGetter(EntityPredicate::team)
                        )
                        .apply(param1, EntityPredicate::new)
            )
    );

    public static Optional<ContextAwarePredicate> fromJson(JsonObject param0, String param1, DeserializationContext param2) {
        JsonElement var0 = param0.get(param1);
        return fromElement(param1, param2, var0);
    }

    public static List<ContextAwarePredicate> fromJsonArray(JsonObject param0, String param1, DeserializationContext param2) {
        JsonElement var0 = param0.get(param1);
        if (var0 != null && !var0.isJsonNull()) {
            JsonArray var1 = GsonHelper.convertToJsonArray(var0, param1);
            List<ContextAwarePredicate> var2 = new ArrayList<>(var1.size());

            for(int var3 = 0; var3 < var1.size(); ++var3) {
                fromElement(param1 + "[" + var3 + "]", param2, var1.get(var3)).ifPresent(var2::add);
            }

            return List.copyOf(var2);
        } else {
            return List.of();
        }
    }

    private static Optional<ContextAwarePredicate> fromElement(String param0, DeserializationContext param1, @Nullable JsonElement param2) {
        Optional<Optional<ContextAwarePredicate>> var0 = ContextAwarePredicate.fromElement(param0, param1, param2, LootContextParamSets.ADVANCEMENT_ENTITY);
        if (var0.isPresent()) {
            return var0.get();
        } else {
            Optional<EntityPredicate> var1 = fromJson(param2);
            return wrap(var1);
        }
    }

    public static ContextAwarePredicate wrap(EntityPredicate.Builder param0) {
        return wrap(param0.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> param0) {
        return param0.map(EntityPredicate::wrap);
    }

    public static List<ContextAwarePredicate> wrap(EntityPredicate.Builder... param0) {
        return Stream.of(param0).map(EntityPredicate::wrap).toList();
    }

    public static ContextAwarePredicate wrap(EntityPredicate param0x) {
        LootItemCondition var0 = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, param0x).build();
        return new ContextAwarePredicate(List.of(var0));
    }

    public boolean matches(ServerPlayer param0, @Nullable Entity param1) {
        return this.matches(param0.serverLevel(), param0.position(), param1);
    }

    public boolean matches(ServerLevel param0, @Nullable Vec3 param1, @Nullable Entity param2) {
        if (param2 == null) {
            return false;
        } else if (this.entityType.isPresent() && !this.entityType.get().matches(param2.getType())) {
            return false;
        } else {
            if (param1 == null) {
                if (this.distanceToPlayer.isPresent()) {
                    return false;
                }
            } else if (this.distanceToPlayer.isPresent()
                && !this.distanceToPlayer.get().matches(param1.x, param1.y, param1.z, param2.getX(), param2.getY(), param2.getZ())) {
                return false;
            }

            if (this.location.isPresent() && !this.location.get().matches(param0, param2.getX(), param2.getY(), param2.getZ())) {
                return false;
            } else {
                if (this.steppingOnLocation.isPresent()) {
                    Vec3 var0 = Vec3.atCenterOf(param2.getOnPos());
                    if (!this.steppingOnLocation.get().matches(param0, var0.x(), var0.y(), var0.z())) {
                        return false;
                    }
                }

                if (this.effects.isPresent() && !this.effects.get().matches(param2)) {
                    return false;
                } else if (this.nbt.isPresent() && !this.nbt.get().matches(param2)) {
                    return false;
                } else if (this.flags.isPresent() && !this.flags.get().matches(param2)) {
                    return false;
                } else if (this.equipment.isPresent() && !this.equipment.get().matches(param2)) {
                    return false;
                } else if (this.subPredicate.isPresent() && !this.subPredicate.get().matches(param2, param0, param1)) {
                    return false;
                } else if (this.vehicle.isPresent() && !this.vehicle.get().matches(param0, param1, param2.getVehicle())) {
                    return false;
                } else if (this.passenger.isPresent()
                    && param2.getPassengers().stream().noneMatch(param2x -> this.passenger.get().matches(param0, param1, param2x))) {
                    return false;
                } else if (this.targetedEntity.isPresent()
                    && !this.targetedEntity.get().matches(param0, param1, param2 instanceof Mob ? ((Mob)param2).getTarget() : null)) {
                    return false;
                } else {
                    if (this.team.isPresent()) {
                        Team var1 = param2.getTeam();
                        if (var1 == null || !this.team.get().equals(var1.getName())) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public static Optional<EntityPredicate> fromJson(@Nullable JsonElement param0) {
        return param0 != null && !param0.isJsonNull()
            ? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new))
            : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static LootContext createContext(ServerPlayer param0, Entity param1) {
        LootParams var0 = new LootParams.Builder(param0.serverLevel())
            .withParameter(LootContextParams.THIS_ENTITY, param1)
            .withParameter(LootContextParams.ORIGIN, param0.position())
            .create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(var0).create(Optional.empty());
    }

    public static class Builder {
        private Optional<EntityTypePredicate> entityType = Optional.empty();
        private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
        private Optional<MobEffectsPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();

        public static EntityPredicate.Builder entity() {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> param0) {
            this.entityType = Optional.of(EntityTypePredicate.of(param0));
            return this;
        }

        public EntityPredicate.Builder of(TagKey<EntityType<?>> param0) {
            this.entityType = Optional.of(EntityTypePredicate.of(param0));
            return this;
        }

        public EntityPredicate.Builder entityType(EntityTypePredicate param0) {
            this.entityType = Optional.of(param0);
            return this;
        }

        public EntityPredicate.Builder distance(DistancePredicate param0) {
            this.distanceToPlayer = Optional.of(param0);
            return this;
        }

        public EntityPredicate.Builder located(LocationPredicate.Builder param0) {
            this.location = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder steppingOn(LocationPredicate.Builder param0) {
            this.steppingOnLocation = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder effects(MobEffectsPredicate.Builder param0) {
            this.effects = param0.build();
            return this;
        }

        public EntityPredicate.Builder nbt(NbtPredicate param0) {
            this.nbt = Optional.of(param0);
            return this;
        }

        public EntityPredicate.Builder flags(EntityFlagsPredicate.Builder param0) {
            this.flags = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate.Builder param0) {
            this.equipment = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate param0) {
            this.equipment = Optional.of(param0);
            return this;
        }

        public EntityPredicate.Builder subPredicate(EntitySubPredicate param0) {
            this.subPredicate = Optional.of(param0);
            return this;
        }

        public EntityPredicate.Builder vehicle(EntityPredicate.Builder param0) {
            this.vehicle = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder passenger(EntityPredicate.Builder param0) {
            this.passenger = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder targetedEntity(EntityPredicate.Builder param0) {
            this.targetedEntity = Optional.of(param0.build());
            return this;
        }

        public EntityPredicate.Builder team(String param0) {
            this.team = Optional.of(param0);
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
}
