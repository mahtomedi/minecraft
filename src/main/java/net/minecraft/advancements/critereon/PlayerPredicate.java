package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerPredicate implements EntitySubPredicate {
    public static final int LOOKING_AT_RANGE = 100;
    private final MinMaxBounds.Ints level;
    @Nullable
    private final GameType gameType;
    private final Map<Stat<?>, MinMaxBounds.Ints> stats;
    private final Object2BooleanMap<ResourceLocation> recipes;
    private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;
    private final EntityPredicate lookingAt;

    private static PlayerPredicate.AdvancementPredicate advancementPredicateFromJson(JsonElement param0) {
        if (param0.isJsonPrimitive()) {
            boolean var0 = param0.getAsBoolean();
            return new PlayerPredicate.AdvancementDonePredicate(var0);
        } else {
            Object2BooleanMap<String> var1 = new Object2BooleanOpenHashMap<>();
            JsonObject var2 = GsonHelper.convertToJsonObject(param0, "criterion data");
            var2.entrySet().forEach(param1 -> {
                boolean var0x = GsonHelper.convertToBoolean(param1.getValue(), "criterion test");
                var1.put(param1.getKey(), var0x);
            });
            return new PlayerPredicate.AdvancementCriterionsPredicate(var1);
        }
    }

    PlayerPredicate(
        MinMaxBounds.Ints param0,
        @Nullable GameType param1,
        Map<Stat<?>, MinMaxBounds.Ints> param2,
        Object2BooleanMap<ResourceLocation> param3,
        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> param4,
        EntityPredicate param5
    ) {
        this.level = param0;
        this.gameType = param1;
        this.stats = param2;
        this.recipes = param3;
        this.advancements = param4;
        this.lookingAt = param5;
    }

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (!(param0 instanceof ServerPlayer)) {
            return false;
        } else {
            ServerPlayer var0 = (ServerPlayer)param0;
            if (!this.level.matches(var0.experienceLevel)) {
                return false;
            } else if (this.gameType != null && this.gameType != var0.gameMode.getGameModeForPlayer()) {
                return false;
            } else {
                StatsCounter var1 = var0.getStats();

                for(Entry<Stat<?>, MinMaxBounds.Ints> var2 : this.stats.entrySet()) {
                    int var3 = var1.getValue(var2.getKey());
                    if (!var2.getValue().matches(var3)) {
                        return false;
                    }
                }

                RecipeBook var4 = var0.getRecipeBook();

                for(it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<ResourceLocation> var5 : this.recipes.object2BooleanEntrySet()) {
                    if (var4.contains(var5.getKey()) != var5.getBooleanValue()) {
                        return false;
                    }
                }

                if (!this.advancements.isEmpty()) {
                    PlayerAdvancements var6 = var0.getAdvancements();
                    ServerAdvancementManager var7 = var0.getServer().getAdvancements();

                    for(Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> var8 : this.advancements.entrySet()) {
                        Advancement var9 = var7.getAdvancement(var8.getKey());
                        if (var9 == null || !var8.getValue().test(var6.getOrStartProgress(var9))) {
                            return false;
                        }
                    }
                }

                if (this.lookingAt != EntityPredicate.ANY) {
                    Vec3 var10 = var0.getEyePosition();
                    Vec3 var11 = var0.getViewVector(1.0F);
                    Vec3 var12 = var10.add(var11.x * 100.0, var11.y * 100.0, var11.z * 100.0);
                    EntityHitResult var13 = ProjectileUtil.getEntityHitResult(
                        var0.level, var0, var10, var12, new AABB(var10, var12).inflate(1.0), param0x -> !param0x.isSpectator(), 0.0F
                    );
                    if (var13 == null || var13.getType() != HitResult.Type.ENTITY) {
                        return false;
                    }

                    Entity var14 = var13.getEntity();
                    if (!this.lookingAt.matches(var0, var14) || !var0.hasLineOfSight(var14)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static PlayerPredicate fromJson(JsonObject param0) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("level"));
        String var1 = GsonHelper.getAsString(param0, "gamemode", "");
        GameType var2 = GameType.byName(var1, null);
        Map<Stat<?>, MinMaxBounds.Ints> var3 = Maps.newHashMap();
        JsonArray var4 = GsonHelper.getAsJsonArray(param0, "stats", null);
        if (var4 != null) {
            for(JsonElement var5 : var4) {
                JsonObject var6 = GsonHelper.convertToJsonObject(var5, "stats entry");
                ResourceLocation var7 = new ResourceLocation(GsonHelper.getAsString(var6, "type"));
                StatType<?> var8 = BuiltInRegistries.STAT_TYPE.get(var7);
                if (var8 == null) {
                    throw new JsonParseException("Invalid stat type: " + var7);
                }

                ResourceLocation var9 = new ResourceLocation(GsonHelper.getAsString(var6, "stat"));
                Stat<?> var10 = getStat(var8, var9);
                MinMaxBounds.Ints var11 = MinMaxBounds.Ints.fromJson(var6.get("value"));
                var3.put(var10, var11);
            }
        }

        Object2BooleanMap<ResourceLocation> var12 = new Object2BooleanOpenHashMap<>();
        JsonObject var13 = GsonHelper.getAsJsonObject(param0, "recipes", new JsonObject());

        for(Entry<String, JsonElement> var14 : var13.entrySet()) {
            ResourceLocation var15 = new ResourceLocation(var14.getKey());
            boolean var16 = GsonHelper.convertToBoolean(var14.getValue(), "recipe present");
            var12.put(var15, var16);
        }

        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> var17 = Maps.newHashMap();
        JsonObject var18 = GsonHelper.getAsJsonObject(param0, "advancements", new JsonObject());

        for(Entry<String, JsonElement> var19 : var18.entrySet()) {
            ResourceLocation var20 = new ResourceLocation(var19.getKey());
            PlayerPredicate.AdvancementPredicate var21 = advancementPredicateFromJson(var19.getValue());
            var17.put(var20, var21);
        }

        EntityPredicate var22 = EntityPredicate.fromJson(param0.get("looking_at"));
        return new PlayerPredicate(var0, var2, var3, var12, var17, var22);
    }

    private static <T> Stat<T> getStat(StatType<T> param0, ResourceLocation param1) {
        Registry<T> var0 = param0.getRegistry();
        T var1 = var0.get(param1);
        if (var1 == null) {
            throw new JsonParseException("Unknown object " + param1 + " for stat type " + BuiltInRegistries.STAT_TYPE.getKey(param0));
        } else {
            return param0.get(var1);
        }
    }

    private static <T> ResourceLocation getStatValueId(Stat<T> param0) {
        return param0.getType().getRegistry().getKey(param0.getValue());
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject var0 = new JsonObject();
        var0.add("level", this.level.serializeToJson());
        if (this.gameType != null) {
            var0.addProperty("gamemode", this.gameType.getName());
        }

        if (!this.stats.isEmpty()) {
            JsonArray var1 = new JsonArray();
            this.stats.forEach((param1, param2) -> {
                JsonObject var0x = new JsonObject();
                var0x.addProperty("type", BuiltInRegistries.STAT_TYPE.getKey(param1.getType()).toString());
                var0x.addProperty("stat", getStatValueId(param1).toString());
                var0x.add("value", param2.serializeToJson());
                var1.add(var0x);
            });
            var0.add("stats", var1);
        }

        if (!this.recipes.isEmpty()) {
            JsonObject var2 = new JsonObject();
            this.recipes.forEach((param1, param2) -> var2.addProperty(param1.toString(), param2));
            var0.add("recipes", var2);
        }

        if (!this.advancements.isEmpty()) {
            JsonObject var3 = new JsonObject();
            this.advancements.forEach((param1, param2) -> var3.add(param1.toString(), param2.toJson()));
            var0.add("advancements", var3);
        }

        var0.add("looking_at", this.lookingAt.serializeToJson());
        return var0;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.PLAYER;
    }

    static class AdvancementCriterionsPredicate implements PlayerPredicate.AdvancementPredicate {
        private final Object2BooleanMap<String> criterions;

        public AdvancementCriterionsPredicate(Object2BooleanMap<String> param0) {
            this.criterions = param0;
        }

        @Override
        public JsonElement toJson() {
            JsonObject var0 = new JsonObject();
            this.criterions.forEach(var0::addProperty);
            return var0;
        }

        public boolean test(AdvancementProgress param0) {
            for(it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<String> var0 : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress var1 = param0.getCriterion(var0.getKey());
                if (var1 == null || var1.isDone() != var0.getBooleanValue()) {
                    return false;
                }
            }

            return true;
        }
    }

    static class AdvancementDonePredicate implements PlayerPredicate.AdvancementPredicate {
        private final boolean state;

        public AdvancementDonePredicate(boolean param0) {
            this.state = param0;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.state);
        }

        public boolean test(AdvancementProgress param0) {
            return param0.isDone() == this.state;
        }
    }

    interface AdvancementPredicate extends Predicate<AdvancementProgress> {
        JsonElement toJson();
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        @Nullable
        private GameType gameType;
        private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
        private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();
        private EntityPredicate lookingAt = EntityPredicate.ANY;

        public static PlayerPredicate.Builder player() {
            return new PlayerPredicate.Builder();
        }

        public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints param0) {
            this.level = param0;
            return this;
        }

        public PlayerPredicate.Builder addStat(Stat<?> param0, MinMaxBounds.Ints param1) {
            this.stats.put(param0, param1);
            return this;
        }

        public PlayerPredicate.Builder addRecipe(ResourceLocation param0, boolean param1) {
            this.recipes.put(param0, param1);
            return this;
        }

        public PlayerPredicate.Builder setGameType(GameType param0) {
            this.gameType = param0;
            return this;
        }

        public PlayerPredicate.Builder setLookingAt(EntityPredicate param0) {
            this.lookingAt = param0;
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation param0, boolean param1) {
            this.advancements.put(param0, new PlayerPredicate.AdvancementDonePredicate(param1));
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation param0, Map<String, Boolean> param1) {
            this.advancements.put(param0, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(param1)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements, this.lookingAt);
        }
    }
}
