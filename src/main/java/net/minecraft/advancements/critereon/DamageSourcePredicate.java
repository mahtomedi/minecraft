package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate {
    public static final DamageSourcePredicate ANY = DamageSourcePredicate.Builder.damageType().build();
    private final List<TagPredicate<DamageType>> tags;
    private final EntityPredicate directEntity;
    private final EntityPredicate sourceEntity;

    public DamageSourcePredicate(List<TagPredicate<DamageType>> param0, EntityPredicate param1, EntityPredicate param2) {
        this.tags = param0;
        this.directEntity = param1;
        this.sourceEntity = param2;
    }

    public boolean matches(ServerPlayer param0, DamageSource param1) {
        return this.matches(param0.getLevel(), param0.position(), param1);
    }

    public boolean matches(ServerLevel param0, Vec3 param1, DamageSource param2) {
        if (this == ANY) {
            return true;
        } else {
            for(TagPredicate<DamageType> var0 : this.tags) {
                if (!var0.matches(param2.typeHolder())) {
                    return false;
                }
            }

            if (!this.directEntity.matches(param0, param1, param2.getDirectEntity())) {
                return false;
            } else {
                return this.sourceEntity.matches(param0, param1, param2.getEntity());
            }
        }
    }

    public static DamageSourcePredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "damage type");
            JsonArray var1 = GsonHelper.getAsJsonArray(var0, "tags", null);
            List<TagPredicate<DamageType>> var2;
            if (var1 != null) {
                var2 = new ArrayList<>(var1.size());

                for(JsonElement var3 : var1) {
                    var2.add(TagPredicate.fromJson(var3, Registries.DAMAGE_TYPE));
                }
            } else {
                var2 = List.of();
            }

            EntityPredicate var5 = EntityPredicate.fromJson(var0.get("direct_entity"));
            EntityPredicate var6 = EntityPredicate.fromJson(var0.get("source_entity"));
            return new DamageSourcePredicate(var2, var5, var6);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (!this.tags.isEmpty()) {
                JsonArray var1 = new JsonArray(this.tags.size());

                for(int var2 = 0; var2 < this.tags.size(); ++var2) {
                    var1.add(this.tags.get(var2).serializeToJson());
                }

                var0.add("tags", var1);
            }

            var0.add("direct_entity", this.directEntity.serializeToJson());
            var0.add("source_entity", this.sourceEntity.serializeToJson());
            return var0;
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private EntityPredicate directEntity = EntityPredicate.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;

        public static DamageSourcePredicate.Builder damageType() {
            return new DamageSourcePredicate.Builder();
        }

        public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> param0) {
            this.tags.add(param0);
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate param0) {
            this.directEntity = param0;
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate.Builder param0) {
            this.directEntity = param0.build();
            return this;
        }

        public DamageSourcePredicate.Builder source(EntityPredicate param0) {
            this.sourceEntity = param0;
            return this;
        }

        public DamageSourcePredicate.Builder source(EntityPredicate.Builder param0) {
            this.sourceEntity = param0.build();
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate(this.tags.build(), this.directEntity, this.sourceEntity);
        }
    }
}
